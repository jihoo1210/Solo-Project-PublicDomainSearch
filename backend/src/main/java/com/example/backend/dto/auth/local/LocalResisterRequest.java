package com.example.backend.dto.auth.local;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class LocalResisterRequest {

    private String email;
    private String username;
    private String password;
}
