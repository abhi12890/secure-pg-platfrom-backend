package com.securepg.service;

import com.securepg.entity.OwnerNotification;
import com.securepg.repository.OwnerNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final OwnerNotificationRepository ownerNotificationRepository;
    private final OwnerNotificationStreamService ownerNotificationStreamService;

    @Transactional
    public OwnerNotification notifyOwner(String message, String eventType) {
        OwnerNotification notification = OwnerNotification.builder()
                .message(message)
                .eventType(eventType)
                .build();
        OwnerNotification saved = ownerNotificationRepository.save(notification);
        ownerNotificationStreamService.publish(saved);
        return saved;
    }
}
