package com.example.backend.dto.auth.google;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class GoogleTokenResponse {

    private String access_token;
    private int expires_in;
    private String token_type;
    private String scope;
    private String refresh_token;

}
