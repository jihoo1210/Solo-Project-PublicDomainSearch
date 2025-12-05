package com.example.backend.controller;

import com.example.backend.controller.utility.ResponseController;
import com.example.backend.dto.book.BookDetailResponse;
import com.example.backend.dto.book.IndexBookResponse;
import com.example.backend.dto.book.gutendex.GutendexDocumentDto;
import com.example.backend.entity.user.enumeration.Language;
import com.example.backend.security.CustomUserDetails;
import com.example.backend.service.UserService;
import com.example.backend.service.book.BookService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final BookService bookService;

    @GetMapping("/books")
    public ResponseEntity<?> searchBooks(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestParam String query) {


        IndexBookResponse response = bookService.searchBooks(query);

        return ResponseController.success(response);
    }

    @GetMapping("/books/{bookId}")
    public ResponseEntity<?> detailBooks(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long bookId) throws JsonProcessingException {

        Language language = userDetails != null ? userDetails.getUser().getLanguage() : Language.EN;
        GutendexDocumentDto response = bookService.detailBooks(bookId, language);
        return ResponseController.success(response);
    }
}
