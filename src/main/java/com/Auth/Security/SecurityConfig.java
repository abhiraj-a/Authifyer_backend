package com.Auth.Security;

import com.Auth.OAuth2.CustomAuthorizationRequestResolver;
import com.Auth.OAuth2.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private  final JWTFilter jwtFilter;
    private final OAuth2SuccessHandler successHandler;
    private final CustomAuthorizationRequestResolver requestResolver;
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {

       return httpSecurity.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth->
                        auth.requestMatchers(
                                "/authifyer/global/signup",
                                "/authifyer/global/login",
                                "/authifyer/project/register/**",
                                "/authifyer/project/login/**",
                                "/authifyer/session/refresh",
                                "/authifyer/jwt/**",
                                "/authifyer/.well-known/jwks.json",
                                "/api/auth/verify-email/**",
                                "/oauth2/**",
                                "/login/**"
                        ).permitAll().anyRequest().authenticated())
               .oauth2Login(oauth2->
                       oauth2.authorizationEndpoint(o->
                                       o.authorizationRequestResolver(requestResolver))
                               .successHandler(successHandler)
                               .failureUrl("/authifyer/login/oauth/failure"))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of("http://localhost:3000" ,"http://localhost:5173"));

        config.setAllowCredentials(true);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setExposedHeaders(List.of("Set-Cookie"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
