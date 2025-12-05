package com.example.backend.dto.book;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Builder
@AllArgsConstructor
@Data

public class IndexBookResponse {

    String nextUrl;
    @Builder.Default
    List<BookDetailResponse> bookDetails = new ArrayList<>();

    @Builder
    public record BookDetailResponse(Long id, String title, String author, String imageUrl) {
    }


}
