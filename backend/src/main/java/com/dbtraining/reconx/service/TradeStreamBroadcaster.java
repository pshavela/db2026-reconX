package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.TradeResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Temporary, in-process substitute for the Kafka-based trade-events pipeline
 * (TICKET-ADV128/ADV129, Day 9 — neither is implemented yet: the topic isn't
 * declared and TradeEventProducer.publish() still throws
 * UnsupportedOperationException). Broadcasts newly created trades directly
 * to any open SSE connection, bypassing Kafka entirely.
 *
 * Not tied to a real ticket. Meant to be replaced once trade-events + a
 * dedicated consumer exist — see tracking-progress/day08/NOTE-sse-temporary-bridge.md.
 */
@Component
public class TradeStreamBroadcaster {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(ex -> emitters.remove(emitter));
        return emitter;
    }

    public void broadcast(TradeResponse trade) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().data(trade));
            } catch (IOException | IllegalStateException ex) {
                emitters.remove(emitter);
            }
        }
    }
}
