package com.Auth.Entity;

import com.Auth.Util.OAuthProvider;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AuthStorage {
    @Id
    @GeneratedValue
    private UUID id;

    @JoinColumn(name = "user_id")
    private GlobalUser user;

    @Enumerated(EnumType.STRING)
    private OAuthProvider provider;

    private String providerUserId;

    private String accessToken;

    private String passwordHash;
}
