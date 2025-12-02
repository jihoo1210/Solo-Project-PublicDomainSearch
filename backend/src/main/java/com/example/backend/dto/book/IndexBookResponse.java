package com.example.backend.dto.book;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter

public class IndexBookResponse {

    private String id;
    private String title;
    private String author;
    private LocalDateTime createdDate;
    private String imageUrl;

}
