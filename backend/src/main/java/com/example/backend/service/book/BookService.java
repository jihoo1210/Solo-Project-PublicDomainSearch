package com.example.backend.service.book;

import com.example.backend.dto.book.IndexBookResponse;
import com.example.backend.dto.book.gutendex.GutendexBooksResponse;
import com.example.backend.dto.book.gutendex.GutendexDocumentDto;
import com.example.backend.entity.book.Book;
import com.example.backend.entity.user.enumeration.Language;
import com.example.backend.repository.BookRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class BookService {

    private final RestTemplate restTemplate;
    private final BookRepository bookRepository;
    private final DownloadService downloadService;
    private final UploadService uploadService;
    private final ParseToJsonService parseToJsonService;
    private final ObjectMapper jsonMapper;

    public IndexBookResponse searchBooks(String query) {
        IndexBookResponse response = IndexBookResponse.builder().build();
        String BASE_URL = "https://gutendex.com/books?copyright=false&language=en";
        String url = addQueryParam(BASE_URL, "search", query);

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<GutendexBooksResponse> restResponse = restTemplate.exchange(url, HttpMethod.GET, entity, GutendexBooksResponse.class);
        if(restResponse.getBody() == null) {
            return null;
        }

        if(restResponse.getBody().getNext() != null) {
            log.info("Next Page URL: {}", restResponse.getBody().getNext());
            response.setNextUrl(restResponse.getBody().getNext());
        }

        List<GutendexBooksResponse.BookResultDto> resultDtoList = restResponse.getBody().getResults();
        log.info("Total Books Found: {}", resultDtoList.size());

        for(GutendexBooksResponse.BookResultDto dto : resultDtoList) {
            log.info("Book ID: {}, Title: {}, TextPlain: {}", dto.getId(), dto.getTitle(), dto.getFormats().getTextUrl());

            if(dto.getMedia_type().equalsIgnoreCase("Text") &&
                    !dto.isCopyright() &&
                    !bookRepository.existsById(dto.getId())) {
                Book book = Book.builder()
                        .id(dto.getId())
                        .title(dto.getTitle())
                        .author(dto.getAuthors().get(0).getName())
                        .downloadUrl(dto.getFormats().getEpubUrl())
                        .imageUrl(dto.getFormats().getImageUrl())
                        .textUrl(dto.getFormats().getTextUrl())
                        .language(Language.fromLocale(dto.getLanguages().get(0)))
                        .build();
                bookRepository.save(book);
                IndexBookResponse.BookDetailResponse responseDetail = IndexBookResponse.BookDetailResponse.builder()
                                .id(dto.getId())
                                .title(dto.getTitle())
                                .author(dto.getAuthors().get(0).getName())
                                .imageUrl(dto.getFormats().getImageUrl())
                                .build();
                response.getBookDetails().add(responseDetail);
            }
        }
        return response;
    }

    public GutendexDocumentDto detailBooks(Long bookId, Language language) throws JsonProcessingException {

            Book book = bookRepository.findById(bookId).orElseThrow(() -> new IllegalArgumentException("해당 도서를 찾을 수 없습니다."));
            String languageStr = language.name();
            String s3Key = generateS3Key(bookId, languageStr, book.getTitle());

            // S3에 없을 때 저장
            if (!uploadService.existsByKey(s3Key)) {
                String content = downloadService.downloadFileAsPlainText(book.getTextUrl())
                        .block(Duration.ofSeconds(60));

                if (content != null) {
                    log.info("title: {} 업로드 시작", book.getTitle());
                    GutendexDocumentDto documentDto = parseToJsonService.convertTextToDocumentDto(content, book.getTitle());

                    try {
                        uploadService.upload(documentDto, s3Key);
                        log.info("title: {} 업로드 완료", book.getTitle());
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            return jsonMapper.readValue(uploadService.findByKey(s3Key), GutendexDocumentDto.class);

    }

    private String addQueryParam(String url, String key, String value) {
            return url + "&" + key + "=" + value;

    }
    private String generateS3Key(Long bookId, String language, String title) {
        return "books/" + bookId + "/" + language + "/" + title.replace(" ", "_") + ".json";
    }
}
