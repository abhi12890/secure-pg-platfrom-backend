package com.securepg.repository;

import com.securepg.entity.OwnerNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OwnerNotificationRepository extends JpaRepository<OwnerNotification, UUID> {

    List<OwnerNotification> findTop50ByOrderByCreatedAtDesc();
}
