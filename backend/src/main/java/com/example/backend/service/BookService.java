package com.example.backend.service;

import com.example.backend.dto.book.IndexBookResponse;
import com.example.backend.dto.book.gutendex.GutendexBooksResponse;
import com.example.backend.entity.book.Book;
import com.example.backend.entity.user.enumeration.Language;
import com.example.backend.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class BookService {

    private final RestTemplate restTemplate;
    private final BookRepository bookRepository;

    public List<IndexBookResponse> searchBooks(String query) {
        String BASE_URL = "https://gutendex.com/books?codyright=false";
        String url = addQueryParam(BASE_URL, "search", query);

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<GutendexBooksResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, GutendexBooksResponse.class);
        if(response.getBody() == null) {
            return null;
        }

        String nextPageUrl = "";
        if(response.getBody().getNext() != null) {
            log.info("Next Page URL: {}", response.getBody().getNext());
            nextPageUrl = response.getBody().getNext();
        }

        List<GutendexBooksResponse.BookResultDto> resultDtoList = response.getBody().getResults();
        log.info("Total Books Found: {}", resultDtoList.size());

        for(GutendexBooksResponse.BookResultDto dto : resultDtoList) {
            log.info("Book ID: {}, Title: {}", dto.getId(), dto.getTitle());

            if(dto.getMedia_type().equalsIgnoreCase("Text") &&
                    !dto.isCopyright() &&
                    !bookRepository.existsById(dto.getId()) &&
                    !bookRepository.existsByAuthorAndTitle(dto.getAuthors().get(0).getName(), dto.getTitle())) {
                Book book = Book.builder()
                        .Id(dto.getId())
                        .title(dto.getTitle())
                        .author(dto.getAuthors().get(0).getName())
                        .downloadUrl(dto.getFormats().getEpubUrl())
                        .imageUrl(dto.getFormats().getImageUrl())
                        .language(Language.fromLocale(dto.getLanguages().get(0)))
                        .build();
                bookRepository.save(book);

                // 내용 json으로 변환해서 S3에 저장 필요
            }
        }

        return null;
    }

    private String addQueryParam(String url, String key, String value) {
            return url + "&" + key + "=" + value;

    }
}
