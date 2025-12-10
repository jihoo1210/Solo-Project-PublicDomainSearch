package com.example.backend.dto.book.gutendex;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Builder
@Value
public class GutendexDocumentDto {
    String title;
    List<TocEntry> tableOfContent;
    List<GutendexSentenceDto> sentences;
}
