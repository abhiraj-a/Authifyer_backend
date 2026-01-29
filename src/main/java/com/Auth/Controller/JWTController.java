package com.Auth.Controller;
import com.Auth.JWT.AccessTokenClaims;
import com.Auth.JWT.JWKSProvider;
import com.Auth.Service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/authifyer")
public class JWTController {
    private final TokenService tokenService;

    private final JWKSProvider jwksProvider;
    @PostMapping("/jwt/refresh-jwt")
    public ResponseEntity<?> refreshJWT(@CookieValue("refresh_token") String refreshToken){

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(401).body(Map.of("error", "Missing refresh token"));
        }

        AccessTokenClaims  jwt = tokenService.issueAccessToken(refreshToken);

        return ResponseEntity.ok().body(Map.of("access_token",jwt));
    }

    @GetMapping("/.well-known/jwks.json")
    public ResponseEntity<?> getJwks(){
        return ResponseEntity.ok(jwksProvider.getJwks());
    }
}
