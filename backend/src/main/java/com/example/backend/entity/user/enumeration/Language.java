package com.example.backend.entity.user.enumeration;

public enum Language {
    KO, EN;

    public static Language fromLocale(String locale) {
        if (locale == null || locale.isBlank()) {
            return EN;
        }
        // "en", "en-US", "ko_KR" 등 다양한 형식을 처리: 접두사(영문자)만 추출
        String normalized = locale.trim().split("[^A-Za-z]")[0].toUpperCase();
        for (Language l : values()) {
            if (l.name().equalsIgnoreCase(normalized)) {
                return l;
            }
        }
        return EN; // 지원하지 않으면 기본 EN
    }
}
