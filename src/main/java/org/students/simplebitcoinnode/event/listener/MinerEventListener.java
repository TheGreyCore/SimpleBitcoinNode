package org.students.simplebitcoinnode.event.listener;

import lombok.Getter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.students.simplebitcoinnode.config.BlockchainMiningConfig;
import org.students.simplebitcoinnode.entity.Block;
import org.students.simplebitcoinnode.event.MineBlockEvent;
import org.students.simplebitcoinnode.service.AsymmetricCryptographyService;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Class that listens for MineBlockEvents and triggers multithreaded block mining with specified parameters
 */
@Component
public class MinerEventListener implements ApplicationListener<MineBlockEvent> {
    private final Logger logger = Logger.getLogger(MinerEventListener.class.getName());

    // injected dependencies
    private final AsymmetricCryptographyService asymmetricCryptographyService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final BlockchainMiningConfig blockchainMiningConfig;

    @Getter
    private final Map<String, MinerProcess> minerProcesses = new HashMap<>();

    public MinerEventListener(AsymmetricCryptographyService asymmetricCryptographyService,
                              ApplicationEventPublisher applicationEventPublisher,
                              BlockchainMiningConfig blockchainMiningConfig) {
        this.asymmetricCryptographyService = asymmetricCryptographyService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.blockchainMiningConfig = blockchainMiningConfig;
    }

    /**
     * Event handler for MineBlockEvent that creates new worker threads that mine payload block
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(MineBlockEvent event) {
        if (alreadyPending(event.getBlock())) {
            logger.info("Skipping block " + event.getBlock().getHash() + ", reason: block is already being mined");
            return;
        }

        BigInteger threadCount = BigInteger.valueOf(blockchainMiningConfig.getThreadCount());
        BigInteger currentOffset = event.getOffset();
        AtomicBoolean continueMining = new AtomicBoolean(true);
        List<Thread> threads = new ArrayList<>();

        logger.info("Starting block '" + event.getBlock().getHash() + "' mining with " + threadCount + " threads");
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

        minerProcesses.put(event.getBlock().getHash(), new MinerProcess(threads, event.getBlock(), continueMining));
    }

    /**
     * Aborts mining and destroys miner process
     * @param blockHash specifies the hash of the original block whose process to abort
     */
    public void abortMining(String blockHash) throws InterruptedException {
        if (!minerProcesses.containsKey(blockHash))
            return;

        minerProcesses.get(blockHash).getMining().set(false);
        for (Thread thread : minerProcesses.get(blockHash).getThreads())
            thread.join();
    }

    /**
     * Scheduled task for cleaning up already mined processes
     */
    @Scheduled(cron = "*/5 * * * *")
    public void scheduledProcessCleanup() throws InterruptedException {
        for (Map.Entry<String, MinerProcess> entry : minerProcesses.entrySet()) {
            if (!entry.getValue().getMining().get()) {
                for (Thread thread : entry.getValue().getThreads())
                    thread.join();
                minerProcesses.remove(entry.getKey());
            }
        }
    }

    /**
     * Utility function that checks if block mining is already being done
     * @param block specified the block object whose task to check
     * @return true if block mining is already pending, false otherwise
     */
    private boolean alreadyPending(Block block) {
        return minerProcesses.containsKey(block.getHash());
    }
}
