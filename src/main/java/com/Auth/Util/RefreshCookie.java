package com.Auth.Util;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

public class RefreshCookie {

    private static final String COOKIE_NAME = "refresh_token";

    public static void set(HttpServletResponse response ,String refreshToken){
        ResponseCookie cookie = ResponseCookie
                .from(COOKIE_NAME,refreshToken)
                .httpOnly(true)
                .secure(true)
                .maxAge(Duration.ofDays(30))
                .sameSite("Lax")
                .path("auth/token/refresh")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE,cookie.toString());
    }


    public static void clear(HttpServletResponse response){
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/auth/token/refresh")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
    
}
