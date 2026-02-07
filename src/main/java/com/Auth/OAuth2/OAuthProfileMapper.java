package com.Auth.OAuth2;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;


@Component
public class OAuthProfileMapper {

    public  OAuthProfile map(OAuth2AuthenticationToken token){
        String provider =token.getAuthorizedClientRegistrationId();



        if(provider.equals("google")) {
          return   OAuthProfile.builder()
                    .provider(provider)
                    .providerUserId(token.getPrincipal().getAttribute("sub"))
                    .email(token.getPrincipal().getAttribute("email"))
                    .name(token.getPrincipal().getAttribute("name"))
                    .build();
        }


        if(provider.equals("github")) {

            Object  rawId = token.getPrincipal().getAttribute("id");
          return   OAuthProfile.builder()
            .email(token.getPrincipal().getAttribute("email"))
            .providerUserId(String.valueOf(String.valueOf(rawId)))
            .provider(provider)
            .name(token.getPrincipal().getAttribute("name"))
            .build();

        }

        throw new RuntimeException();
    }
}
