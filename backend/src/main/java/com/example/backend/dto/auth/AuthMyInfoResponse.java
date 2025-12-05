package com.example.backend.dto.auth;

import com.example.backend.entity.user.enumeration.Role;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AuthMyInfoResponse {

    private String email;
    private String username;
    private Role role;

}
