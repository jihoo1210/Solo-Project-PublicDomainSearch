package com.example.backend.controller;

import com.example.backend.controller.utility.ResponseController;
import com.example.backend.dto.book.IndexBookResponse;
import com.example.backend.entity.user.enumeration.Language;
import com.example.backend.security.CustomUserDetails;
import com.example.backend.service.UserService;
import com.example.backend.service.book.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final BookService bookService;

    @GetMapping("/books")
    public ResponseEntity<?> searchBooks(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestParam String query) {

        Language language;
        if(userDetails != null) {
            language = userDetails.getUser().getLanguage();
        } else {
            language = Language.EN;
        }

        IndexBookResponse response = bookService.searchBooks(query, language);

        return ResponseController.success(response);
    }
}
