package com.Auth.DTO;

import com.Auth.JWT.TokenPair;
import lombok.Builder;

@Builder
public class PasswordProjectLoginResponseDTO {

    private String publicSessionId;
    private String publicProjectId;
    private String authifyerId;
    private String accessToken;
}
