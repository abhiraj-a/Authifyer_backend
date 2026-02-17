package com.Auth.DTO;

import lombok.*;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SessionDTO {
    private String subjectId;
    private String publicSessionId;
    private Instant lastAccessedAt;
    private Instant createdAt;
    private Instant expiresAt;
    private String publicProjectId;
    private String accessToken;
    private Instant accessTokenExpiresAt;
    private UserDTO user;

    @Data
    @Builder
    public static class UserDTO{
        private String subjectId;
        private String email;
        private String name;
        private boolean emailVerified;
        private String provider;
    }

}
