package com.Auth.DTO;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Builder
public class ProjectDTO {
    private String publicProjectId;
    private String ownerSubjectId;
    private Instant createdAt;
    private List<ProjectUserDTO> projectUsers;

    @Data
    @Builder
    public  class ProjectUserDTO{
        private String name;
        private String email;
        private Instant signupAt;
    }
}
