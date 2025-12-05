package com.example.backend.dto.book.gutendex;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Builder
@Value
public class GutendexDocumentDto {
    String title;
    List<String> tableOfContent;
    List<GutendexSentenceDto> sentences;

}
