package com.Auth.DTO;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Builder
@Data
@Getter
public class ProjectDTO {
    private String publicProjectId;
    private String publishableKey;
    private String ownerSubjectId;
    private Instant createdAt;
    private List<ProjectUserDTO> projectUsers;

    @Data
    @Builder
    public static class ProjectUserDTO{
        private String name;
        private String email;
        private Instant signupAt;
    }
}
