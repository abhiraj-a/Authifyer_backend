package com.Auth.Util;

import java.security.SecureRandom;

public class IdGenerator {
    private static final SecureRandom secureRandom =new SecureRandom();
    private static final String ALPHABET ="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public static String generateAuthifyerId(){
        StringBuilder sb =new StringBuilder("auth_usr_");
        for (int i = 0; i < 18; i++) {
            sb.append(ALPHABET.charAt(secureRandom.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    public static String generatePublicSessionId(){
        StringBuilder sb =new StringBuilder("session_");
        for (int i =0 ; i < 15 ; i++){
            sb.append(ALPHABET.charAt(secureRandom.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    public static String generatePublicProjectId(){
        StringBuilder sb =new StringBuilder("proj_");
        for (int i =0 ; i < 17 ; i++){
            sb.append(ALPHABET.charAt(secureRandom.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    public static String generateGlobalUserSubjectId(){
        StringBuilder sb =new StringBuilder("glob_usr_");
        for (int i =0 ; i < 13 ; i++){
            sb.append(ALPHABET.charAt(secureRandom.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    public static String generatePublishableKey(){
        StringBuilder sb =new StringBuilder("pk_");
        for (int i =0 ; i < 20 ; i++){
            sb.append(ALPHABET.charAt(secureRandom.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
