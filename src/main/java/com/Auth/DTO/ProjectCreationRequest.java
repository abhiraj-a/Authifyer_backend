package com.Auth.DTO;

import com.Auth.Util.OAuthProvider;
import lombok.Data;

import java.util.List;

@Data
public class ProjectCreationRequest {

    private  String projectName;
    private List<OAuthProvider> providers;
    private boolean emailPasswordEnabled;

}
