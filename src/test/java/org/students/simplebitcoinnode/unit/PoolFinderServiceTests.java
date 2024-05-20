package org.students.simplebitcoinnode.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.students.simplebitcoinnode.config.BlockchainMiningConfig;
import org.students.simplebitcoinnode.config.BlockchainMiningPoolConfig;
import org.students.simplebitcoinnode.dto.BlockIntroductionDTO;
import org.students.simplebitcoinnode.dto.PoolInitiationBlockMetadataDTO;
import org.students.simplebitcoinnode.entity.AdjacentNode;
import org.students.simplebitcoinnode.repository.AdjacentNodeRepository;
import org.students.simplebitcoinnode.service.PoolFinderService;
import org.students.simplebitcoinnode.service.impl.PoolFinderServiceImpl;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PoolFinderServiceTests {
    @Mock
    private AdjacentNodeRepository adjacentNodeRepository;

    @Mock
    private BlockchainMiningConfig blockchainMiningConfig;

    @Mock
    private BlockchainMiningPoolConfig blockchainMiningPoolConfig;

    @Mock
    private HttpResponse mockBadResponse;

    @Mock
    private HttpResponse mockGoodResponse;

    @Mock
    private HttpClient httpClient;

    private <T> HttpRequest makeJsonPostRequest(AdjacentNode adjacentNode, T object, String endpoint) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(object);
        String baseURL = (adjacentNode.isTls() ? "https://" : "http://") + adjacentNode.getHostname() + endpoint;
        return HttpRequest.newBuilder()
                .uri(new URI(baseURL))
                .header("Content-Type", "application/json")
                .header("User-Agent", "SimpleBitcoinNode v1.0")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
    }

    private List<AdjacentNode> makeTestAdjacentNodes() {
        return List.of(
                AdjacentNode.builder().ip("227.77.196.233").name("Trusted Coin Market Inc").port(443).tls(true).hostname("trusted.node").averageHashRate(2891.f).build(),
                AdjacentNode.builder().ip("38.68.198.190").name("Basement Miner").port(80).tls(false).hostname("1337h2x.tk").averageHashRate(1337.f).build(),
                AdjacentNode.builder().ip("167.3.36.139").name("Random Mine").port(443).tls(true).hostname("random.ru").averageHashRate(666.f).build()
        );
    }

    @Test
    @DisplayName("Ensure that pool proposal maker is able to filter nodes that accept the proposal")
    public void testProposePoolMining_EnsureThatPoolProposalMakerIsAbleToFilterNodesThatAcceptTheProposal() throws Exception {
        List<AdjacentNode> adjacentNodes = makeTestAdjacentNodes();

        BlockIntroductionDTO blockIntroductionDTO = new BlockIntroductionDTO();

        given(blockchainMiningConfig.getPool())
                .willReturn(blockchainMiningPoolConfig);
        given(blockchainMiningConfig.getPool().getMaximumPoolRequests())
                .willReturn(2);
        given(adjacentNodeRepository.findBestNodesByHashRate(Float.MAX_VALUE, blockchainMiningConfig.getPool().getMaximumPoolRequests()))
                .willReturn(adjacentNodes.subList(0, 2));
        given(adjacentNodeRepository.findBestNodesByHashRate(1337.f, blockchainMiningConfig.getPool().getMaximumPoolRequests()))
                .willReturn(adjacentNodes.subList(2, adjacentNodes.size()));
        given(mockGoodResponse.statusCode())
                .willReturn(200);
        given(mockBadResponse.statusCode())
                .willReturn(400);
        given(httpClient.send(any(), any()))
                .willReturn(mockGoodResponse);
        given(httpClient.send(makeJsonPostRequest(adjacentNodes.get(1), blockIntroductionDTO, "/blockchain/mine/propose"), HttpResponse.BodyHandlers.ofString()))
                .willReturn(mockBadResponse);

        PoolFinderService poolFinderService = new PoolFinderServiceImpl(adjacentNodeRepository, blockchainMiningConfig, httpClient);
        List<AdjacentNode> consentedNodes = poolFinderService.proposePoolMining(blockIntroductionDTO);
        assertEquals(blockchainMiningConfig.getPool().getMaximumPoolRequests(), consentedNodes.size());
        assertEquals(adjacentNodes.getFirst().getIp(), consentedNodes.getFirst().getIp());
        assertEquals(adjacentNodes.get(2).getIp(), consentedNodes.get(1).getIp());
    }

    @Test
    @DisplayName("Ensure that pool mining initiation does not throw when adjacent nodes return 200 status code")
    public void testInitiatePoolMining_ValidStatusCode_ExpectDoesNotThrow() throws Exception {
        List<AdjacentNode> adjacentNodes = makeTestAdjacentNodes();
        final String blockHash = "1".repeat(64);

        given(mockGoodResponse.statusCode())
                .willReturn(200);
        given(httpClient.send(any(), any()))
            .willReturn(mockGoodResponse);

        PoolFinderService poolFinderService = new PoolFinderServiceImpl(adjacentNodeRepository, blockchainMiningConfig, httpClient);
        assertDoesNotThrow(() -> poolFinderService.initiatePoolMining(adjacentNodes, blockHash));
    }

    @Test
    @DisplayName("Ensure that pool mining initiation throws IOException when adjacent node returns non-200 status code")
    public void testInitiatePoolMining_ThrowsIOException_ExpectDoesNotThrow() throws Exception {
        List<AdjacentNode> adjacentNodes = makeTestAdjacentNodes();
        final String blockHash = "1".repeat(64);
        PoolInitiationBlockMetadataDTO poolInitiationBlockMetadataDTO = PoolInitiationBlockMetadataDTO.builder()
                .miners(adjacentNodes.stream().map(AdjacentNode::getPubKey).toList())
                .hash(blockHash).build();

        given(mockGoodResponse.statusCode())
                .willReturn(200);
        given(mockBadResponse.statusCode())
                .willReturn(404);
        given(httpClient.send(makeJsonPostRequest(adjacentNodes.getFirst(), poolInitiationBlockMetadataDTO, "/blockchain/mine/initiate"), HttpResponse.BodyHandlers.discarding()))
                .willReturn(mockGoodResponse);
        given(httpClient.send(makeJsonPostRequest(adjacentNodes.get(1), poolInitiationBlockMetadataDTO, "/blockchain/mine/initiate"), HttpResponse.BodyHandlers.discarding()))
                .willReturn(mockGoodResponse);
        given(httpClient.send(makeJsonPostRequest(adjacentNodes.get(2), poolInitiationBlockMetadataDTO, "/blockchain/mine/initiate"), HttpResponse.BodyHandlers.discarding()))
                .willReturn(mockBadResponse);

        PoolFinderService poolFinderService = new PoolFinderServiceImpl(adjacentNodeRepository, blockchainMiningConfig, httpClient);
        assertThrows(IOException.class, () -> poolFinderService.initiatePoolMining(adjacentNodes, blockHash));
    }
}
