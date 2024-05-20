package org.students.simplebitcoinnode.service.cron;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.students.simplebitcoinnode.config.BlockchainMiningConfig;
import org.students.simplebitcoinnode.dto.BlockIntroductionDTO;
import org.students.simplebitcoinnode.dto.PoolInitiationBlockMetadataDTO;
import org.students.simplebitcoinnode.dto.PoolMiningProposalDTO;
import org.students.simplebitcoinnode.entity.AdjacentNode;
import org.students.simplebitcoinnode.entity.Block;
import org.students.simplebitcoinnode.entity.MerkleTreeNode;
import org.students.simplebitcoinnode.entity.Transaction;
import org.students.simplebitcoinnode.event.MineBlockEvent;
import org.students.simplebitcoinnode.exceptions.encoding.InvalidEncodedStringException;
import org.students.simplebitcoinnode.exceptions.encoding.SerializationException;
import org.students.simplebitcoinnode.repository.AdjacentNodeRepository;
import org.students.simplebitcoinnode.repository.BlockRepository;
import org.students.simplebitcoinnode.repository.TransactionRepository;
import org.students.simplebitcoinnode.service.BlockBuilderService;
import org.students.simplebitcoinnode.util.DTOMapperWrapper;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Cron job responsible for building the block and, if configured, mining it
 */
@Service
public class BlockBuilderCronService {
    private final Logger logger = Logger.getLogger(BlockBuilderCronService.class.getName());

    private final ApplicationEventPublisher applicationEventPublisher;
    private final BlockBuilderService blockBuilderService;
    private final TransactionRepository transactionRepository;
    private final AdjacentNodeRepository adjacentNodeRepository;
    private final BlockRepository blockRepository;
    private final BlockchainMiningConfig blockchainMiningConfig;
    private final DTOMapperWrapper dtoMapperWrapper;
    private final HttpClient httpClient;

