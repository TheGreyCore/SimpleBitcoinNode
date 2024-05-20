package org.students.simplebitcoinnode.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.students.simplebitcoinnode.config.BlockchainMiningConfig;
import org.students.simplebitcoinnode.dto.BlockIntroductionDTO;
import org.students.simplebitcoinnode.dto.PoolInitiationBlockMetadataDTO;
import org.students.simplebitcoinnode.dto.PoolMiningProposalDTO;
import org.students.simplebitcoinnode.entity.AdjacentNode;
import org.students.simplebitcoinnode.repository.AdjacentNodeRepository;
import org.students.simplebitcoinnode.service.PoolFinderService;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class PoolFinderServiceImpl implements PoolFinderService {
    private final Logger logger = Logger.getLogger(PoolFinderService.class.getName());
    private final AdjacentNodeRepository adjacentNodeRepository;
    private final BlockchainMiningConfig blockchainMiningConfig;
    private final HttpClient httpClient;

    public PoolFinderServiceImpl(AdjacentNodeRepository adjacentNodeRepository, BlockchainMiningConfig blockchainMiningConfig, HttpClient httpClient) {
        this.adjacentNodeRepository = adjacentNodeRepository;
        this.blockchainMiningConfig = blockchainMiningConfig;
        this.httpClient = httpClient;
    }

    @Override
    public List<AdjacentNode> proposePoolMining(BlockIntroductionDTO blockIntroductionDTO) {
        List<AdjacentNode> consentedNodes = new ArrayList<>();
        List<AdjacentNode> adjacentNodes = adjacentNodeRepository.findBestNodesByHashRate(Float.MAX_VALUE, blockchainMiningConfig.getPool().getMaximumPoolRequests());

        String json = objectToJson(new PoolMiningProposalDTO(blockIntroductionDTO, blockchainMiningConfig.getPool().getMaximumPoolRequests()));
        while (!adjacentNodes.isEmpty() && consentedNodes.size() < blockchainMiningConfig.getPool().getMaximumPoolRequests()) {
            for (AdjacentNode adjacentNode : adjacentNodes) {
                String baseURL = (adjacentNode.isTls() ? "https://" : "http://") + adjacentNode.getHostname() + "/blockchain/mine/propose";
                logger.info("Trying to make a proposal mining request to " + baseURL);

                try {
                    HttpResponse<String> response = httpClient.send(buildJsonPostRequest(new URI(baseURL), json), HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() != 200) {
                        logger.warning("Could not make a proposal mining request to " + baseURL + " invalid status code: " + response.statusCode());
                        continue;
                    }

                    consentedNodes.add(adjacentNode);
                } catch (Exception e) {
                    logger.warning("An exception has occurred while making a proposal mining request: " + e.getMessage());
                }
            }
            adjacentNodes = adjacentNodeRepository.findBestNodesByHashRate(adjacentNodes.getLast().getAverageHashRate(), blockchainMiningConfig.getPool().getMaximumPoolRequests());
        }

        return consentedNodes;
    }

    @Override
    public void initiatePoolMining(List<AdjacentNode> poolNodes, String blockHash) throws IOException {
        PoolInitiationBlockMetadataDTO poolInitiationBlockMetadataDTO = PoolInitiationBlockMetadataDTO.builder()
                .miners(poolNodes.stream().map(AdjacentNode::getPubKey).toList())
                .hash(blockHash).build();

        for (int i = 0; i < poolNodes.size(); i++) {
            String baseURL = (poolNodes.get(i).isTls() ? "https://" : "http://") + poolNodes.get(i).getHostname() + "/blockchain/mine/initiate";
            poolInitiationBlockMetadataDTO.setOffset(i);
            String json = objectToJson(poolInitiationBlockMetadataDTO);
            try {
                HttpResponse<Void> response = httpClient.send(buildJsonPostRequest(new URI(baseURL), json), HttpResponse.BodyHandlers.discarding());
                if (response.statusCode() != 200)
                    throw new IOException("Invalid status code " + response.statusCode() + " while initiating pool request");
            }
            catch (URISyntaxException | InterruptedException e) {
                throw new IOException(e);
            }
        }
    }

    /**
     * Utility function for converting the object into json string
     * @param object specifies the object to use for conversion
     * @return json string specifying the serialized object
     * @param <T> generic argument for the object type
     */
    private <T> String objectToJson(T object) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(object);
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper function for building a JSON POST request
     * @param uri specifies the destination URI
     * @param json specifies the json body to use in request
     * @return HttpRequest object
     */
    private HttpRequest buildJsonPostRequest(URI uri, String json) {
        return HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .header("User-Agent", "SimpleBitcoinNode v1.0")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
    }
}
