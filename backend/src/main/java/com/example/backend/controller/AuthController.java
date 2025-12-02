package com.example.backend.controller;

import com.example.backend.controller.utility.ResponseController;
import com.example.backend.dto.auth.AuthMyInfoResponse;
import com.example.backend.dto.auth.AuthResetPasswordRequest;
import com.example.backend.dto.auth.google.GoogleUserInfoResponse;
import com.example.backend.dto.auth.local.LocalLoginRequest;
import com.example.backend.dto.auth.local.LocalResisterRequest;
import com.example.backend.dto.auth.naver.NaverUserInfoResponse;
import com.example.backend.entity.user.User;
import com.example.backend.entity.user.enumeration.AuthProvider;
import com.example.backend.entity.user.enumeration.Language;
import com.example.backend.security.CustomUserDetails;
import com.example.backend.security.CustomUserDetailsService;
import com.example.backend.security.JwtProvider;
import com.example.backend.service.UserService;
import com.example.backend.service.auth.GoogleService;
import com.example.backend.service.auth.NaverService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Slf4j @RequiredArgsConstructor
@RestController @RequestMapping("/api/auth")
public class AuthController {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    private final UserService userService;
    private final NaverService naverService;
    private final GoogleService googleService;


    // ***** <<<<<RESISTER>>>>> ***** //
    @PostMapping("/resister/local")
    public ResponseEntity<?> resisterByLocal(@RequestBody LocalResisterRequest dto) throws Exception {
        String email = dto.getEmail();
        String username = dto.getUsername();
        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        Language language = Language.fromLocale(dto.getLanguage());

        userService.createUser(email, username, encodedPassword, AuthProvider.LOCAL, language);
        return ResponseController.success(null);
    }


    // ***** <<<<<LOGIN>>>>> ***** //

    // ***** LOCAL ***** //
    @PostMapping("/login/local")
    public ResponseEntity<?> loginByLocal(@RequestBody LocalLoginRequest dto, HttpServletResponse response) throws Exception {
        String email = dto.getEmail();
        String password = dto.getPassword();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email,password));

        if(!userService.checkAuthProvider(email, AuthProvider.LOCAL)) {
            throw new IllegalAccessException("다른 방식으로 가입된 회원입니다.");
        }

        CustomUserDetails userDetails = (CustomUserDetails) customUserDetailsService.loadUserByUsername(email);
        String token = jwtProvider.tokenProvide(userDetails);

        Cookie cookie = new Cookie("ACCESS_TOKEN", token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(3600);

        response.addCookie(cookie);

        return ResponseController.success(null);
    }

    // ***** NAVER ***** //
    @GetMapping("/callback/naver")
    public ResponseEntity<?> callbackByNaver (
            @RequestParam String code,
            @RequestParam String state,
            @RequestParam(required = false) String error,
            @RequestParam(required = false, name = "error_description") String errorDescription,
            HttpServletResponse response) throws Exception {

        if(StringUtils.hasText(error) || StringUtils.hasText(errorDescription)) {
            throw new IllegalAccessException("fail login with naver");
        }

        NaverUserInfoResponse userInfoResponse = naverService.getUsernameAndEmail(code, state);

        String email = userInfoResponse.getEmail();
        String username = userInfoResponse.getNickname();

        User user = userService.createUser(email, username, null, AuthProvider.NAVER, Language.KO);

        CustomUserDetails userDetails = (CustomUserDetails) customUserDetailsService.loadUserByUsername(user.getEmail());
        String jwtToken = jwtProvider.tokenProvide(userDetails);

        Cookie cookie = new Cookie("ACCESS_TOKEN", jwtToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(3600);
        response.addCookie(cookie);

        // 프론트엔드로 리다이렉트
        response.sendRedirect("http://localhost:3000?callback=success");
        return null;
    }

    // ***** GOOGLE ***** //
    @GetMapping("/callback/google")
    public ResponseEntity<?> callbackByGoogle (
            @RequestParam String code,
            @RequestParam String state,
            @RequestParam(required = false) String error,
            @RequestParam(required = false, name = "error_description") String errorDescription,
            HttpServletResponse response) throws Exception {

        if(StringUtils.hasText(error) || StringUtils.hasText(errorDescription)) {
            throw new IllegalAccessException("fail login with google");
        }

        GoogleUserInfoResponse userInfoResponse = googleService.getUsernameAndEmailAndLocale(code, state);

        String email = userInfoResponse.getEmail();
        String username = userInfoResponse.getName(); // Google은 'name' 필드를 사용합니다.
        Language language = Language.fromLocale(userInfoResponse.getLocale());

        User user = userService.createUser(email, username, null, AuthProvider.GOOGLE, language);

        CustomUserDetails userDetails = (CustomUserDetails) customUserDetailsService.loadUserByUsername(user.getEmail());
        String jwtToken = jwtProvider.tokenProvide(userDetails);

        Cookie cookie = new Cookie("ACCESS_TOKEN", jwtToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(3600);
        response.addCookie(cookie);

        response.sendRedirect("http://localhost:3000?callback=success");
        return null;
    }

    // ***** <<<<<LOGOUT>>>>> ***** //
    @DeleteMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal CustomUserDetails userDetails, HttpServletResponse response) throws Exception {
        Cookie cookie = new Cookie("ACCESS_TOKEN", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        response.addCookie(cookie);
        return ResponseController.success(null);
    }

    // ***** <<<<<UTILITY>>>>> ***** //
    @GetMapping("/myInfo")
    public ResponseEntity<?> getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails) throws Exception {
        AuthMyInfoResponse user = userService.getUserByEmail(userDetails.getUsername());
        return ResponseController.success(user);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@AuthenticationPrincipal CustomUserDetails userDetails, @Valid @RequestBody AuthResetPasswordRequest dto) throws Exception {
        String email = userDetails.getUsername();
        userService.resetPassword(email, dto);
        return ResponseController.success("Password reset link has been sent to your email.");
    }
}
