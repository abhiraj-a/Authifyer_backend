package com.Auth.Repo;

import com.Auth.Entity.OAuthStorage;
import com.Auth.Util.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OAuthStorageRepo extends JpaRepository<OAuthStorage , UUID> {

    boolean existsByProviderIdAndProvider(String providerId, OAuthProvider provider);

    Optional<OAuthStorage> findByProviderAndProviderId(String provider, String providerId);

    OAuthStorage findBySubjectId(String subjectId);
}
