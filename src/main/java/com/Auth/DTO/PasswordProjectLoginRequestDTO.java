package com.Auth.DTO;

import lombok.Getter;

@Getter
public class PasswordProjectLoginRequestDTO {

    private String email;
    private String password;
    private String publishableKey;
}
