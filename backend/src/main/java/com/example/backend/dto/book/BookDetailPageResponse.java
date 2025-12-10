package com.example.backend.dto.book;

import com.example.backend.dto.book.gutendex.GutendexSentenceDto;
import lombok.Builder;

import java.util.List;

@Builder
public record BookDetailPageResponse(
        String title,
        List<ChapterInfo> chapters,
        List<GutendexSentenceDto> sentences,
        int currentPage,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
}
