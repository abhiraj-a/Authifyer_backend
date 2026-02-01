package com.Auth.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JWTKey {
    @Id
    private String kid;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String publicKeyPem;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String privateKeyPem;

    private boolean isActive;

    private Instant createdAt;
}
