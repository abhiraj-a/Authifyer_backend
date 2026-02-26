package com.Auth.DTO;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ToggleUserDTO {
    private String authifyerId;
    private boolean isActive;
}
