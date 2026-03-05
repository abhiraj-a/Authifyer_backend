package com.Auth.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TempUserStorage {
    @Id
    @GeneratedValue
    private UUID id;

    private String subjectId;
    private String email;
    private String name;
    private String password;
    @OneToOne
    private Project project;
}
