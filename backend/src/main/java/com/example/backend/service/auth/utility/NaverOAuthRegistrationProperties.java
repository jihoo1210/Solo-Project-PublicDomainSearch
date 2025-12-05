package com.example.backend.service.auth.utility;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "spring.security.oauth2.client.registration.naver")
public class NaverOAuthRegistrationProperties {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String authorizationGrantType;
    private List<String> scope;
}
