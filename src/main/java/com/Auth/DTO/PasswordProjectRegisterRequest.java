package com.Auth.DTO;

import lombok.Getter;

@Getter
public class PasswordProjectRegisterRequest {
    private String email;
    private String password;
    private String publishableKey;
}
