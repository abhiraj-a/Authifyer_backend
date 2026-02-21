package com.Auth.Security;
import com.Auth.OAuth2.CustomAuthorizationRequestResolver;
import com.Auth.OAuth2.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
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

@Slf4j
@EnableWebSecurity
@RequiredArgsConstructor
@Configuration
public class SecurityConfig {

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;
    private  final JWTFilter jwtFilter;
    private final OAuth2SuccessHandler successHandler;
    private final CustomAuthorizationRequestResolver requestResolver;
    private final ApiKeyAuthFilter apiKeyAuthFilter;

    @Bean
    @Order(3)
    public SecurityFilterChain apiChain(HttpSecurity http) throws Exception {

        return http
                .securityMatcher("/authifyer/**", "/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/authifyer/jwt/**").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            res.setStatus(401);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"error\":\"unauthorized\"}");
                        })
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain publicChain(HttpSecurity http) throws Exception {
        log.warn("public chain accessed");
        return http
                .securityMatcher(
                        "/authifyer/global/signup",
                        "/authifyer/global/login",
                        "/authifyer/project/register/email",
                        "/authifyer/project/login/email",
                        "/authifyer/jwt/refresh-jwt",
                        "/api/auth/verify-email",
                        "/authifyer/.well-known/jwks.json"
                )
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain oauthChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/oauth2/**", "/login/**")
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(s ->
                        s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .oauth2Login(oauth2 ->
                        oauth2
                                .authorizationEndpoint(o ->
                                        o.authorizationRequestResolver(requestResolver))
                                .successHandler(successHandler)
                                .failureUrl("/authifyer/login/oauth/failure")
                )
                .build();
    }

    @Bean
    @Order(4)
    public SecurityFilterChain serverApi(HttpSecurity http) throws Exception {
       return http.cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth->
                        auth.requestMatchers("/api/v1").permitAll())
               .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
               .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }



        @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(frontendUrl ,"http://localhost:3000" ,"http://localhost:5173"));
        config.setAllowCredentials(true);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Set-Cookie"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
