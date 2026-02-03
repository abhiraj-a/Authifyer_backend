package com.Auth.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SessionDTO {
    private String subjectId;
    private String publicSessionId;
    private Instant lastAccessedAt;
    private Instant createdAt;
    private Instant expiresAt;
    private String publicProjectId;
    private String accessToken;
    private Instant accessTokenExpiresAt;
}
