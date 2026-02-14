package com.Auth.Security;

import com.Auth.Entity.Project;
import com.Auth.Repo.ProjectRepo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final ProjectRepo projectRepo;
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return !request.getRequestURI().startsWith("/api/v1");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String secretKey = request.getHeader("x-secret-key");
        if(secretKey==null||secretKey.isBlank()){
            filterChain.doFilter(request,response);
            return;
        }
        Project project = projectRepo.findBySecretKeys(secretKey).orElseThrow(RuntimeException::new);

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(project,null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SERVER")));
        SecurityContextHolder.getContext().setAuthentication(token);
        filterChain.doFilter(request,response);
    }
}
