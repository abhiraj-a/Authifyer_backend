package com.Auth.Repo;

import com.Auth.Entity.JWTKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JWTKeyRepo extends JpaRepository<JWTKey,String> {

    List<JWTKey> findAllByIsActiveTrue();
}
