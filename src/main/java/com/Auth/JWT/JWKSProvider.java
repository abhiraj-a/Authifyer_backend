package com.Auth.JWT;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPublicKey;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JWKSProvider {

    private final JWTKeyProvider jwtKeyProvider;

    public Map<String, Object> getJwks() {

        RSAKey rsaKey = new RSAKey.Builder(jwtKeyProvider.getPublicKey())
                .keyID(jwtKeyProvider.getKeyId())
                .algorithm(JWSAlgorithm.RS256)
                .keyUse(KeyUse.SIGNATURE)
                .build();

        return new JWKSet(rsaKey).toJSONObject();
    }
}
