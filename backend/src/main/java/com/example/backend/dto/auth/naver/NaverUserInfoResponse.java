package com.example.backend.dto.auth.naver;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class NaverUserInfoResponse {

    private String resultcode;
    private String message;
    private NaverUserInfo response;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NaverUserInfo {
        private String id;
        private String email;
        private String nickname;
    }

    // 편의 메서드
    public String getEmail() {
        return response != null ? response.getEmail() : null;
    }

    public String getNickname() {
        return response != null ? response.getNickname() : null;
    }
}