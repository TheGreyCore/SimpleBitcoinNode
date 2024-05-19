package org.students.simplebitcoinnode.event.listener;

import lombok.Getter;
import org.students.simplebitcoinnode.entity.Block;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Data class that represents a miner process
 */
@Getter
public class MinerProcess {
    private final List<Thread> threads;
    private final Block block;
    private final AtomicBoolean mining;

    public MinerProcess(List<Thread> threads, Block block, AtomicBoolean mining) {
        this.threads = threads;
        this.block = block;
        this.mining = mining;
    }
}
