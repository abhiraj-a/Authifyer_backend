package com.Auth.JWT;

import com.auth0.jwt.algorithms.Algorithm;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDate;

@Component
@Data
public class JWTKeyProvider {


    private final Algorithm algorithm;
    private final String kid;
    private final RSAPublicKey publicKey;

    public JWTKeyProvider() {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(4096);
            KeyPair pair = gen.generateKeyPair();

            this.publicKey = (RSAPublicKey) pair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) pair.getPrivate();

            this.kid = "rsa-" + LocalDate.now();

            this.algorithm = Algorithm.RSA256(publicKey, privateKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public String getKeyId() {
        return kid;
    }

}
