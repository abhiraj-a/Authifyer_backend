package com.Auth.Repo;

import com.Auth.Entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionRepo extends JpaRepository<Session, UUID> {


    Optional<Session> findByPublicId(String sessionPublicId);

    Optional<Session> findByTokenHash(String hash);

    List<Session> findAllBySubjectIdAndRevokedAtIsNull(String subjectId);

    List<Session> findBySubjectId(String subjectId);
}
