package org.students.simplebitcoinnode.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event that signals that the block has been mined
 */
@Getter
public class BlockMinedEvent extends ApplicationEvent {
    private final Long id;

    public BlockMinedEvent(Object source, Long id) {
        super(source);
        this.id = id;
    }
}
