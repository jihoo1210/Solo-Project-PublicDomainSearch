package com.example.backend.service.auth;

import com.example.backend.dto.auth.google.GoogleTokenResponse;
import com.example.backend.dto.auth.google.GoogleUserInfoResponse;
import com.example.backend.service.auth.utility.GoogleOAuthRegistrationProperties;
import com.example.backend.service.auth.utility.GoogleOAuthProviderProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j @RequiredArgsConstructor
@Service
public class GoogleService {

    // Field
    private final RestTemplate restTemplate;
    private final GoogleOAuthRegistrationProperties googleOAuthProperties;
    private final GoogleOAuthProviderProperties googleProviderProperties;

    // Method
    public GoogleUserInfoResponse getUsernameAndEmailAndLocale(String code, String state) throws Exception{
        // 1. Access Token 요청
        String tokenExchangeUrl = googleProviderProperties.getTokenUri() + "?"
                + "grant_type=authorization_code"
                + "&client_id=" + googleOAuthProperties.getClientId()
                + "&client_secret=" + googleOAuthProperties.getClientSecret()
                + "&code=" + code
                + "&state=" + state
                + "&redirect_uri=" + googleOAuthProperties.getRedirectUri(); // 리디렉션 URI는 토큰 요청 시에도 필요합니다.

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<GoogleTokenResponse> tokenResponse = restTemplate.exchange(
                tokenExchangeUrl,
                HttpMethod.POST, // Google은 POST 요청을 권장합니다.
                entity,
                GoogleTokenResponse.class);

        GoogleTokenResponse tokenResponseBody = tokenResponse.getBody();
        if(!tokenResponse.getStatusCode().is2xxSuccessful() || tokenResponseBody == null) {
            throw new IllegalAccessError("Google Access Token 발급 실패");
        }

        String accessToken = tokenResponseBody.getAccess_token();

        // 2. 사용자 정보 요청
        HttpHeaders infoHeaders = new HttpHeaders();
        infoHeaders.setBearerAuth(accessToken);
        HttpEntity<String> infoEntity = new HttpEntity<>(infoHeaders);

        // Google은 userInfoUri(user-info-uri)을 통해 사용자 정보를 제공합니다.
        ResponseEntity<GoogleUserInfoResponse> userInfoResponse = restTemplate.exchange(
                googleProviderProperties.getUserInfoUri(),
                HttpMethod.GET,
                infoEntity,
                GoogleUserInfoResponse.class);
        log.info("GoogleUserInfoResponse: {}", userInfoResponse); // locale 필드는 없을 수도 있음

        if(!userInfoResponse.getStatusCode().is2xxSuccessful() || userInfoResponse.getBody() == null) {
            throw new IllegalAccessError("Google 사용자 정보 요청 실패");
        }
        return userInfoResponse.getBody();
    }
}
