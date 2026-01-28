package com.Auth.Repo;

import com.Auth.Entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VerificationTokenRepo extends JpaRepository<VerificationToken, UUID> {
}
