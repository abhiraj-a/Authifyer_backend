package com.Auth.Util;

import org.springframework.beans.factory.annotation.Value;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;
import java.util.Base64;

public class TokenHash {
    @Value("${SERVER.SECRET.KEY}")
   private static String secret;
    private static final String HMAC_ALGO ="HmacSHA256";
    public static String hash(String token){
        Mac mac = null;
        try {
            mac = Mac.getInstance(HMAC_ALGO);
            SecretKeySpec keySpec =new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),HMAC_ALGO);
            mac.init(keySpec);
            byte[] bytes = mac.doFinal(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().withoutPadding().encodeToString(bytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}
