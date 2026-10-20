package com.securepg.repository;

import com.securepg.entity.TenantProfile;
import com.securepg.entity.TenantStatus;
import com.securepg.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TenantProfileRepository extends JpaRepository<TenantProfile, UUID> {

    Optional<TenantProfile> findByUser(User user);

    Optional<TenantProfile> findByUserId(UUID userId);

    @Query("SELECT t FROM TenantProfile t JOIN FETCH t.room JOIN FETCH t.user WHERE t.id = :id")
    Optional<TenantProfile> findByIdWithRoomAndUser(UUID id);

    List<TenantProfile> findByStatus(TenantStatus status);

    @Query("SELECT t FROM TenantProfile t JOIN FETCH t.room JOIN FETCH t.user WHERE t.status = :status")
    List<TenantProfile> findByStatusWithRoom(TenantStatus status);

    @Query("SELECT t FROM TenantProfile t JOIN FETCH t.room JOIN FETCH t.user")
    List<TenantProfile> findAllWithRoom();
}
