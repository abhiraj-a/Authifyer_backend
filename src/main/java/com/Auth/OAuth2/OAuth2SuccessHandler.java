package com.Auth.OAuth2;
import com.Auth.JWT.AccessTokenClaims;
import com.Auth.Service.SessionService;
import com.Auth.Service.TokenService;
import com.Auth.Util.RefreshCookie;
import com.Auth.Util.RefreshResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Value("${}")
    private final String frontendURL;
    private final SessionService sessionService;
    private final TokenService tokenService;
    private final OAuthProfileMapper mapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;

        OAuthProfile oAuthProfile =mapper.map(oAuth2AuthenticationToken );

        String state = request.getParameter("state");
        String publicProjectId = (String) request.getSession().getAttribute(state);

        if(state!=null){
            request.getSession().removeAttribute("state");
        }

        RefreshResult refreshResult =  sessionService.createOAuthSession(request , oAuthProfile,publicProjectId);

        AccessTokenClaims jwt = tokenService.issueAccessToken(refreshResult.getRawRefreshToken());

        RefreshCookie.set(response, refreshResult.getRawRefreshToken());

        String targetUrl = UriComponentsBuilder.fromHttpUrl(frontendURL + "/auth/api/callback")
                .queryParam("access_token" , jwt)
                .queryParam("scope",refreshResult.getSession().getSessionScope())
                .queryParam("sid",refreshResult.getSession().getPublicId())
                .queryParam("pid",refreshResult.getSession().getPublicProjectId())
                .build().toUriString();
        response.sendRedirect(targetUrl);
    }
}