    public BlockBuilderCronService(ApplicationEventPublisher applicationEventPublisher,
                                   BlockBuilderService blockBuilderService,
                                   TransactionRepository transactionRepository,
                                   AdjacentNodeRepository adjacentNodeRepository,
                                   BlockRepository blockRepository,
                                   BlockchainMiningConfig blockchainMiningConfig,
                                   DTOMapperWrapper dtoMapperWrapper,
                                   HttpClient httpClient) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.blockBuilderService = blockBuilderService;
        this.transactionRepository = transactionRepository;
        this.adjacentNodeRepository = adjacentNodeRepository;
        this.blockchainMiningConfig = blockchainMiningConfig;
        this.blockRepository = blockRepository;
        this.dtoMapperWrapper = dtoMapperWrapper;
        this.httpClient = httpClient;
    }


    /**
     * Scheduled method that periodically checks if blocks should be constructed by looking at the amount of unverified transactions in the database
     */
    @Scheduled(cron = "${blockchain.mining.block-construction-cron}")
    public void scheduledBlockBuildingAndMining() {
        long unverified = transactionRepository.findAmountOfUnverifiedTransactions();
        if (unverified >= blockchainMiningConfig.getTransactionsPerBlock()) {
            try {
                List<Transaction> transactions = transactionRepository.findUnverifiedTransactionsLimitByN(blockchainMiningConfig.getTransactionsPerBlock());
                MerkleTreeNode root = blockBuilderService.createMerkleTreeRoot(transactions);
                Block previousBlock = blockRepository.findBlockWithLongestChain();
                Block block = blockBuilderService.newBlock(root, previousBlock.getHash());
                try {
                    List<AdjacentNode> consentedNodes = proposePoolMining(dtoMapperWrapper.map(block, BlockIntroductionDTO.class));
                    logger.info("Starting pool mining with " + consentedNodes.size() + " nodes (combined hash rate: " + consentedNodes.stream().map(AdjacentNode::getAverageHashRate).reduce(0.f, Float::sum) + " MH/s)");
                    initiatePoolMining(consentedNodes, block.getHash());
                    applicationEventPublisher.publishEvent(new MineBlockEvent(this, block, BigInteger.valueOf(consentedNodes.size()), BigInteger.valueOf(consentedNodes.size() + 1L)));
                }
                catch (IOException e) {
                    applicationEventPublisher.publishEvent(new MineBlockEvent(this, block, BigInteger.ZERO, BigInteger.ONE));
                }
            }
            catch (InvalidEncodedStringException e) {
                logger.severe("Failed to build a merkle tree from transactions: " + e.getMessage());
                logger.severe("This could indicate a severe problem with transaction processing");
            }
        }
    }

    /**
     * Signals adjacent nodes that accepted pool mining proposal to initiate block mining on their systems
     * @param consentedNodes represents the list of adjacent nodes that accepted pool mining proposal
     * @param hash represents the current hash of the block to mine
     */
    private void initiatePoolMining(List<AdjacentNode> consentedNodes, String hash) {
        PoolInitiationBlockMetadataDTO poolInitiationBlockMetadataDTO = PoolInitiationBlockMetadataDTO.builder()
                .miners(consentedNodes.stream().map(AdjacentNode::getPubKey).toList())
                .hash(hash).build();

        ObjectMapper mapper = new ObjectMapper();

        for (int i = 0; i < consentedNodes.size(); i++) {
            String baseURL = (consentedNodes.get(i).isTls() ? "https://" : "http://") + consentedNodes.get(i).getHostname() + "/blockchain/mine/initiate";
            poolInitiationBlockMetadataDTO.setOffset(i);
            try {
                String json = mapper.writeValueAsString(poolInitiationBlockMetadataDTO);
                httpClient.send(buildJsonPostRequest(new URI(baseURL), json), HttpResponse.BodyHandlers.discarding());
            }
            catch (Exception e) {
                logger.warning("Could not initiate pool mining for node with URL: " + baseURL);
            }
        }
    }

    /**
     * Helper method that creates HTTP POST request using json body
     * @param uri specifies the destination URI where to make a request
     * @param json represents the JSON body
     * @return HttpRequest object that can be used to make a request with HttpClient API
     */
    private HttpRequest buildJsonPostRequest(URI uri, String json) {
        return HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .header("User-Agent", "SimpleBitcoinNode v1.0")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
    }

    /**
     * Propose pool mining to adjacent nodes and return a list of adjacent nodes that accepted the proposal
     * @param blockIntroductionDTO represents the block to use for mining
     * @return list of adjacent node objects each representing a node that accepted the proposal
     */
    private List<AdjacentNode> proposePoolMining(BlockIntroductionDTO blockIntroductionDTO) throws IOException {
        List<AdjacentNode> consentedNodes = new ArrayList<>();
        List<AdjacentNode> adjacentNodes = adjacentNodeRepository.findBestNodesByHashRate(Float.MAX_VALUE, blockchainMiningConfig.getPool().getMaximumPoolRequests());

        PoolMiningProposalDTO proposal = new PoolMiningProposalDTO(blockIntroductionDTO, blockchainMiningConfig.getPool().getMaximumPoolRequests());
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(proposal);

        int consentedNodeCount = 0;
        while (!adjacentNodes.isEmpty() && consentedNodeCount < blockchainMiningConfig.getPool().getMaximumPoolRequests()) {
            for (AdjacentNode adjacentNode : adjacentNodes) {
                String baseURL = (adjacentNode.isTls() ? "https://" : "http://") + adjacentNode.getHostname() + "/blockchain/mine/propose";
                logger.info("Trying to make a proposal mining request to " + baseURL);

                try {
                    HttpResponse<String> response = httpClient.send(buildJsonPostRequest(new URI(baseURL), json), HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() != 200) {
                        logger.warning("Could not make a proposal mining request to " + baseURL + " invalid status code: " + response.statusCode());
                        continue;
                    }

                    consentedNodeCount++;
                    consentedNodes.add(adjacentNode);
                } catch (Exception e) {
                    logger.warning("An exception has occurred while making a proposal mining request: " + e.getMessage());
                }
            }
            adjacentNodes = adjacentNodeRepository.findBestNodesByHashRate(adjacentNodes.getLast().getAverageHashRate(), blockchainMiningConfig.getPool().getMaximumPoolRequests());
        }

        return consentedNodes;
    }

    /**
     * Helper method that constructs a new block to propose
     * @param merkleTreeNode specifies the merkle tree root node to use in the block
     * @return BlockIntroductionDTO object representing the block ready to use for proposals
     */
    private BlockIntroductionDTO constructBlock(MerkleTreeNode merkleTreeNode) throws SerializationException {
        Block previous = blockRepository.findBlockWithLongestChain();
        Block block = blockBuilderService.newBlock(merkleTreeNode, previous.getHash());
        return dtoMapperWrapper.map(block, BlockIntroductionDTO.class);
    }
}
