package com.Auth.DTO;

import lombok.Getter;

import java.util.UUID;

@Getter
public class PasswordProjectLoginRequestDTO {

    private String email;
    private String password;
    private String publicProjectId;
}
