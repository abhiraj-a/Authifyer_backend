package com.Auth.DTO;

import lombok.Getter;

@Getter
public class ProjectUpdateRequest {
    private String name;
    private boolean enableEmailPassword;
    private boolean enableGoogleOAuth;
    private boolean enableGithubOAuth;
}
