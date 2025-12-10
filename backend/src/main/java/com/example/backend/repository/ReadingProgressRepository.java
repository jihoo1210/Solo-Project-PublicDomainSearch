package com.example.backend.repository;

import com.example.backend.entity.book.ReadingProgress;
import com.example.backend.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReadingProgressRepository extends JpaRepository<ReadingProgress, Long> {

    Optional<ReadingProgress> findByUserAndBookId(User user, Long bookId);

    List<ReadingProgress> findByUserOrderByLastReadAtDesc(User user);

    void deleteByUserAndBookId(User user, Long bookId);
}
