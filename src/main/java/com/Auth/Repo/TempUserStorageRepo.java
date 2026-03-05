package com.Auth.Repo;

import com.Auth.Entity.ProjectUser;
import com.Auth.Entity.TempUserStorage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TempUserStorageRepo extends JpaRepository<TempUserStorage, UUID> {

    TempUserStorage findBySubjectId(String subjectId);
}
