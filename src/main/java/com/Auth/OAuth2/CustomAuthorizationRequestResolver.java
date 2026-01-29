package com.Auth.OAuth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final DefaultOAuth2AuthorizationRequestResolver defaultResolver;

    public CustomAuthorizationRequestResolver(ClientRegistrationRepository repo) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(repo,"/oauth2/authorization");
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authRequest=defaultResolver.resolve(request);
        saveProjectIdToSession(request,authRequest);
        return authRequest;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest authRequest=defaultResolver.resolve(request,clientRegistrationId);
        saveProjectIdToSession(request,authRequest);
        return authRequest;
    }

    private void saveProjectIdToSession(HttpServletRequest request,OAuth2AuthorizationRequest authorizationRequest){
        if(authorizationRequest!=null) {
            String projectId = request.getParameter("project_id");
            if (projectId != null && !projectId.isBlank()) {
                HttpSession session = request.getSession();
                session.setAttribute(authorizationRequest.getState(),projectId);
            }
        }
    }
}
