package com.Auth.DTO;

import com.Auth.Util.OAuthProvider;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@Getter
public class ProjectCreationRequest {
//
//    private  String projectName;
//    private List<OAuthProvider> enableProvider;
//    private boolean emailPasswordEnabled;


    private String name;

    private boolean enableEmailPassword;
    private boolean enableGoogleOAuth;
    private boolean enableGithubOAuth;
}
