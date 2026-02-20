package com.Auth.Security;

import com.Auth.Entity.Session;
import com.Auth.Exception.SessionNotFoundException;
import com.Auth.JWT.JWTKeyProvider;
import com.Auth.Principal.AuthPrincipal;
import com.Auth.Repo.SessionRepo;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class JWTFilter extends OncePerRequestFilter {

    private final JWTKeyProvider keyProvider;
    private final SessionService sessionService;
    private final SessionRepo sessionRepo;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        String path = request.getRequestURI();

        return path.startsWith("/oauth2/")
                || path.startsWith("/login/oauth2/")
                || path.equals("/authifyer/global/signup")
                || path.equals("/authifyer/global/login")
                || path.equals("/authifyer/jwt/refresh-jwt")
                || path.equals("/authifyer/session/refresh")
                || path.equals("/authifyer/.well-known/jwks.json")
                || path.equals("/api/auth/verify-email");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        log.warn("jwt filter initiated");
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = header.substring(7);
        if (token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }
        JWTVerifier verifier = JWT.require(keyProvider.getAlgorithm())
                .withIssuer("http://localhost:8080")
                .build();
        DecodedJWT decodedJWT = verifier.verify(token);

        String authId = decodedJWT.getSubject();

      String publicSessionId = decodedJWT.getClaim("sid").asString();

      String type =decodedJWT.getClaim("scope").asString();
       Session session =sessionRepo.findByPublicId(publicSessionId).orElseThrow(SessionNotFoundException::new);

       log.warn("type : " + type);

      if(session.getRevokedAt()!=null){
          log.warn("session revoked ");
          response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
          return;
      }
      if(isGlobal(request)&& !type.equals(SessionScope.GLOBAL.toString().toLowerCase())){
          log.warn("isglobal method");
          log.warn(request.getRequestURI());
          response.setStatus(HttpServletResponse.SC_FORBIDDEN);
          return;
      }
        if(isProject(request)&& !type.equals(SessionScope.PROJECT.toString().toLowerCase())){
            log.warn("is project");
            log.warn(request.getRequestURI());
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

        return path.startsWith("/authifyer/project");
    }

    private boolean isGlobal(HttpServletRequest request){
        String path = request.getRequestURI();

        return path.startsWith("/authifyer/global");
    }
}
