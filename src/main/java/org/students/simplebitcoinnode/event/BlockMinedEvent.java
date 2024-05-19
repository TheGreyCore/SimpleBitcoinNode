package org.students.simplebitcoinnode.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.students.simplebitcoinnode.entity.Block;

/**
 * Event that signals that the block has been mined
 */
@Getter
public class BlockMinedEvent extends ApplicationEvent {
    private final Block block;

    public BlockMinedEvent(Object src, Block block) {
        super(src);
        this.block = block;
    }
}
