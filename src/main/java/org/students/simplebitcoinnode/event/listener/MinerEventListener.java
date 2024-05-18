package org.students.simplebitcoinnode.event.listener;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.students.simplebitcoinnode.config.BlockchainMiningConfig;
import org.students.simplebitcoinnode.event.MineBlockEvent;
import org.students.simplebitcoinnode.repository.BlockRepository;
import org.students.simplebitcoinnode.service.AsymmetricCryptographyService;
import java.math.BigInteger;

@Component
public class MinerEventListener implements ApplicationListener<MineBlockEvent> {
    private final AsymmetricCryptographyService asymmetricCryptographyService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final BlockRepository blockRepository;
    private final BlockchainMiningConfig blockchainMiningConfig;

    public MinerEventListener(AsymmetricCryptographyService asymmetricCryptographyService,
                              ApplicationEventPublisher applicationEventPublisher,
                              BlockRepository blockRepository,
                              BlockchainMiningConfig blockchainMiningConfig) {
        this.asymmetricCryptographyService = asymmetricCryptographyService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.blockRepository = blockRepository;
        this.blockchainMiningConfig = blockchainMiningConfig;
    }

    @Override
    public void onApplicationEvent(MineBlockEvent event) {
        BigInteger threadCount = BigInteger.valueOf(blockchainMiningConfig.getThreadCount());
        BigInteger currentOffset = event.getOffset();

        for (BigInteger i = BigInteger.ZERO; i.compareTo(threadCount) < 0; i = i.add(BigInteger.ONE)) {
            Thread thread = new Thread(new MineWorker(
                    asymmetricCryptographyService,
                    applicationEventPublisher,
                    blockRepository,
                    currentOffset.add(event.getStride()),
                    event.getStride().multiply(threadCount),
                    blockchainMiningConfig.getMinedBlockZeroBitCount(),
                    event.getBlock()));
            thread.start();
        }
    }
}
