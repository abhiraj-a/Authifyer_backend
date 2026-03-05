package com.Auth.DTO;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;

@Getter
public class PasswordProjectLoginRequestDTO {

    private String email;
    private String password;
    @JsonAlias({"publicProjectId", "publishableKey"})
    private String publishableKey;
}
