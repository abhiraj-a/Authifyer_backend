package com.Auth.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Entity
@Builder
@Data
public class RefreshToken {

    @Id
    @GeneratedValue
    private UUID id;

    private String tokenHash;

    private UUID sessionId;

    private UUID projectUserId;

    private Instant expiresAt;

    private Instant revokedAt;

}
