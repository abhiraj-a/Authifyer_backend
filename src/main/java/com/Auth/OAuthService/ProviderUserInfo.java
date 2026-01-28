package com.Auth.OAuthService;

import lombok.Builder;

@Builder
public class ProviderUserInfo {
    private String providerUserId; // IMMUTABLE (sub / id)
    private String email;
}
