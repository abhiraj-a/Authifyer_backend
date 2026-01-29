package com.Auth.Repo;

import com.Auth.Entity.OAuthStorage;
import com.Auth.Entity.ProjectUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ProjectUserRepo extends JpaRepository<ProjectUser, UUID> {

    Optional<ProjectUser> findByEmailAndPublicProjectId(String email, String publicProjectId);

    boolean existsByPublicProjectIdAndEmail(String publicProjectId, String email);

    Optional<ProjectUser> findByOAuthStorage(OAuthStorage oAuthStorage);

    Optional<ProjectUser> findByPublicProjectIdAndProviderAndProviderId(String publicProjectId, String provider, String providerUserId);

    Optional<ProjectUser> findByEmail(String email);

    Optional<ProjectUser> findByAuthifyerId(String subjectId);
}
