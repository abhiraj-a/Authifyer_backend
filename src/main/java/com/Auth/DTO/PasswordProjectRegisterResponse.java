package com.Auth.DTO;

import lombok.Builder;

import java.time.Instant;

@Builder
public class PasswordProjectRegisterResponse {
    private String authId;
    private String email;
    private Instant createdAt;
}
