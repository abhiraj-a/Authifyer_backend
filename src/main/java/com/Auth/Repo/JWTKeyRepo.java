package com.Auth.Repo;

import com.Auth.Entity.JWTKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JWTKeyRepo extends JpaRepository<JWTKey,String> {
}
