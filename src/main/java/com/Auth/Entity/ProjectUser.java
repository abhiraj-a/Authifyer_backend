package com.Auth.Entity;

import com.Auth.Util.VerifyUser;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ProjectUser implements VerifyUser {

    @Id
    @GeneratedValue
    private UUID id;

    private String authifyerId;

    private String name;

    private String email;

    private String password;

    private Instant createdAt;

    private String provider;

    private String providerId;

    private boolean emailVerified;

    @Builder.Default
    private boolean isActive=true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="project_id")
    private Project project;
    @Override
    public String getSubjectId() {
     return this.authifyerId;
    }
}
