package com.Auth.DTO;

import lombok.Getter;

import java.util.UUID;

@Getter
public class PasswordProjectRegisterRequest {
    private String email;
    private String password;
    private String publicProjectId;
}
