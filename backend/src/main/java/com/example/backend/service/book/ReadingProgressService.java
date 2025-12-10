package com.example.backend.service.book;

import com.example.backend.dto.book.ReadingProgressDto;
import com.example.backend.entity.book.Book;
import com.example.backend.entity.book.ReadingProgress;
import com.example.backend.entity.user.User;
import com.example.backend.repository.BookRepository;
import com.example.backend.repository.ReadingProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReadingProgressService {

    private final ReadingProgressRepository readingProgressRepository;
    private final BookRepository bookRepository;

    @Transactional(readOnly = true)
    public ReadingProgressDto.Response getProgress(User user, Long bookId) {
        Optional<ReadingProgress> progress = readingProgressRepository.findByUserAndBookId(user, bookId);

        return progress.map(p -> ReadingProgressDto.Response.builder()
                .bookId(p.getBook().getId())
                .bookTitle(p.getBookTitle())
                .currentPage(p.getCurrentPage())
                .lastReadAt(p.getLastReadAt())
                .build())
                .orElse(null);
    }

    @Transactional
    public ReadingProgressDto.Response saveProgress(User user, ReadingProgressDto.Request request) {
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + request.getBookId()));

        ReadingProgress progress = readingProgressRepository.findByUserAndBookId(user, request.getBookId())
                .orElseGet(() -> ReadingProgress.builder()
                        .user(user)
                        .book(book)
                        .bookTitle(request.getBookTitle())
                        .currentPage(request.getCurrentPage())
                        .lastReadAt(LocalDateTime.now())
                        .build());

        progress.updateProgress(request.getCurrentPage());
        if (request.getBookTitle() != null) {
            progress.setBookTitle(request.getBookTitle());
        }

        ReadingProgress saved = readingProgressRepository.save(progress);

        return ReadingProgressDto.Response.builder()
                .bookId(saved.getBook().getId())
                .bookTitle(saved.getBookTitle())
                .currentPage(saved.getCurrentPage())
                .lastReadAt(saved.getLastReadAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<ReadingProgressDto.Response> getAllProgress(User user) {
        List<ReadingProgress> progressList = readingProgressRepository.findByUserOrderByLastReadAtDesc(user);

        return progressList.stream()
                .map(p -> ReadingProgressDto.Response.builder()
                        .bookId(p.getBook().getId())
                        .bookTitle(p.getBookTitle())
                        .currentPage(p.getCurrentPage())
                        .lastReadAt(p.getLastReadAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteProgress(User user, Long bookId) {
        readingProgressRepository.deleteByUserAndBookId(user, bookId);
    }
}
