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
import com.example.backend.security.CookieUtil;
import com.example.backend.security.CustomUserDetails;
import com.example.backend.service.AuthService;
import com.example.backend.service.UserService;
import com.example.backend.service.auth.GoogleService;
import com.example.backend.service.auth.NaverService;
import com.example.backend.service.auth.utility.GoogleOAuthProviderProperties;
import com.example.backend.service.auth.utility.GoogleOAuthRegistrationProperties;
import com.example.backend.service.auth.utility.NaverOAuthProviderProperties;
import com.example.backend.service.auth.utility.NaverOAuthRegistrationProperties;
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

import java.io.IOException;
import java.util.UUID;

@Slf4j @RequiredArgsConstructor
@RestController @RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    private final AuthService authService;
    private final UserService userService;
    private final NaverService naverService;
    private final GoogleService googleService;
    private final NaverOAuthRegistrationProperties naverOAuthRegistrationProperties;
    private final NaverOAuthProviderProperties naverOAuthProviderProperties;
    private final GoogleOAuthRegistrationProperties googleOAuthRegistrationProperties;
    private final GoogleOAuthProviderProperties googleOAuthProviderProperties;


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

    // ***** NAVER OAuth Authorization ***** //
    @GetMapping("/login/naver")
    public void loginByNaver(HttpServletResponse response) throws IOException {
        String state = UUID.randomUUID().toString();
        String authorizationUrl = naverOAuthProviderProperties.getAuthorizationUri()
                + "?response_type=code"
                + "&client_id=" + naverOAuthRegistrationProperties.getClientId()
                + "&redirect_uri=" + naverOAuthRegistrationProperties.getRedirectUri()
                + "&state=" + state;

        log.info("Naver OAuth 인가 요청: {}", authorizationUrl);
        response.sendRedirect(authorizationUrl);
    }

    // ***** GOOGLE OAuth Authorization ***** //
    @GetMapping("/login/google")
    public void loginByGoogle(HttpServletResponse response) throws IOException {
        String state = UUID.randomUUID().toString();
        String scope = String.join(" ", googleOAuthRegistrationProperties.getScope());
        String authorizationUrl = googleOAuthProviderProperties.getAuthorizationUri()
                + "?response_type=code"
                + "&client_id=" + googleOAuthRegistrationProperties.getClientId()
                + "&redirect_uri=" + googleOAuthRegistrationProperties.getRedirectUri()
                + "&scope=" + scope
                + "&state=" + state;

        log.info("Google OAuth 인가 요청: {}", authorizationUrl);
        response.sendRedirect(authorizationUrl);
    }

    // ***** LOCAL ***** //
    @PostMapping("/login/local")
    public ResponseEntity<?> loginByLocal(@RequestBody LocalLoginRequest dto, HttpServletResponse response) throws Exception {
        String email = dto.getEmail();
        String password = dto.getPassword();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));

        if (!userService.checkAuthProvider(email, AuthProvider.LOCAL)) {
            throw new IllegalAccessException("다른 방식으로 가입된 회원입니다.");
        }

        String token = authService.generateToken(email);
        CookieUtil.addAccessToken(response, token);

        log.info("Local 로그인 성공: {}", email);

        AuthMyInfoResponse userInfo = userService.getUserByEmail(email);
        return ResponseController.success(userInfo);
    }

    // ***** NAVER Callback ***** //
    @GetMapping("/callback/naver")
    public ResponseEntity<?> callbackByNaver(
            @RequestParam String code,
            @RequestParam String state,
            @RequestParam(required = false) String error,
            @RequestParam(required = false, name = "error_description") String errorDescription,
            HttpServletResponse response) throws Exception {

        if (StringUtils.hasText(error) || StringUtils.hasText(errorDescription)) {
            throw new IllegalAccessException("fail login with naver");
        }

        NaverUserInfoResponse userInfoResponse = naverService.getUsernameAndEmail(code, state);

        String email = userInfoResponse.getEmail();
        String username = userInfoResponse.getNickname();

        User user = userService.createUser(email, username, null, AuthProvider.NAVER, Language.KO);

        String token = authService.generateToken(user.getEmail());
        CookieUtil.addAccessToken(response, token);

        log.info("Naver OAuth 콜백 완료: {}", email);

        response.sendRedirect("http://localhost:3000");
        return null;
    }

    // ***** GOOGLE Callback ***** //
    @GetMapping("/callback/google")
    public ResponseEntity<?> callbackByGoogle(
            @RequestParam String code,
            @RequestParam String state,
            @RequestParam(required = false) String error,
            @RequestParam(required = false, name = "error_description") String errorDescription,
            HttpServletResponse response) throws Exception {

        if (StringUtils.hasText(error) || StringUtils.hasText(errorDescription)) {
            throw new IllegalAccessException("fail login with google");
        }

        GoogleUserInfoResponse userInfoResponse = googleService.getUsernameAndEmailAndLocale(code, state);

        String email = userInfoResponse.getEmail();
        String username = userInfoResponse.getName();
        Language language = Language.fromLocale(userInfoResponse.getLocale());

        User user = userService.createUser(email, username, null, AuthProvider.GOOGLE, language);

        String token = authService.generateToken(user.getEmail());
        CookieUtil.addAccessToken(response, token);

        log.info("Google OAuth 콜백 완료: {}", email);

        response.sendRedirect("http://localhost:3000");
        return null;
    }

    // ***** <<<<<LOGOUT>>>>> ***** //
    @DeleteMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal CustomUserDetails userDetails, HttpServletResponse response) throws Exception {
        CookieUtil.clearAccessToken(response);
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
