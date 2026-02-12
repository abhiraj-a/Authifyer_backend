package com.Auth.Service;

import com.Auth.DTO.ProjectCreationRequest;
import com.Auth.DTO.ProjectCreationResponse;
import com.Auth.DTO.ProjectDTO;
import com.Auth.Entity.GlobalUser;
import com.Auth.Entity.Project;
import com.Auth.Entity.ProjectUser;
import com.Auth.Principal.AuthPrincipal;
import com.Auth.Repo.GlobalUserRepo;
import com.Auth.Repo.ProjectRepo;
import com.Auth.Repo.ProjectUserRepo;
import com.Auth.Util.IdGenerator;
import com.Auth.Util.OAuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class ProjectService {
    private final ProjectRepo projectRepo;
    private final GlobalUserRepo globalUserRepo;
    private final ProjectUserRepo projectUserRepo;

    public ProjectCreationResponse createProject(AuthPrincipal principal ,ProjectCreationRequest projectCreationRequest) {

        GlobalUser owner = globalUserRepo.findBySubjectId(principal.getSubjectId()).orElseThrow(RuntimeException::new);
        String publishableKey =  IdGenerator.generatePublishableKey();
        List<OAuthProvider> enableproviders = new ArrayList<>();
        if(projectCreationRequest.isEnableGithubOAuth()) enableproviders.add(OAuthProvider.GITHUB);
        if(projectCreationRequest.isEnableGoogleOAuth()) enableproviders.add(OAuthProvider.GOOGLE);

        Project p = Project.builder()
                .createdAt(Instant.now())
                .owner(owner)
                .publishableKey(publishableKey)
                .name(projectCreationRequest.getName())
                .enabledProviders(enableproviders)
                .publicProjectId(IdGenerator.generatePublicProjectId())
                .emailPassEnabled(projectCreationRequest.isEnableEmailPassword())
                .build();

        projectRepo.save(p);

        return  ProjectCreationResponse.builder()
                .projectName(projectCreationRequest.getName())
                .createdAt(Instant.now())
                .providers(enableproviders)
                .emailPasswordEnabled(projectCreationRequest.isEnableEmailPassword())
                .publishableKey(publishableKey)
                .publicProjectId(p.getPublicProjectId())
                .emailPasswordEnabled(p.isEmailPassEnabled())
                .build();
    }

    public List<ProjectDTO> loadProjects(AuthPrincipal principal) {
        GlobalUser user = globalUserRepo.findBySubjectId(principal.getSubjectId()).orElseThrow(RuntimeException::new);
        List<Project> projects = projectRepo.findAllByOwner(user);
        return projects.stream()
                .map(p->ProjectDTO.builder()
                        .publicProjectId(p.getPublicProjectId())
                        .createdAt(p.getCreatedAt())
                        .ownerSubjectId(p.getOwner().getSubjectId())
                        .githubOauthEnabled(p.getEnabledProviders().contains(OAuthProvider.GITHUB))
                        .googleOauthEnabled(p.getEnabledProviders().contains(OAuthProvider.GOOGLE))
                        .name(p.getName())
                        .emailPassEnabled(p.isEmailPassEnabled())
                        .build()).toList();
    }

    public ProjectDTO getProject(AuthPrincipal principal, String publicId) {
        GlobalUser user = globalUserRepo.findBySubjectId(principal.getSubjectId()).orElseThrow(RuntimeException::new);
        Project project =projectRepo.findByPublicProjectId(publicId).orElseThrow(RuntimeException::new);
        if(!project.getOwner().getSubjectId().equals(user.getSubjectId())) throw new RuntimeException("Invalid");

        return ProjectDTO.builder()
                .publicProjectId(project.getPublicProjectId())
                .createdAt(project.getCreatedAt())
                .publishableKey(project.getPublishableKey())
                .githubOauthEnabled(project.getEnabledProviders().contains(OAuthProvider.GITHUB))
                .googleOauthEnabled(project.getEnabledProviders().contains(OAuthProvider.GOOGLE))
                .name(project.getName())
                .emailPassEnabled(project.isEmailPassEnabled())
                .projectUsers(project.getProjectUsers().stream().map(p->
                        ProjectDTO.ProjectUserDTO.builder()
                                .email(p.getEmail())
                                .signupAt(p.getCreatedAt())
                                .name(p.getName())
                                .build()
                        ).toList()).build();

    }

    public void toggleUser(AuthPrincipal principal, String publicId, String authifyerId) {
        GlobalUser owner = globalUserRepo.findBySubjectId(principal.getSubjectId()).orElseThrow(RuntimeException::new);
        Project project = projectRepo.findByPublicProjectId(publicId).orElseThrow(RuntimeException::new);
        ProjectUser user = projectUserRepo.findByAuthifyerId(authifyerId).orElseThrow(RuntimeException::new);
        if(user.isActive()){
            user.setActive(false);
        }
    }

    public void toggleStatusViaKey(String secretKey, Map<String, String> payload) {

        Project project = projectRepo.findBySecretKeys(secretKey).orElseThrow(RuntimeException::new);
        String authifyerId = payload.get("userId");
        ProjectUser user = projectUserRepo.findByAuthifyerId(authifyerId).orElseThrow(RuntimeException::new);
        if(user.isActive()){
            user.setActive(false);
        }
    }
}
