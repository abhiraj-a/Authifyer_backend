package com.Auth.Entity;

import com.Auth.Util.VerifyUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class GlobalUser implements VerifyUser {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;

    @Column(unique = true)
    private String email;

    private String password;

    private Instant createdAt;

    private List<String> organisation;

    private boolean emailVerified;

    private String subjectId;

    private String provider;

    private String providerUserId;

    @Builder.Default
    private boolean isActive=true;
}
