package com.Auth.Service;

import com.Auth.DTO.ProjectCreationRequest;
import com.Auth.DTO.ProjectCreationResponse;
import com.Auth.DTO.ProjectDTO;
import com.Auth.Entity.GlobalUser;
import com.Auth.Entity.Project;
import com.Auth.Principal.AuthPrincipal;
import com.Auth.Repo.GlobalUserRepo;
import com.Auth.Repo.ProjectRepo;
import com.Auth.Util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ProjectService {
    private final ProjectRepo projectRepo;
    private final GlobalUserRepo globalUserRepo;

    public ProjectCreationResponse createProject(AuthPrincipal principal ,ProjectCreationRequest projectCreationRequest) {

        GlobalUser owner = globalUserRepo.findBySubjectId(principal.getSubjectId()).orElseThrow(RuntimeException::new);
        String publishableKey = "pk_"+ IdGenerator.generatePublicProjectId();

        Project p = Project.builder()
                .createdAt(Instant.now())
                .owner(owner)
                .publishableKey(publishableKey)
                .name(projectCreationRequest.getProjectName())
                .build();

        projectRepo.save(p);

        return  ProjectCreationResponse.builder()
                .projectName(projectCreationRequest.getProjectName())
                .createdAt(Instant.now())
                .providers(projectCreationRequest.getProviders())
                .emailPasswordEnabled(projectCreationRequest.isEmailPasswordEnabled())
                .publishableKey(publishableKey)
                .build();
    }

    public List<ProjectDTO> loadProjects(AuthPrincipal principal) {
        GlobalUser user = globalUserRepo.findBySubjectId(principal.getSubjectId()).orElseThrow(RuntimeException::new);
        List<Project> projects = projectRepo.findAllByOwner(user);
        return projects.stream()
                .map(p->ProjectDTO.builder()
                        .publicProjectId(p.getPublicId())
                        .createdAt(p.getCreatedAt())
                        .ownerSubjectId(p.getOwner().getSubjectId())
                        .build()).toList();
    }

    public Object getProject(AuthPrincipal principal, String publicId) {
        GlobalUser user = globalUserRepo.findBySubjectId(principal.getSubjectId()).orElseThrow(RuntimeException::new);
        Project project =projectRepo.findByPublicProjectId(publicId).orElseThrow(RuntimeException::new);
        if(!project.getOwner().getSubjectId().equals(user.getSubjectId())) throw new RuntimeException("Invalid");

        return ProjectDTO.builder()
                .publicProjectId(project.getPublicId())
                .createdAt(project.getCreatedAt())
                .projectUsers(project.getProjectUsers().stream().map(p->
                        ProjectDTO.ProjectUserDTO.builder()
                                .email(p.getEmail())
                                .signupAt(p.getCreatedAt())
                                .name(p.getName())
                                .build()
                        ).toList());
    }
}
