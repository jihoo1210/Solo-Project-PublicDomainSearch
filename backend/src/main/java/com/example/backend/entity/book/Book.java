package com.example.backend.entity.book;

import com.example.backend.entity.user.enumeration.Language;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data

@Entity
public class Book {

    @Id
    private Long id;

    @Column
    private String title;

    @Column
    private String author;

    @Column
    private String downloadUrl;

    @Column
    private String textUrl;

    @Column
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private Language language;
}
