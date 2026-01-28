package com.Auth.Repo;

import com.Auth.Entity.AuthStorage;
import com.Auth.Entity.GlobalUser;
import com.Auth.Util.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuthStorageRepo extends JpaRepository<AuthStorage, UUID> {
    boolean existsByUserAndProvider(GlobalUser user, OAuthProvider provider);
}
