package com.example.backend.dto.book.gutendex;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class GutendexSentenceDto {
    int sentenceNumber;
    int paragraphNumber;
    String content;
    boolean paragraphStart;
}
