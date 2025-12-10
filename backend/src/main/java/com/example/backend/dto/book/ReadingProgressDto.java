package com.example.backend.dto.book;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class ReadingProgressDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private Long bookId;
        private String bookTitle;
        private Integer currentPage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long bookId;
        private String bookTitle;
        private Integer currentPage;
        private LocalDateTime lastReadAt;
    }
}
