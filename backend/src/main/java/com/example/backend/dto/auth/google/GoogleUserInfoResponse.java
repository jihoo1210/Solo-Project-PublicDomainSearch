package com.example.backend.dto.auth.google;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nonnull;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class GoogleUserInfoResponse {

    private String id;
    private String email;
    private String name;
}
