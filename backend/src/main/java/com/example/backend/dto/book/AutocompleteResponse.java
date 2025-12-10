package com.example.backend.dto.book;

import lombok.Builder;

import java.util.List;

@Builder
public record AutocompleteResponse(List<AutocompleteItem> items) {

    @Builder
    public record AutocompleteItem(Long id, String title, String author) {
    }
}
