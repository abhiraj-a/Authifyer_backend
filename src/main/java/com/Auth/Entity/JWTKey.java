package com.Auth.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Entity
@Builder
@Data
public class JWTKey {
    @Id
    private String kid;

    @Lob
    private String publicKeyPem;
    @Lob
    private String privateKeyPem;

    private boolean isActive;

    private Instant createdAt;
}
