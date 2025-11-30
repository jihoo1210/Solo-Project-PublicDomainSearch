package com.example.backend.service.auth;

import com.example.backend.dto.auth.google.GoogleTokenResponse;
import com.example.backend.dto.auth.google.GoogleUserInfoResponse;
import com.example.backend.service.auth.utility.RandomState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j @RequiredArgsConstructor
@Service
public class GoogleService {

    // Field
    private final RestTemplate restTemplate;
    private final RandomState randomState;
    @Value("${spring.security.oauth2.client.provider.google.authorization-uri}")
    private String authorizationUrl;
    @Value("${spring.security.oauth2.client.provider.google.token-uri}")
    private String tokenUrl;
    @Value("${spring.security.oauth2.client.provider.google.user-info-uri}")
    private String accessUrl;
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUrl;
    @Value("${spring.security.oauth2.client.registration.google.scope}")
    private String scope;

    // Method
    public String getLoginUrl() {
        try {
            String state = randomState.getRandomState(); // CSRF 방지를 위한 상태 값 생성

            String url = authorizationUrl + "?"
                    + "response_type=code"
                    + "&client_id=" + clientId
                    + "&redirect_uri=" + redirectUrl
//                    + "&scope=" + scope // 스코프는 URL 인코딩이 필요합니다.
                    + "&state=" + state;

            log.info("생성된 Google 로그인 URL: {}", url);
            return url;
        } catch (Exception e) {
            log.error("Google 로그인 URL 생성 중 오류 발생", e);
            throw e;
        }
    }

    public GoogleUserInfoResponse getUsernameAndEmail(String code, String state) throws Exception{
        // 1. Access Token 요청
        String tokenExchangeUrl = tokenUrl + "?"
                + "grant_type=authorization_code"
                + "&client_id=" + clientId
                + "&client_secret=" + clientSecret
                + "&code=" + code
                + "&state=" + state
                + "&redirect_uri=" + redirectUrl; // 리디렉션 URI는 토큰 요청 시에도 필요합니다.

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

        // Google은 accessUrl(user-info-uri)을 통해 사용자 정보를 제공합니다.
        ResponseEntity<GoogleUserInfoResponse> userInfoResponse = restTemplate.exchange(
                accessUrl,
                HttpMethod.GET,
                infoEntity,
                GoogleUserInfoResponse.class);

        if(!userInfoResponse.getStatusCode().is2xxSuccessful() || userInfoResponse.getBody() == null) {
            throw new IllegalAccessError("Google 사용자 정보 요청 실패");
        }
        return userInfoResponse.getBody();
    }
}
