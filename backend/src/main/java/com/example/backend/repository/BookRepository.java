package com.example.backend.repository;

import com.example.backend.entity.book.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
    boolean existsByAuthorAndTitle(String author, String title);
}
