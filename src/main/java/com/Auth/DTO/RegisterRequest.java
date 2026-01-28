package com.Auth.DTO;
import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    private String name;
    private String publicProjectId;

}
