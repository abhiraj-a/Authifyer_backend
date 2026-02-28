package com.Auth.OAuth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final DefaultOAuth2AuthorizationRequestResolver defaultResolver;

    public CustomAuthorizationRequestResolver(ClientRegistrationRepository repo) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(repo,"/oauth2/authorization");
    }

//    @Override
//    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
//        OAuth2AuthorizationRequest authRequest=defaultResolver.resolve(request);
//        savePublishableKeyToSession(request,authRequest);
//        return authRequest;
//    }
//
//    @Override
//    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
//        OAuth2AuthorizationRequest authRequest=defaultResolver.resolve(request,clientRegistrationId);
//        savePublishableKeyToSession(request,authRequest);
//        return authRequest;
//    }

//    private void savePublishableKeyToSession(HttpServletRequest request, OAuth2AuthorizationRequest authorizationRequest){
//        if(authorizationRequest!=null) {
//            String publishableKey = request.getParameter("publishable_key");
//            if (publishableKey != null && !publishableKey.isBlank()) {
//                HttpSession session = request.getSession();
//                session.setAttribute(authorizationRequest.getState(),publishableKey);
//            }
//        }
//        String redirectUri = request.getParameter("redirect_uri");
//        if(redirectUri!=null&&!redirectUri.isBlank()){
//            HttpSession session = request.getSession();
//            session.setAttribute(authorizationRequest.getState() + "_redirect_uri", redirectUri);
//        }
//    }

    public OAuth2AuthorizationRequest customRequest(HttpServletRequest request, OAuth2AuthorizationRequest authorizationRequest){

        if (authorizationRequest == null) {
            return null;
        }
        String publishableKey = request.getParameter("publishableKey");
        String redirectUri = request.getParameter("redirect_uri");

        if(publishableKey==null){
            publishableKey="";
        }

        if(redirectUri==null){
            redirectUri="";
        }

        String customState = authorizationRequest.getState()+"::"+publishableKey+"::"+redirectUri;
        String encodedCustomState = Base64.getEncoder().encodeToString(customState.getBytes(StandardCharsets.UTF_8));

        return OAuth2AuthorizationRequest.from(authorizationRequest)
                .state(encodedCustomState)
                .build();
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest oAuth2AuthorizationRequest = defaultResolver.resolve(request);
        return customRequest(request,oAuth2AuthorizationRequest);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest oAuth2AuthorizationRequest = defaultResolver.resolve(request , clientRegistrationId);
        return customRequest(request,oAuth2AuthorizationRequest);
    }
}
