package com.example.backend.service.auth.utility;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "spring.security.oauth2.client.provider.google")
public class GoogleOAuthProviderProperties {
    private String authorizationUri;
    private String tokenUri;
    private String userInfoUri;
}
