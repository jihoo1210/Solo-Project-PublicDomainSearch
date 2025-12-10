package com.example.backend.controller;

import com.example.backend.controller.utility.ResponseController;
import com.example.backend.dto.book.AutocompleteResponse;
import com.example.backend.dto.book.BookDetailPageResponse;
import com.example.backend.dto.book.IndexBookResponse;
import com.example.backend.dto.book.ReadingProgressDto;
import com.example.backend.entity.user.enumeration.Language;
import com.example.backend.security.CustomUserDetails;
import com.example.backend.service.book.BookService;
import com.example.backend.service.book.ReadingProgressService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final BookService bookService;
    private final ReadingProgressService readingProgressService;

    @GetMapping("/books/all")
    public ResponseEntity<?> getAllBooks() {
        IndexBookResponse response = bookService.getAllBooks();
        return ResponseController.success(response);
    }
    

    @GetMapping("/books")
    public ResponseEntity<?> searchBooks(@AuthenticationPrincipal CustomUserDetails userDetails,
                                         @RequestParam String query,
                                         @RequestParam(required = false) String topic) {
        IndexBookResponse response = bookService.searchBooks(query, topic);
        return ResponseController.success(response);
    }

    @GetMapping("/books/next")
    public ResponseEntity<?> searchBooksNext(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestParam String nextUrl) {
        IndexBookResponse response = bookService.searchBooksByUrl(nextUrl);
        return ResponseController.success(response);
    }

    @GetMapping("/books/autocomplete")
    public ResponseEntity<?> getAutocomplete() {
        AutocompleteResponse response = bookService.getAutocompleteData();
        return ResponseController.success(response);
    }

    @GetMapping("/books/{bookId}")
    public ResponseEntity<?> detailBooks(@AuthenticationPrincipal CustomUserDetails userDetails,
                                         @PathVariable Long bookId,
                                         @RequestParam(defaultValue = "0") int page) throws JsonProcessingException {

        Language language = userDetails != null ? userDetails.getUser().getLanguage() : Language.EN;
        BookDetailPageResponse response = bookService.detailBooks(bookId, language, page);
        return ResponseController.success(response);
    }

    // 읽기 진행 상태 조회
    @GetMapping("/reading-progress/{bookId}")
    public ResponseEntity<?> getReadingProgress(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                @PathVariable Long bookId) {
        ReadingProgressDto.Response response = readingProgressService.getProgress(userDetails.getUser(), bookId);
        return ResponseController.success(response);
    }

    // 읽기 진행 상태 저장
    @PostMapping("/reading-progress")
    public ResponseEntity<?> saveReadingProgress(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                 @RequestBody ReadingProgressDto.Request request) {
        ReadingProgressDto.Response response = readingProgressService.saveProgress(userDetails.getUser(), request);
        return ResponseController.success(response);
    }

    // 전체 읽기 진행 상태 조회
    @GetMapping("/reading-progress")
    public ResponseEntity<?> getAllReadingProgress(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseController.success(readingProgressService.getAllProgress(userDetails.getUser()));
    }

    // 읽기 진행 상태 삭제
    @DeleteMapping("/reading-progress/{bookId}")
    public ResponseEntity<?> deleteReadingProgress(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                   @PathVariable Long bookId) {
        readingProgressService.deleteProgress(userDetails.getUser(), bookId);
        return ResponseController.success("삭제되었습니다.");
    }
}
