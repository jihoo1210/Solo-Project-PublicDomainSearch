package com.example.backend.service.book;

import com.example.backend.dto.book.IndexBookResponse;
import com.example.backend.dto.book.gutendex.GutendexBooksResponse;
import com.example.backend.dto.book.gutendex.GutendexDocumentDto;
import com.example.backend.entity.book.Book;
import com.example.backend.entity.user.enumeration.Language;
import com.example.backend.repository.BookRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
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
    private final DownloadService downloadService;
    private final UploadService uploadService;
    private final ParseToJsonService parseToJsonService;

    public IndexBookResponse searchBooks(String query, Language language) {
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
                        .Id(dto.getId())
                        .title(dto.getTitle())
                        .author(dto.getAuthors().get(0).getName())
                        .downloadUrl(dto.getFormats().getEpubUrl())
                        .imageUrl(dto.getFormats().getImageUrl())
                        .language(Language.fromLocale(dto.getLanguages().get(0)))
                        .build();
                bookRepository.save(book);
                IndexBookResponse.BookDetailResponse responseDetail = IndexBookResponse.BookDetailResponse.builder()
                                .id(dto.getId())
                                .title(dto.getTitle())
                                .author(dto.getAuthors().get(0).getName())
                                .build();
                response.getBookDetails().add(responseDetail);

                // 내용 json으로 변환해서 S3에 저장 필요
                downloadService.downloadFileAsPlainText(dto.getFormats().getTextUrl())
                        .subscribe(content -> {
                            log.info("title: {} 업로드 시작", dto.getTitle());
                                GutendexDocumentDto documentDto = parseToJsonService.convertTextToDocumentDto(content, dto.getTitle());

                                String languageStr = language.name();
                                String s3Key = "books/" + dto.getId() + "/" + languageStr + "/" + dto.getTitle().replace(" ", "_") + ".json";
                            try {
                                uploadService.upload(documentDto, s3Key);
                                log.info("title: {} 업로드 완료", dto.getTitle());
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                        });
            }
        }
        return response;
    }

    private String addQueryParam(String url, String key, String value) {
            return url + "&" + key + "=" + value;

    }
}
