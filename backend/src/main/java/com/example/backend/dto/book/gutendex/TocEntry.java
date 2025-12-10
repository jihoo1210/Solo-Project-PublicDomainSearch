package com.example.backend.dto.book.gutendex;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class TocEntry {
    String title;      // 목차 제목
    String chapterKey; // 특수문자 제거 후 대문자 변환한 키
}
