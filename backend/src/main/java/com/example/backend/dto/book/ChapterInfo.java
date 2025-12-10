package com.example.backend.dto.book;

import lombok.Builder;

@Builder
public record ChapterInfo(
        String title,
        int startPage
) {
}
