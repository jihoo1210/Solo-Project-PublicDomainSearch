package com.example.backend.service.auth;

import com.example.backend.service.auth.utility.NaverOAuthRegistrationProperties;
import com.example.backend.service.auth.utility.NaverOAuthProviderProperties;
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
    private final NaverOAuthRegistrationProperties naverOAuthProperties;
    private final NaverOAuthProviderProperties naverProviderProperties;

    // METHOD
    public NaverUserInfoResponse getUsernameAndEmail(String code, String state) throws Exception{
        String url = naverProviderProperties.getTokenUri() + "?"
                + "grant_type=" + naverOAuthProperties.getAuthorizationGrantType()
                + "&client_id=" + naverOAuthProperties.getClientId()
                + "&client_secret=" + naverOAuthProperties.getClientSecret()
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
        log.info("tokenResponse: {}", tokenResponse);

            headers.setBearerAuth(accessToken);
            entity = new HttpEntity<>(headers);
            ResponseEntity<NaverUserInfoResponse> accessResponse = restTemplate.exchange(
                    naverProviderProperties.getUserInfoUri(),
                    HttpMethod.GET,
                    entity,
                    NaverUserInfoResponse.class);
            return accessResponse.getBody();
        } else {
            throw new IllegalAccessError("fail login with naver");
        }
    }
}
