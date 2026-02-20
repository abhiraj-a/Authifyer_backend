package com.Auth.Repo;

import com.Auth.Entity.OAuthStorage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;


public interface OAuthStorageRepo extends JpaRepository<OAuthStorage , UUID> {

    Optional<OAuthStorage> findByProviderAndProviderId(String provider, String providerId);

    OAuthStorage findBySubjectId(String subjectId);

    Optional<OAuthStorage> findByProviderAndProviderIdAndPublishableKey(String provider, String providerUserId, String publicProjectId);

    Optional<OAuthStorage> findByProviderAndProviderIdAndEmail(String provider, String providerUserId, String email);

    Optional<OAuthStorage> findByProviderAndProviderIdAndEmailAndPublishableKey(String provider, String providerUserId, String email, String publishableKey);
}
