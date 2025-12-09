package com.example.backend.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

public class CookieUtil {

    private static final String ACCESS_TOKEN_NAME = "ACCESS_TOKEN";
    private static final int MAX_AGE = 3600;

    private CookieUtil() {}

    public static void addAccessToken(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(ACCESS_TOKEN_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(MAX_AGE);
        response.addCookie(cookie);
    }

    public static void clearAccessToken(HttpServletResponse response) {
        Cookie cookie = new Cookie(ACCESS_TOKEN_NAME, null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
