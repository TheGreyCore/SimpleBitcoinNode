package org.students.simplebitcoinnode.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.students.simplebitcoinnode.config.BlockchainMiningConfig;
import org.students.simplebitcoinnode.entity.AdjacentNode;
import org.students.simplebitcoinnode.entity.Block;
import org.students.simplebitcoinnode.entity.MinerPublicKey;
import org.students.simplebitcoinnode.entity.Transaction;
import org.students.simplebitcoinnode.repository.BlockRepository;
import org.students.simplebitcoinnode.repository.TransactionRepository;
import org.students.simplebitcoinnode.service.BlockBuilderService;
import org.students.simplebitcoinnode.service.PoolFinderService;
import org.students.simplebitcoinnode.service.cron.BlockBuilderCronService;
import org.students.simplebitcoinnode.util.DTOMapperWrapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class BlockBuilderCronServiceTests {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final BlockBuilderService blockBuilderService;
    private final DTOMapperWrapper dtoMapperWrapper;
    private final MineBlockEventTestListener mineBlockEventTestListener;

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private BlockchainMiningConfig blockchainMiningConfig;
    @Mock
    private BlockRepository blockRepository;
    @Mock
    private PoolFinderService poolFinderService;

    @Autowired
    public BlockBuilderCronServiceTests(ApplicationEventPublisher applicationEventPublisher,
                                        BlockBuilderService blockBuilderService,
                                        DTOMapperWrapper dtoMapperWrapper,
                                        MineBlockEventTestListener mineBlockEventTestListener) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.blockBuilderService = blockBuilderService;
        this.dtoMapperWrapper = dtoMapperWrapper;
        this.mineBlockEventTestListener = mineBlockEventTestListener;
    }

    @Test
    @DisplayName("Ensure that MineBlockEvent gets published with valid payload")
    public void testScheduledBlockBuildingAndMining_EnsureThatMineBlockEventGetsPublishedWithValidPayload() throws Exception {
        List<Transaction> testTransactions = List.of(
                Transaction.builder().transactionHash("4355a46b19d348dc2f57c046f8ef63d4538ebb936000f3c9ee954a27460dd865").build(),
                Transaction.builder().transactionHash("53c234e5e8472b6ac51c1ae1cab3fe06fad053beb8ebfd8977b010655bfdd3c3").build(),
                Transaction.builder().transactionHash("1121cfccd5913f0a63fec40a6ffd44ea64f9dc135c66634ba001d10bcf4302a2").build());

        List<AdjacentNode> testAdjacentNodes = List.of(
                AdjacentNode.builder().ip("227.77.196.233").pubKey("1").name("Trusted Coin Market Inc").port(443).tls(true).hostname("trusted.node").averageHashRate(2891.f).build(),
                AdjacentNode.builder().ip("38.68.198.190").pubKey("2").name("Basement Miner").port(80).tls(false).hostname("1337h2x.tk").averageHashRate(1337.f).build(),
                AdjacentNode.builder().ip("167.3.36.139").pubKey("3").name("Random Mine").port(443).tls(true).hostname("random.ru").averageHashRate(666.f).build());

        Block testBlock = Block.builder().hash("7de1555df0c2700329e815b93b32c571c3ea54dc967b89e81ab73b9972b72d1d").build();

        given(blockchainMiningConfig.getTransactionsPerBlock())
                .willReturn(testTransactions.size());
        given(transactionRepository.findAmountOfUnverifiedTransactions())
                .willReturn((long)testTransactions.size());
        given(transactionRepository.findUnverifiedTransactionsLimitByN(blockchainMiningConfig.getTransactionsPerBlock()))
                .willReturn(testTransactions);
        given(blockRepository.findBlockWithLongestChain())
                .willReturn(testBlock);
        given(poolFinderService.proposePoolMining(any()))
                .willReturn(testAdjacentNodes);


        BlockBuilderCronService blockBuilderCronService = new BlockBuilderCronService(
                applicationEventPublisher,
                blockBuilderService,
                transactionRepository,
                blockRepository,
                blockchainMiningConfig,
                dtoMapperWrapper,
                poolFinderService);
        blockBuilderCronService.scheduledBlockBuildingAndMining();

        verify(poolFinderService, times(1)).initiatePoolMining(any(), any());
        assertEquals(1, mineBlockEventTestListener.getEvents().size());
        testAdjacentNodes.forEach(x -> assertTrue(mineBlockEventTestListener.getEvents().stream().map(e -> e.getBlock().getMiners().stream().map(MinerPublicKey::getPubKey).toList()).allMatch(m -> m.contains(x.getPubKey()))));
    }
}
