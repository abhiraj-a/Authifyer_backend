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
    @Column(name = "project_usr_id")
    private ProjectUser projectUser;
    @OneToOne
    @Column(name = "global_usr_id")
    private  GlobalUser globalUser;
    private String subjectId;
}
