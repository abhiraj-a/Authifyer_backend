package com.Auth.DTO;

import com.Auth.Util.OAuthProvider;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public class ProjectCreationResponse {

    private  String projectName;
    private List<OAuthProvider> providers;
    private boolean emailPasswordEnabled;
    private Instant createdAt;
    private String publishableKey;
}
