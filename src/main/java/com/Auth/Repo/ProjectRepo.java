package com.Auth.Repo;

import com.Auth.Entity.GlobalUser;
import com.Auth.Entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepo extends JpaRepository<Project , UUID>
{
    Optional<Project> findByPublicProjectId(String publicProjectId);

    List<Project> findAllByOwner(GlobalUser user);

}
