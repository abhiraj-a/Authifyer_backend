package com.Auth.Entity;

import jakarta.persistence.*;
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
public class Project {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private GlobalUser owner;

    private Instant createdAt;

    @Column(nullable = false,unique = true)
    private String publishableKey;

    private List<String> secretKeys;

    @Column(unique = true)
    private String publicId;

    @OneToMany(
            mappedBy = "project",
            fetch = FetchType.LAZY
    )
    private List<ProjectUser> projectUsers;
}
