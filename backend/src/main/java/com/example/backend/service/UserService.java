package com.example.backend.service;

import com.example.backend.dto.auth.AuthMyInfoResponse;
import com.example.backend.dto.auth.AuthResetPasswordRequest;
import com.example.backend.entity.user.User;
import com.example.backend.entity.user.enumeration.AuthProvider;
import com.example.backend.entity.user.enumeration.Language;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User createUser(String email, String username, String password, AuthProvider auth, Language language) {
        if(userRepository.existsByEmail(email)) {
            return userRepository.findByEmail(email).orElse(null);
        } else {
            User newUser = User.createUser(auth, email, username, password, language);
            return userRepository.save(newUser);
        }
    }

    public boolean checkAuthProvider(String email, AuthProvider auth) {
        User user = userRepository.findByEmail(email).orElse(null);
        if(user != null) return user.getAuthProvider().equals(auth);
        return false;
    }

    public AuthMyInfoResponse getUserByEmail(String username) {
        User user = userRepository.findByEmail(username).orElse(null);
        if(user == null) return null;
        return AuthMyInfoResponse.builder()
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

    public void resetPassword(String email, AuthResetPasswordRequest dto) {
        User user = userRepository.findByEmail(email).orElse(null);
        if(user != null) {
            user.setPassword(dto.getPassword());
            userRepository.save(user);
        }
    }
}
