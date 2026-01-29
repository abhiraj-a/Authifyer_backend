package com.Auth.Security;

import com.Auth.Entity.Session;
import com.Auth.JWT.JWTKeyProvider;
import com.Auth.Principal.AuthPrincipal;
import com.Auth.Service.SessionService;
import com.Auth.Util.SessionScope;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTKeyProvider keyProvider;
    private final SessionService sessionService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        String path = request.getServletPath();
        return path.startsWith("/oauth2/")
                || path.startsWith("/login/oauth2/")
                || path.startsWith("/authifyer/register/")
                || path.startsWith("/authifyer/login/")
                || path.startsWith("/authifyer/session/refresh/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(isPublic(request)){
            filterChain.doFilter(request,response);
            return;
        }
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = header.substring(7);
        JWTVerifier verifier = JWT.require(keyProvider.getAlgorithm())
                .withIssuer("http://localhost:8080")
                .build();
        DecodedJWT decodedJWT = verifier.verify(token);

        String authId = decodedJWT.getSubject();

      String publicSessionId = decodedJWT.getClaim("sid").asString();

      String type =decodedJWT.getClaim("typ").asString();

      Session session =sessionService.getPublicIdCache(publicSessionId);

      if(session.getRevokedAt()!=null){
          response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
          return;
      }
      if(isGlobal(request)&& !type.equals(SessionScope.GLOBAL.toString().toLowerCase())){
          response.setStatus(HttpServletResponse.SC_FORBIDDEN);
          return;
      }

        if(isProject(request)&& !type.equals(SessionScope.PROJECT.toString().toLowerCase())){
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        AuthPrincipal principal = AuthPrincipal.builder()
                .subjectId(authId)
                .build();

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken=
                new UsernamePasswordAuthenticationToken(principal,null, Collections.emptyList());

        usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

        filterChain.doFilter(request,response);
    }

    private boolean isPublic(HttpServletRequest request){
        String path = request.getRequestURI();
        return path.startsWith("/authifyer/jwt") ||
                path.contains("/register") ||
                path.contains("/login") ||
                path.startsWith("/authifyer/session/refresh") ||
                path.startsWith("/api/auth/verify-email") ||
                path.startsWith("/oauth2/") ||
                path.startsWith("/login/oauth2/");
    }

    private boolean isProject(HttpServletRequest request){
        String path = request.getRequestURI();

        if(path.startsWith("/authifyer/project"))return true;
        return false;
    }

    private boolean isGlobal(HttpServletRequest request){
        String path = request.getRequestURI();

        if(path.startsWith("/authifyer/global")) return true;
        return false;
    }
}
