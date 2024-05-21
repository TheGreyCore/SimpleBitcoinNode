package org.students.simplebitcoinnode.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.students.simplebitcoinnode.entity.Block;

import java.math.BigInteger;

/**
 * Event that triggers block mining on specified block
 */
@Getter
public class MineBlockEvent extends ApplicationEvent {
    private final Block block;
    private final BigInteger offset;
    private final BigInteger stride;

    public MineBlockEvent(Object src, Block block, BigInteger offset, BigInteger stride) {
        super(src);
        this.block = block;
        this.offset = offset;
        this.stride = stride;
    }
}
