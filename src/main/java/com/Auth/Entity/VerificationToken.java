package com.Auth.Entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Builder
@Data
public class VerificationToken {

    @Id
    @GeneratedValue
    private UUID id;

    private ProjectUser projectUser;

    private String subjectId;

    private String verificationToken;

    private Instant expiresAt;
}
