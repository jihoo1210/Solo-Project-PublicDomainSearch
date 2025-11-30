package com.example.backend.entity.book.elastic;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "public_domain_passage")
@Getter
@Builder
public class ElasticBookPassage {

    @Id
    private String id; // bookId_sequenceId

    private Long bookId;

    @Field(type = FieldType.Keyword) // 정확 검색
    private String title;

    @Field(type = FieldType.Keyword) // 정확 검색
    private String author;

    @Field(type = FieldType.Text, analyzer = "english") // 유사 검색 - english 분석기
    private String textContent;

    private Integer sequenceId;

    private String language;
}
