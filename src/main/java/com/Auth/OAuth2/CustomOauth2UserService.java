package com.Auth.OAuth2;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomOauth2UserService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // Fetch the standard user profile
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // If the provider is GitHub and the email is missing, fetch it manually
        if ("github".equals(userRequest.getClientRegistration().getRegistrationId())) {
            String email = oAuth2User.getAttribute("email");

            if (email == null) {
                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(userRequest.getAccessToken().getTokenValue());
                HttpEntity<String> entity = new HttpEntity<>("", headers);

                // Call GitHub email API
                ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                        "https://api.github.com/user/emails",
                        HttpMethod.GET,
                        entity,
                        new ParameterizedTypeReference<List<Map<String, Object>>>() {}
                );

                List<Map<String, Object>> emails = response.getBody();
                if (emails != null) {
                    for (Map<String, Object> emailData : emails) {
                        Boolean primary = (Boolean) emailData.get("primary");
                        if (primary != null && primary) {
                            email = (String) emailData.get("email");
                            break;
                        }
                    }
                }

                // Create a new map to hold the modified attributes, including our fetched email
                Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
                attributes.put("email", email);

                // Return a new user with the updated attributes
                return new DefaultOAuth2User(oAuth2User.getAuthorities(), attributes, "id");
            }
        }
        return oAuth2User;
    }
}
