package com.Auth.Entity;

import com.Auth.Util.SessionScope;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Session {

    @Id
    @GeneratedValue
    private UUID id;

    private String publicId;

    private String publicProjectId;

    private String publishableKey;

    private String IpAddress;

    private String userAgent;

    private String deviceName;

    private Instant createdAt;

    private Instant lastAccessedAt;

    private Instant revokedAt;

    private String subjectId;

    private String tokenHash;

    private String sessionScope;

}
