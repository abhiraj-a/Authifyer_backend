package com.Auth.Repo;

import com.Auth.Entity.GlobalUser;
import com.Auth.Entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepo extends JpaRepository<Project , UUID>
{
    Optional<Project> findByPublicProjectId(String publicProjectId);


    Optional<Project> findByPublicProjectIdAndProviderAndProviderUserId(String publicProjectId, String provider, String providerUserId);

    List<Project> findAllByOwner(GlobalUser user);
}
