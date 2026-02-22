package com.Auth.DTO;

import lombok.Getter;

@Getter
public class VerifyEmailRequest {
    private String token;
    private String subjectId;
}
