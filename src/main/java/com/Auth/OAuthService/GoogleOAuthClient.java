package com.Auth.OAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class GoogleOAuthClient {
    private final WebClient webClient;
    private final JwtDecoder jwtDecoder;

    private GoogleJwtConfig config;
    public ProviderUserInfo verify(String code){

        TokenResponse token = webClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("client_id", config.getClientId())
                        .with("client_secret", config.getClientSecret())
                        .with("code", code)
                        .with("grant_type", "authorization_code")
                        .with("redirect_uri", config.getRedirectUri()))
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .block();
        Jwt jwt = jwtDecoder.decode(token.getIdToken());
        return  ProviderUserInfo.builder()
                .providerUserId(jwt.getSubject())
                .email(jwt.getClaim("email"))
                .build();
    }



}
