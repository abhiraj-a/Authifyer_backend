package com.Auth.OAuth2;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class OAuthProfile {
    private String provider;
    private String providerUserId;
    private String email;
    private String name;
}
