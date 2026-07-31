package com.dbtraining.reconx.service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.dbtraining.reconx.dto.TradeResponse;

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


// For now, SSE does not work well with Spring Boot 3.0.5
// @Component
public class TradeStreamBroadcaster {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(ex -> emitters.remove(emitter));
        try {
            // Without this, the HTTP response isn't committed until the first
            // real trade is broadcast — the browser's EventSource never fires
            // onopen, so the UI shows "disconnected" forever if nothing is
            // being created. A no-op comment forces headers out immediately.
            emitter.send(SseEmitter.event().comment("connected"));
        } catch (Exception e) {
            emitters.remove(emitter);
        }
        return emitter;
    }

    public void broadcast(TradeResponse trade) {
        // run separately to avoid blocking the thread that created the trade
        executor.execute(() -> {
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event().data(trade));
                } catch (Exception e) {
                    emitters.remove(emitter);
                }
            }
        });
    }
}
