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

    @OneToOne
    private ProjectUser projectUser;
    @OneToOne
    private  GlobalUser globalUser;
    private String subjectId;
}
