package com.securepg.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "rooms", indexes = {
        @Index(name = "idx_rooms_number", columnList = "room_number", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "room_number", nullable = false, unique = true, length = 32)
    private String roomNumber;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "current_occupants_count", nullable = false)
    @Builder.Default
    private Integer currentOccupantsCount = 0;
}
