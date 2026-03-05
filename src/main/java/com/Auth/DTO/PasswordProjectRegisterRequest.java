package com.Auth.DTO;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;

@Getter
public class PasswordProjectRegisterRequest {
    private String email;
    private String password;
    private String name;
    @JsonAlias({"publicProjectId", "publishableKey"})
    private String publishableKey;
}
