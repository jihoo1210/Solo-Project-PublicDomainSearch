package com.example.backend.controller;

import com.example.backend.service.BookService;
import com.example.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> searchBooks(@RequestParam String query) {

        bookService.searchBooks(query);

        return ResponseEntity.ok("Search results for query: " + query);
    }
}
