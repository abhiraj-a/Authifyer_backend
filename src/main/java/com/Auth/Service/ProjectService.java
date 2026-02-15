package com.Auth.Service;
import com.Auth.DTO.ProjectCreationRequest;
import com.Auth.DTO.ProjectCreationResponse;
import com.Auth.DTO.ProjectDTO;
import com.Auth.Entity.GlobalUser;
import com.Auth.Entity.Project;
import com.Auth.Entity.ProjectUser;
import com.Auth.Entity.Session;
import com.Auth.Exception.OwnerMismatchException;
import com.Auth.Exception.ProjectNotFoundException;
import com.Auth.Exception.UserNotFoundException;
import com.Auth.Principal.AuthPrincipal;
import com.Auth.Repo.GlobalUserRepo;
import com.Auth.Repo.ProjectRepo;
import com.Auth.Repo.ProjectUserRepo;
import com.Auth.Repo.SessionRepo;
import com.Auth.Util.IdGenerator;
import com.Auth.Util.OAuthProvider;
import com.Auth.Util.TokenHash;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProjectService {
    private final ProjectRepo projectRepo;
    private final GlobalUserRepo globalUserRepo;
    private final ProjectUserRepo projectUserRepo;
    private final SessionRepo sessionRepo;
    private final TokenHash tokenHash;

    @Transactional
    public ProjectCreationResponse createProject(AuthPrincipal principal ,ProjectCreationRequest projectCreationRequest) {

        GlobalUser owner = globalUserRepo.findBySubjectId(principal.getSubjectId()).orElseThrow(RuntimeException::new);
        String publishableKey =  IdGenerator.generatePublishableKey();
        List<OAuthProvider> enableproviders = new ArrayList<>();
        if(projectCreationRequest.isEnableGithubOAuth()) enableproviders.add(OAuthProvider.GITHUB);
        if(projectCreationRequest.isEnableGoogleOAuth()) enableproviders.add(OAuthProvider.GOOGLE);

        String secretKey = IdGenerator.generateSecretKey();
        Project p = Project.builder()
                .createdAt(Instant.now())
                .owner(owner)
                .publishableKey(publishableKey)
                .name(projectCreationRequest.getName())
                .enabledProviders(enableproviders)
                .publicProjectId(IdGenerator.generatePublicProjectId())
                .emailPassEnabled(projectCreationRequest.isEnableEmailPassword())
                .secretKeys(Collections.singletonList(tokenHash.hash(secretKey)))
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
                .secretKeys(List.of(secretKey))
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

        if(!project.getOwner().getSubjectId().equals(user.getSubjectId())) {
            log.warn("owner mismatch error");
            throw new OwnerMismatchException();
        }

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
                                .authifyerId(p.getAuthifyerId())
                                .isActive(p.isActive())
                                .build()
                        ).toList()).build();

    }

    public void toggleUser(AuthPrincipal principal, String publicId, String authifyerId) {
        GlobalUser owner = globalUserRepo.findBySubjectId(principal.getSubjectId()).orElseThrow(UserNotFoundException::new);
        Project project = projectRepo.findByPublicProjectId(publicId).orElseThrow(ProjectNotFoundException::new);
        if(!project.getOwner().equals(owner)){
            throw new OwnerMismatchException();
        }
        ProjectUser user = projectUserRepo.findByAuthifyerId(authifyerId).orElseThrow(UserNotFoundException::new);
        if(user.isActive()){
            user.setActive(false);
        }
    }

    @Transactional
    public void toggleStatusViaKey(String secretKey, Map<String, String> payload) {

        Project project = projectRepo.findBySecretKeys(secretKey).orElseThrow(ProjectNotFoundException::new);
        String authifyerId = payload.get("userId");
        ProjectUser user = projectUserRepo.findByAuthifyerId(authifyerId).orElseThrow(UserNotFoundException::new);
        if(user.isActive()){
            user.setActive(false);
        }
    }

    @Transactional
    public void deleteUser(AuthPrincipal principal, String publicId, String authifyerId) {
        GlobalUser owner = globalUserRepo.findBySubjectId(principal.getSubjectId()).orElseThrow(UserNotFoundException::new);
        Project project = projectRepo.findByPublicProjectId(publicId).orElseThrow(ProjectNotFoundException::new);
        if(!project.getOwner().equals(owner)){
            throw new OwnerMismatchException();
        }
        ProjectUser user = projectUserRepo.findByAuthifyerId(authifyerId).orElseThrow(UserNotFoundException::new);
        List<Session> sessions =sessionRepo.findAllBySubjectIdAndRevokedAtIsNull(authifyerId);
        sessionRepo.deleteAll(sessions);
        projectUserRepo.delete(user);
    }

    @Transactional
    public String generate_key(AuthPrincipal principal, String publicProjectId) {
        GlobalUser owner = globalUserRepo.findBySubjectId(principal.getSubjectId()).orElseThrow(UserNotFoundException::new);
        Project project = projectRepo.findByPublicProjectId(publicProjectId).orElseThrow(ProjectNotFoundException::new);
        if(!project.getOwner().equals(owner)){
            throw new OwnerMismatchException();
        }
        String newSecretKey = IdGenerator.generateSecretKey();
        project.getSecretKeys().add(tokenHash.hash(newSecretKey));
        return newSecretKey;
    }
}
