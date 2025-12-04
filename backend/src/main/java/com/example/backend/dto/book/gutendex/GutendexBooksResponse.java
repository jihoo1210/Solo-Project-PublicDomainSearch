package com.example.backend.dto.book.gutendex;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GutendexBooksResponse {

    // 1. 최상위 응답 필드
    private int count;
    private String next;
    private String previous;
    private List<BookResultDto> results; // 내부 클래스 리스트

    // --- 2. 내부 클래스: 개별 도서 정보 (BookResultDto) ---
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class BookResultDto {

        // 요청 필드: id, title, languages
        private Long id;
        private String title;
        private List<String> languages;
        private String media_type;
        private boolean copyright;

        // 내부 클래스 리스트: 저자 정보
        private List<AuthorDto> authors;

        // 내부 클래스 객체: 형식 정보
        private FormatDto formats;

    }

    // --- 3. 내부 클래스: 저자 정보 (AuthorDto) ---
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class AuthorDto {

        // 요청 필드: name
        private String name;
        private Integer birth_year;
        private Integer death_year;

    }

    // --- 4. 내부 클래스: 형식 정보 (FormatDto) ---
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class FormatDto {

        // 요청 필드: application/epub+zip, text/html
        // @JsonProperty를 사용하여 특수 문자가 포함된 JSON 키를 매핑
        @JsonProperty("application/epub+zip")
        private String epubUrl;

        @JsonProperty("text/plain; charset=us-ascii")
        private String textUrl;

        @JsonProperty("image/jpeg")
        private String imageUrl; // 유연성을 위해 다른 형식도 포함할 수 있습니다.

    }
}