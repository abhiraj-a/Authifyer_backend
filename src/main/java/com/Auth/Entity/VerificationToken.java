package com.Auth.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class VerificationToken {

    @Id
    @GeneratedValue
    private UUID id;

    // OPTIONAL for global users
    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(
            name = "project_user_id",
            nullable = true
    )
    private ProjectUser projectUser;

    @Column(nullable = false)
    private String subjectId;

    private String verificationToken;

    private Instant expiresAt;
}
