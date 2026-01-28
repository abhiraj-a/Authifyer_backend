package com.Auth.JWT;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenPair {
    private String accessToken;
    private String refreshToken;
}
