package com.example.backend.service.auth;

import com.example.backend.service.auth.utility.RandomState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.backend.dto.auth.naver.NaverTokenResponse;
import com.example.backend.dto.auth.naver.NaverUserInfoResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor @Slf4j
public class NaverService {

    // FIELD
    private final RestTemplate restTemplate;
    private final RandomState randomState;
    @Value("${spring.security.oauth2.client.provider.naver.authorization-uri}")
    private String authorizationUrl;
    @Value("${spring.security.oauth2.client.provider.naver.token-uri}")
    private String tokenUrl;
    @Value("${spring.security.oauth2.client.provider.naver.user-info-uri}")
    private String accessUrl;
    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String clientSecret;
    @Value("${spring.security.oauth2.client.registration.naver.redirect-uri}")
    private String redirectUrl;
    @Value("${spring.security.oauth2.client.registration.naver.authorization-grant-type}")
    private String authorizationGrantType;

    // METHOD
    public String getLoginUrl() {
        try {
            // null 체크
            if (authorizationUrl == null || clientId == null || redirectUrl == null) {
                log.error("네이버 설정 값이 null입니다. authorizationUrl: {}, clientId: {}, redirectUrl: {}",
                        authorizationUrl, clientId, redirectUrl);
                throw new IllegalStateException("네이버 OAuth 설정이 올바르지 않습니다");
            }

            String state = randomState.getRandomState();

            String url = authorizationUrl + "?"
                    + "response_type=code"
                    + "&client_id=" + clientId
                    + "&redirect_uri=" + redirectUrl
                    + "&state=" + state;

            log.info("생성된 네이버 로그인 URL: {}", url);
            return url;
        } catch (Exception e) {
            log.error("네이버 로그인 URL 생성 중 오류 발생", e);
            throw e;
        }
    }

    public NaverUserInfoResponse getUsernameAndEmail(String code, String state) throws Exception{
        String url = tokenUrl + "?"
                + "grant_type=" + authorizationGrantType
                + "&client_id=" + clientId
                + "&client_secret=" + clientSecret
                + "&code=" + code
                + "&state=" + state;

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<NaverTokenResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                NaverTokenResponse.class);

        NaverTokenResponse tokenResponse = response.getBody();
        if(response.getStatusCode().is2xxSuccessful() && tokenResponse != null) {
            String accessToken = tokenResponse.getAccess_token();

            headers.setBearerAuth(accessToken);
            entity = new HttpEntity<>(headers);
            ResponseEntity<NaverUserInfoResponse> accessResponse = restTemplate.exchange(
                    accessUrl,
                    HttpMethod.GET,
                    entity,
                    NaverUserInfoResponse.class);
            return accessResponse.getBody();
        } else {
            throw new IllegalAccessError("fail login with naver");
        }
    }
}
