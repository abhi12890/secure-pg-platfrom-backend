package com.securepg.repository;

import com.securepg.entity.ConcernStatus;
import com.securepg.entity.GuestConcern;
import com.securepg.entity.TenantProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GuestConcernRepository extends JpaRepository<GuestConcern, UUID> {

    List<GuestConcern> findByTenantOrderByCreatedAtDesc(TenantProfile tenant);

    long countByStatus(ConcernStatus status);

    @Query("SELECT c FROM GuestConcern c JOIN FETCH c.tenant t JOIN FETCH t.room ORDER BY c.createdAt DESC")
    List<GuestConcern> findAllWithTenantAndRoom();

    @Query("SELECT c FROM GuestConcern c JOIN FETCH c.tenant t JOIN FETCH t.room WHERE c.id = :id")
    Optional<GuestConcern> findByIdWithTenantAndRoom(UUID id);
}
