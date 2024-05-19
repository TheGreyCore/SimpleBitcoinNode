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
import org.students.simplebitcoinnode.entity.Block;
import org.students.simplebitcoinnode.entity.MerkleTreeNode;
import org.students.simplebitcoinnode.entity.MinerPublicKey;
import org.students.simplebitcoinnode.event.MineBlockEvent;
import org.students.simplebitcoinnode.event.listener.MinerEventListener;
import org.students.simplebitcoinnode.service.AsymmetricCryptographyService;
import org.students.simplebitcoinnode.util.Encoding;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class MinerEventListenerTest {
    // injected dependencies
    private final AsymmetricCryptographyService asymmetricCryptographyService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final TestEventListener testEventListener;

    @Mock
    private BlockchainMiningConfig blockchainMiningConfig;

    @Autowired
    public MinerEventListenerTest(AsymmetricCryptographyService asymmetricCryptographyService, ApplicationEventPublisher applicationEventPublisher, TestEventListener testEventListener) {
        this.asymmetricCryptographyService = asymmetricCryptographyService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.testEventListener = testEventListener;
    }

    private Block buildTestBlock() throws Exception {
        Block block = Block.builder()
                .id(1L)
                .previousHash("0".repeat(64))
                .miners(List.of(new MinerPublicKey(null, "PZ8Tyr4Nx8MHsRAGMpZmZ6TWY63dXWSCxtEZTC9qqAWU95UpXARRnKYMb35cEDFjPVuBfffRRdKWe7BMRsmimYh2sqHvYekcQkbZbhKhSqqyfW4rLJXCstNP"),
                                new MinerPublicKey(null, "PZ8Tyr4Nx8MHsRAGMpZmZ6TWY63dXWSCxJZoDfW2gJemgZMuUY6hH29EtoKHKwqKMqxCnDwkzn6TXUQXXr7AKm7io7DvTL1w7AHsmgSqTL9phCgM68GpFC1J"),
                                new MinerPublicKey(null, "PZ8Tyr4Nx8MHsRAGMpZmZ6TWY63dXWSCxBN72CnVs5C3FbQ2HKfq6c7QtU757xWoEjR7Bvpbn8XeKPogAi8qqXWPrHS6RsFQh7wgwcWqoqp5Yi2AH4N5Vuqr")))
                .blockAssemblyTimestamp(LocalDateTime.now(ZoneId.of("UTC")))
                .nonce(BigInteger.ZERO)
                .merkleTree(MerkleTreeNode.builder().hash("1".repeat(64)).build())
                .build();

        block.setHash(Encoding.toHexString(asymmetricCryptographyService.digestObject(block)));
        return block;
    }

    @Test
    @DisplayName("Test that block mining works as intended")
    public void testBlockMining() throws Exception {
        Block testBlock = buildTestBlock();
        final long zeroBits = 20;
        final int threadCount = 12;

        given(blockchainMiningConfig.getMinedBlockZeroBitCount())
                .willReturn(zeroBits);
        given(blockchainMiningConfig.getThreadCount())
                .willReturn(threadCount);

        MinerEventListener listener = new MinerEventListener(
                asymmetricCryptographyService,
                applicationEventPublisher,
                blockchainMiningConfig);

        MineBlockEvent event = new MineBlockEvent(this, testBlock, BigInteger.ZERO, BigInteger.ONE);
        listener.onApplicationEvent(event);
        List<Thread> threads = listener.getThreads();

        for (Thread thread : threads)
            thread.join();

        // ensure that there is only one event for given block
        assertEquals(1, testEventListener.getEvents().size());

        // ensure that block hash matches the calculated hash
        byte[] calculatedHash = asymmetricCryptographyService.digestObject(testEventListener.getEvents().getFirst().getBlock());
        assertEquals(testEventListener.getEvents().getFirst().getBlock().getHash(), Encoding.toHexString(calculatedHash));

        // ensure that hash prefix contains N zero bits
        long prefixVal = ((calculatedHash[7] & 0xFFL) << 56) |
                ((calculatedHash[6] & 0xFFL) << 48) |
                ((calculatedHash[5] & 0xFFL) << 40) |
                ((calculatedHash[4] & 0xFFL) << 32) |
                ((calculatedHash[3] & 0xFFL) << 24) |
                ((calculatedHash[2] & 0xFFL) << 16) |
                ((calculatedHash[1] & 0xFFL) << 8) |
                ((calculatedHash[0] & 0xFFL));
        assertEquals(0, prefixVal & ((1L << zeroBits)));
    }
}
