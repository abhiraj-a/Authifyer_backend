package com.Auth.JWT;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Builder
@Getter
public class AccessTokenClaims {
    private String accessToken;
    private Instant issued_at;
    private Instant  expires_at;
}
