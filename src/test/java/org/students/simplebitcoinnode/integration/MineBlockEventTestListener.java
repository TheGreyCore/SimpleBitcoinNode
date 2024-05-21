package org.students.simplebitcoinnode.integration;

import lombok.Getter;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.students.simplebitcoinnode.event.MineBlockEvent;

import java.util.ArrayList;
import java.util.List;

@Component
@Getter
public class MineBlockEventTestListener {
    private final List<MineBlockEvent> events = new ArrayList<>();

    @EventListener
    public void onEvent(MineBlockEvent event) {
        events.add(event);
    }

    public void reset() { events.clear(); }
}
