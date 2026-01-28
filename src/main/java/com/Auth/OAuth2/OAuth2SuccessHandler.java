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
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final SessionService sessionService;
    private final TokenService tokenService;
    private final OAuthProfileMapper mapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;

        OAuthProfile oAuthProfile =mapper.map(oAuth2AuthenticationToken );

        RefreshResult refreshResult =  sessionService.createOAuthSession(request , oAuthProfile);

        AccessTokenClaims jwt = tokenService.issueAccessToken(refreshResult.getRawRefreshToken());

        RefreshCookie.set(response, refreshResult.getRawRefreshToken());

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write("""
                {
                  "access_token": "%s",
                  "scope": "%s",
                  "pid": "%s",
                  "sid": "%s"
                }
                """.formatted(
                jwt,
                refreshResult.getSession().getSessionScope(),
                refreshResult.getSession().getPublicProjectId(),
                refreshResult.getSession().getPublicId()
        ));
    }
}
