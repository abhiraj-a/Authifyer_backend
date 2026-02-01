package com.Auth.Repo;

import com.Auth.Entity.GlobalUser;
import jakarta.validation.constraints.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface GlobalUserRepo extends JpaRepository<GlobalUser, UUID> {
    Optional<GlobalUser> findByEmail(@Email String email);


    Optional<GlobalUser> findBySubjectId(String subjectId);
}
