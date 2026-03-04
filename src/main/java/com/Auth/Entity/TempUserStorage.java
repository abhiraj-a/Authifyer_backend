package com.Auth.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
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
    private ProjectUser projectUser;
    private  GlobalUser globalUser;
    private String subjectId;
}
