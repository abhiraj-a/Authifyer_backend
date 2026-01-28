package com.Auth.Util;

import lombok.Getter;

@Getter
public class OAuthLoginPrincipal {
    private OAuthProvider provider;
    private String providerId;
    private String email;
}
