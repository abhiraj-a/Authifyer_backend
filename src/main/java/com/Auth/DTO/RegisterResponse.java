package com.Auth.DTO;
import lombok.Builder;

import java.time.Instant;

@Builder
public class RegisterResponse {
    private Instant createdAt;
    private String email;
    private String subjectId;
}
