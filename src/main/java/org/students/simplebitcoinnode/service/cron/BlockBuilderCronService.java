package org.students.simplebitcoinnode.service.cron;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.students.simplebitcoinnode.config.BlockchainMiningConfig;
import org.students.simplebitcoinnode.dto.BlockIntroductionDTO;
import org.students.simplebitcoinnode.entity.*;
import org.students.simplebitcoinnode.event.MineBlockEvent;
import org.students.simplebitcoinnode.exceptions.encoding.InvalidEncodedStringException;
import org.students.simplebitcoinnode.repository.BlockRepository;
import org.students.simplebitcoinnode.repository.TransactionRepository;
import org.students.simplebitcoinnode.service.BlockBuilderService;
import org.students.simplebitcoinnode.service.PoolFinderService;
import org.students.simplebitcoinnode.util.DTOMapperWrapper;

import java.io.IOException;
import java.math.BigInteger;
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
    private final BlockRepository blockRepository;
    private final BlockchainMiningConfig blockchainMiningConfig;
    private final DTOMapperWrapper dtoMapperWrapper;
    private final PoolFinderService poolFinderService;

    public BlockBuilderCronService(ApplicationEventPublisher applicationEventPublisher,
                                   BlockBuilderService blockBuilderService,
                                   TransactionRepository transactionRepository,
                                   BlockRepository blockRepository,
                                   BlockchainMiningConfig blockchainMiningConfig,
                                   DTOMapperWrapper dtoMapperWrapper,
                                   PoolFinderService poolFinderService) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.blockBuilderService = blockBuilderService;
        this.transactionRepository = transactionRepository;
        this.blockchainMiningConfig = blockchainMiningConfig;
        this.blockRepository = blockRepository;
        this.dtoMapperWrapper = dtoMapperWrapper;
        this.poolFinderService = poolFinderService;
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
                    List<AdjacentNode> consentedNodes = poolFinderService.proposePoolMining(dtoMapperWrapper.map(block, BlockIntroductionDTO.class));
                    block.setMiners(consentedNodes.stream().map(x -> new MinerPublicKey(null, x.getPubKey())).toList());
                    logger.info("Starting pool mining with " + consentedNodes.size() + " nodes (combined hash rate: " + consentedNodes.stream().map(AdjacentNode::getAverageHashRate).reduce(0.f, Float::sum) + " MH/s)");
                    poolFinderService.initiatePoolMining(consentedNodes, block.getHash());
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
}
