package com.example.backend.service;

import com.example.backend.entity.user.User;
import com.example.backend.entity.user.enumeration.AuthProvider;
import org.springframework.stereotype.Service;

import com.example.backend.dto.auth.naver.NaverUserInfoResponse;
import com.example.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User createUser(String email, String username, String password, AuthProvider auth) {
        if(userRepository.existsByEmail(email)) {
            return userRepository.findByEmail(email).orElse(null);
        } else {
            User newUser = User.createUser(auth, email, username, password);
            return userRepository.save(newUser);
        }
    }

    public boolean checkAuthProvider(String email, AuthProvider auth) {
        User user = userRepository.findByEmail(email).orElse(null);
        if(user != null) return user.getAuthProvider().equals(auth);
        return false;
    }
}
