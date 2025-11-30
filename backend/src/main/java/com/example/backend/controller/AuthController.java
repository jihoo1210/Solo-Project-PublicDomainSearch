package com.example.backend.controller;

import com.example.backend.controller.utility.ResponseController;
import com.example.backend.dto.auth.google.GoogleUserInfoResponse;
import com.example.backend.dto.auth.local.LocalLoginRequest;
import com.example.backend.dto.auth.local.LocalResisterRequest;
import com.example.backend.dto.auth.naver.NaverUserInfoResponse;
import com.example.backend.entity.user.User;
import com.example.backend.entity.user.enumeration.AuthProvider;
import com.example.backend.security.CustomUserDetails;
import com.example.backend.security.CustomUserDetailsService;
import com.example.backend.security.JwtProvider;
import com.example.backend.service.UserService;
import com.example.backend.service.auth.GoogleService;
import com.example.backend.service.auth.NaverService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
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
    public ResponseEntity<?> resisterByLocal(LocalResisterRequest dto) {
        try {
            String email = dto.getEmail();
            String username = dto.getUsername();
            String encodedPassword = passwordEncoder.encode(dto.getPassword());

            userService.createUser(email, username, encodedPassword, AuthProvider.LOCAL);
            return ResponseController.success(null);
        } catch (Exception e) {
            return ResponseController.fail(e);
        }
    }


    // ***** <<<<<LOGIN>>>>> ***** //

    // ***** LOCAL ***** //
    @PostMapping("/login/local")
    public ResponseEntity<?> loginByLocal(LocalLoginRequest dto, HttpServletResponse response) {
        try {
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
        } catch (Exception e) {
            return ResponseController.fail(e);
        }
    }

    // ***** NAVER ***** //
    @GetMapping("/login/naver")
    public ResponseEntity<?> getNaverLoginUrl() {
        try {
            log.info("네이버 로그인 URL 요청 받음");
            String url = naverService.getLoginUrl();
            log.info("네이버 로그인 URL 생성 성공: {}", url);
            return ResponseController.success(url);
        } catch (Exception e) {
            log.error("네이버 로그인 URL 생성 실패", e);
            return ResponseController.fail(e);
        }
    }

    @GetMapping("/callback/naver")
    public ResponseEntity<?> callbackByNaver (
            @RequestParam String code,
            @RequestParam String state,
            @RequestParam(required = false) String error,
            @RequestParam(required = false, name = "error_description") String errorDescription,
            HttpServletResponse response) {
        try {
            if(StringUtils.hasText(error) || StringUtils.hasText(errorDescription)) {
                throw new IllegalAccessError("fail login with naver");
            }

            NaverUserInfoResponse userInfoResponse = naverService.getUsernameAndEmail(code, state);

            String email = userInfoResponse.getEmail();
            String username = userInfoResponse.getNickname();

            User user = userService.createUser(email, username, null, AuthProvider.NAVER);

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
        } catch (Exception e) {
            return ResponseController.fail(e);
        }
    }

    // ***** GOOGLE ***** //
    @GetMapping("/login/google")
    public ResponseEntity<?> getGoogleLoginUrl() {
        try {
            log.info("Google 로그인 URL 요청 받음");
            String url = googleService.getLoginUrl();
            log.info("Google 로그인 URL 생성 성공: {}", url);
            return ResponseController.success(url);
        } catch (Exception e) {
            return ResponseController.fail(e);
        }
    }

    @GetMapping("/callback/google")
    public ResponseEntity<?> callbackByGoogle (
            @RequestParam String code,
            @RequestParam String state,
            @RequestParam(required = false) String error,
            @RequestParam(required = false, name = "error_description") String errorDescription,
            HttpServletResponse response) {
        try {
            if(StringUtils.hasText(error) || StringUtils.hasText(errorDescription)) {
                throw new IllegalAccessError("fail login with google");
            }

            GoogleUserInfoResponse userInfoResponse = googleService.getUsernameAndEmail(code, state);

            String email = userInfoResponse.getEmail();
            String username = userInfoResponse.getName(); // Google은 'name' 필드를 사용합니다.

            User user = userService.createUser(email, username, null, AuthProvider.GOOGLE);

            CustomUserDetails userDetails = (CustomUserDetails) customUserDetailsService.loadUserByUsername(user.getEmail());
            String jwtToken = jwtProvider.tokenProvide(userDetails);

            Cookie cookie = new Cookie("ACCESS_TOKEN", jwtToken);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(3600);
            response.addCookie(cookie);

            response.sendRedirect("http://localhost:3000?callback=success");
            return null;
        } catch (Exception e) {
            return ResponseController.fail(e);
        }
    }

    // ***** <<<<<LOGOUT>>>>> ***** //
    @DeleteMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal CustomUserDetails userDetails, HttpServletResponse response) {
        try {
            Cookie cookie = new Cookie("ACCESS_TOKEN", null);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(0);

            response.addCookie(cookie);
            return ResponseController.success(null);
        } catch (Exception e) {
            return ResponseController.fail(e);
        }
    }
}
