package com.Auth.Repo;

import com.Auth.Entity.RefreshToken;
import com.Auth.Entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TokenRepo extends JpaRepository<RefreshToken, UUID> {
    RefreshToken findBySessionIdAndRevokedAtIsNullAndRotatedAtIsNull(Session session);

    Optional<RefreshToken> findTokenHash(String hashedToken);
}
