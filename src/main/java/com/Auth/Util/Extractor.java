package com.Auth.Util;

import jakarta.servlet.http.HttpServletRequest;

public class Extractor {
    public static String parseDeviceName(HttpServletRequest servletRequest){
        String header = servletRequest.getHeader("User-Agent");

        if (header.contains("Android")) return "Android device";
        if (header.contains("iPhone")) return "iPhone";
        if (header.contains("Windows")) return "Windows PC";
        if (header.contains("Mac")) return "Mac";
        return "Unknown device";
    }

    public static String getClientIP(HttpServletRequest servletRequest){
        String xff =servletRequest.getHeader("X-Forwarded-For");
        if(xff!=null && !xff.isBlank()){
            return xff.split(",")[0].trim();
        }
        return servletRequest.getRemoteAddr();
    }
}
