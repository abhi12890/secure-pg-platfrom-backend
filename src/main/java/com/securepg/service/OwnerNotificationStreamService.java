package com.securepg.service;

import com.securepg.dto.OwnerNotificationDto;
import com.securepg.entity.OwnerNotification;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

/** Authenticated in-process SSE feed for owner dashboard notifications. */
@Service
public class OwnerNotificationStreamService {

    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        try {
            emitter.send(SseEmitter.event().name("connected").data("ready"));
        } catch (IOException ex) {
            emitters.remove(emitter);
            emitter.completeWithError(ex);
        }
        return emitter;
    }

    public void publish(OwnerNotification notification) {
        OwnerNotificationDto payload = OwnerNotificationDto.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .eventType(notification.getEventType())
                .readFlag(notification.getReadFlag())
                .createdAt(notification.getCreatedAt())
                .build();

        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event().name("notification").data(payload));
            } catch (IOException ex) {
                emitters.remove(emitter);
                emitter.complete();
            }
        });
    }
}