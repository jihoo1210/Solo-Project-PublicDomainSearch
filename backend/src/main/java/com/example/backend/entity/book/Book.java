package com.example.backend.entity.book;

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
    private Long Id;

    @Column
    private String title;

    @Column
    private String author;
}
