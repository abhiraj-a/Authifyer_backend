package com.Auth.Entity;

import com.Auth.Util.OAuthProvider;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuthStorage {
    @Id
    @GeneratedValue
    private UUID id;

    private String provider;

    private String email;

    private String providerId;

    private Instant createdAt;

    private String subjectId;
}
