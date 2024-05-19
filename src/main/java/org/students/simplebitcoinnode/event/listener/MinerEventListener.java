package org.students.simplebitcoinnode.event.listener;

import lombok.Getter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.students.simplebitcoinnode.config.BlockchainMiningConfig;
import org.students.simplebitcoinnode.entity.Block;
import org.students.simplebitcoinnode.event.MineBlockEvent;
import org.students.simplebitcoinnode.repository.BlockRepository;
import org.students.simplebitcoinnode.service.AsymmetricCryptographyService;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class MinerEventListener {
    private final AsymmetricCryptographyService asymmetricCryptographyService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final BlockchainMiningConfig blockchainMiningConfig;

    @Getter
    private List<Thread> threads = new ArrayList<>();

    public MinerEventListener(AsymmetricCryptographyService asymmetricCryptographyService,
                              ApplicationEventPublisher applicationEventPublisher,
                              BlockchainMiningConfig blockchainMiningConfig) {
        this.asymmetricCryptographyService = asymmetricCryptographyService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.blockchainMiningConfig = blockchainMiningConfig;
    }

    @EventListener
    public void onApplicationEvent(MineBlockEvent event) {
        BigInteger threadCount = BigInteger.valueOf(blockchainMiningConfig.getThreadCount());
        BigInteger currentOffset = event.getOffset();
        AtomicBoolean continueMining = new AtomicBoolean(true);

        for (BigInteger i = BigInteger.ZERO; i.compareTo(threadCount) < 0; i = i.add(BigInteger.ONE)) {
            MineWorker worker = new MineWorker(
                asymmetricCryptographyService,
                applicationEventPublisher,
                currentOffset.add(BigInteger.ZERO),
                event.getStride().multiply(threadCount),
                blockchainMiningConfig.getMinedBlockZeroBitCount(),
                (Block)event.getBlock().clone(),
                continueMining);

            Thread thread = new Thread(worker);
            currentOffset = currentOffset.add(event.getStride());
            thread.start();
            threads.add(thread);
        }
    }
}
