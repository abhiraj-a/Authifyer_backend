package com.Auth.DTO;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Builder
@Data
@Getter
public class ProjectDTO {
    private String name;
    private String publicProjectId;
    private String publishableKey;
    private String ownerSubjectId;
    private Instant createdAt;
    private boolean googleOauthEnabled;
    private boolean githubOauthEnabled;
    private boolean emailPassEnabled;
    private List<ProjectUserDTO> projectUsers;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProjectUserDTO{
        private String name;
        private String email;
        private Instant signupAt;
    }
}
