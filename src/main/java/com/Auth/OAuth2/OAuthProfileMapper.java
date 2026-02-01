package com.Auth.OAuth2;

import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;


@Component
public class OAuthProfileMapper {

    public  OAuthProfile map(OAuth2AuthenticationToken token){
        String provider =token.getAuthorizedClientRegistrationId();

        return switch (provider){
            case "google"-> OAuthProfile.builder()
                    .provider(provider)
                    .providerUserId(token.getPrincipal().getAttribute("sub"))
                    .email(token.getPrincipal().getAttribute("email"))
                    .name(token.getPrincipal().getAttribute("name"))
                    .build();
            case "github"-> OAuthProfile.builder()
                    .email(token.getPrincipal().getAttribute("email"))
                    .providerUserId(token.getPrincipal().getAttribute("id"))
                    .provider(provider)
                    .name(token.getPrincipal().getAttribute("name"))
                    .build();

            default -> throw new RuntimeException("unsuported provider "+ provider);
        };
    }
}
