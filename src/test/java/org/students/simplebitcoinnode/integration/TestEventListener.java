package org.students.simplebitcoinnode.integration;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.students.simplebitcoinnode.event.BlockMinedEvent;

import java.util.ArrayList;
import java.util.List;

@Component
@Getter
public class TestEventListener {
    private final List<BlockMinedEvent> events = new ArrayList<>();

    @EventListener
    void onEvent(BlockMinedEvent event) {
        synchronized (events) {
            events.add(event);
        }
    }

    public void reset() {
        events.clear();
    }
}
