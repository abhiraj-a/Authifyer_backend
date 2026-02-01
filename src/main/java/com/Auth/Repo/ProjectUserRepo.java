package com.Auth.Repo;

import com.Auth.Entity.Project;
import com.Auth.Entity.ProjectUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectUserRepo extends JpaRepository<ProjectUser, UUID> {

    Optional<ProjectUser> findByEmailAndProject(String email, Project project);


    Optional<ProjectUser> findByEmail(String email);

    Optional<ProjectUser> findByAuthifyerId(String subjectId);

    boolean existsByProjectAndEmail(Project project, String email);

    Optional<ProjectUser> findByProjectAndEmail(Project project, String email);

    Optional<ProjectUser> findByProjectAndProviderAndProviderId(Project project, String provider, String providerUserId);
}
