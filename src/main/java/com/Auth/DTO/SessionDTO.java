package com.Auth.DTO;

import com.Auth.JWT.AccessTokenClaims;
import lombok.Builder;

import java.time.Instant;

@Builder
public class SessionDTO {
    private String subjectId;
    private String publicSessionId;
    private Instant lastAccessedAt;
    private Instant createdAt;
    private Instant expiresAt;
    private String publicProjectId;
    private AccessTokenClaims accessTokenClaims;
}
