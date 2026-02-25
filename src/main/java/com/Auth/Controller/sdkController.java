package com.Auth.Controller;

import com.Auth.Entity.Project;
import com.Auth.Exception.ProjectNotFoundException;
import com.Auth.Repo.ProjectRepo;
import com.Auth.Util.OAuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class sdkController {

    private final ProjectRepo projectRepo;
    @GetMapping("/public/config/{publishablekey}")
    public ResponseEntity<?> projectConfig(@PathVariable String publishablekey){

        Project project =projectRepo.findByPublishableKey(publishablekey).orElseThrow(ProjectNotFoundException::new);

        return ResponseEntity.ok().body(
                Map.of(
                        "isEmailPassEnabled",project.isEmailPassEnabled(),
                        "isGoogleOauthEnabled" , project.getEnabledProviders().contains(OAuthProvider.GOOGLE),
                        "isGithubOauthEnabled" , project.getEnabledProviders().contains(OAuthProvider.GITHUB)
                )
        );
    }

}
