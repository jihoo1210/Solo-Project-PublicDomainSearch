package com.example.backend.service.book;

import com.example.backend.dto.book.AutocompleteResponse;
import com.example.backend.dto.book.BookDetailPageResponse;
import com.example.backend.dto.book.ChapterInfo;
import com.example.backend.dto.book.IndexBookResponse;
import com.example.backend.dto.book.IndexBookResponse.BookDetailResponse;
import com.example.backend.dto.book.gutendex.GutendexBooksResponse;
import com.example.backend.dto.book.gutendex.GutendexDocumentDto;
import com.example.backend.dto.book.gutendex.GutendexSentenceDto;
import com.example.backend.dto.book.gutendex.TocEntry;
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

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
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


    public IndexBookResponse getAllBooks() {
        List<Book> books = bookRepository.findAll();
        IndexBookResponse response = IndexBookResponse.builder().build();
        books.stream().forEach(book -> {
            BookDetailResponse data = BookDetailResponse.builder()
            .author(book.getAuthor())
            .imageUrl(book.getImageUrl())
            .title(book.getTitle())
            .build();
            response.getBookDetails().add(data);});
        return response;
    }

    public IndexBookResponse searchBooks(String query, String topic) {
        String BASE_URL = "https://gutendex.com/books?copyright=false&language=en&sort=download_count";
        String url = addQueryParam(BASE_URL, "search", query);
        if (topic != null && !topic.isEmpty()) {
            url = addQueryParam(url, "topic", topic);
        }
        log.info("Gutendex 요청 URL: {}", url);
        return fetchBooks(url);
    }

    public IndexBookResponse searchBooksByUrl(String nextUrl) {
        return fetchBooks(nextUrl);
    }

    private IndexBookResponse fetchBooks(String url) {
        IndexBookResponse response = IndexBookResponse.builder().build();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<GutendexBooksResponse> restResponse = restTemplate.exchange(URI.create(url), HttpMethod.GET, entity, GutendexBooksResponse.class);
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
            log.info("Book ID: {}, Title: {}, MediaType: {}, Copyright: {}",
                    dto.getId(), dto.getTitle(), dto.getMedia_type(), dto.isCopyright());

            try {
                if(dto.getMedia_type() != null && dto.getMedia_type().equalsIgnoreCase("Text") && !dto.isCopyright()) {
                    String authorName = (dto.getAuthors() != null && !dto.getAuthors().isEmpty())
                            ? dto.getAuthors().get(0).getName()
                            : "Unknown";
                    String languageCode = (dto.getLanguages() != null && !dto.getLanguages().isEmpty())
                            ? dto.getLanguages().get(0)
                            : "en";

                    // DB에 없으면 저장
                    if (!bookRepository.existsById(dto.getId())) {
                        Book book = Book.builder()
                                .id(dto.getId())
                                .title(dto.getTitle())
                                .author(authorName)
                                .downloadUrl(dto.getFormats().getEpubUrl())
                                .imageUrl(dto.getFormats().getImageUrl())
                                .textUrl(dto.getFormats().getTextUrl())
                                .language(Language.fromLocale(languageCode))
                                .build();
                        bookRepository.save(book);
                    }

                    // 검색 결과에는 항상 추가
                    IndexBookResponse.BookDetailResponse responseDetail = IndexBookResponse.BookDetailResponse.builder()
                            .id(dto.getId())
                            .title(dto.getTitle())
                            .author(authorName)
                            .imageUrl(dto.getFormats().getImageUrl())
                            .build();
                    response.getBookDetails().add(responseDetail);
                    log.info("검색 결과에 추가됨: {}", dto.getTitle());
                } else {
                    log.warn("조건 불충족 - MediaType: {}, Copyright: {}", dto.getMedia_type(), dto.isCopyright());
                }
            } catch (Exception e) {
                log.warn("Book ID {} 처리 중 오류: {}", dto.getId(), e.getMessage());
            }
        }
        log.info("최종 검색 결과 수: {}", response.getBookDetails().size());
        return response;
    }

    private static final int SENTENCES_PER_PAGE = 50;

    public BookDetailPageResponse detailBooks(Long bookId, Language language, int page) throws JsonProcessingException {

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

            GutendexDocumentDto fullDocument = jsonMapper.readValue(uploadService.findByKey(s3Key), GutendexDocumentDto.class);

            // 페이지네이션 처리
            List<GutendexSentenceDto> allSentences = fullDocument.getSentences();
            int totalSentences = allSentences.size();
            int totalPages = (int) Math.ceil((double) totalSentences / SENTENCES_PER_PAGE);

            // 페이지 번호 유효성 검사 (0-based)
            if (page < 0) page = 0;
            if (page >= totalPages) page = totalPages - 1;

            int startIndex = page * SENTENCES_PER_PAGE;
            int endIndex = Math.min(startIndex + SENTENCES_PER_PAGE, totalSentences);

            List<GutendexSentenceDto> pageSentences = allSentences.subList(startIndex, endIndex);

            // 챕터-페이지 매핑 생성
            List<ChapterInfo> chapters = buildChapterInfo(fullDocument.getTableOfContent(), allSentences, totalPages);

            return BookDetailPageResponse.builder()
                    .title(fullDocument.getTitle())
                    .chapters(chapters)
                    .sentences(pageSentences)
                    .currentPage(page)
                    .totalPages(totalPages)
                    .hasNext(page < totalPages - 1)
                    .hasPrevious(page > 0)
                    .build();
    }

    private List<ChapterInfo> buildChapterInfo(List<TocEntry> tableOfContent, List<GutendexSentenceDto> allSentences, int totalPages) {
        List<ChapterInfo> chapters = new ArrayList<>();
        if (tableOfContent == null || tableOfContent.isEmpty()) {
            return chapters;
        }

        // 문단 시작 문장 인덱스만 추출 (챕터 제목은 문단 시작에 위치)
        List<Integer> paragraphStartIndices = new ArrayList<>();
        for (int i = 0; i < allSentences.size(); i++) {
            if (allSentences.get(i).isParagraphStart()) {
                paragraphStartIndices.add(i);
            }
        }

        int lastFoundIndex = 0; // 이전 챕터 이후부터 검색 (순서 보장)

        for (TocEntry entry : tableOfContent) {
            String chapterKey = entry.getChapterKey();
            String title = entry.getTitle();
            if ((chapterKey == null || chapterKey.isEmpty()) && (title == null || title.isEmpty())) {
                continue;
            }

            int foundSentenceIndex = -1;

            // 검색할 패턴들 준비
            List<String> searchPatterns = new ArrayList<>();

            // 1. 원본 제목에서 패턴 추출 (예: "Letter 2" -> "LETTER 2")
            if (title != null && !title.isEmpty()) {
                searchPatterns.add(title.trim().toUpperCase());
            }

            // 2. chapterKey 추가
            if (chapterKey != null && !chapterKey.isEmpty()) {
                searchPatterns.add(chapterKey.trim().toUpperCase());
            }

            // 3. 제목에서 주요 단어 추출 (예: "I. Loomings" -> "LOOMINGS")
            if (chapterKey != null) {
                String[] words = chapterKey.split("\\s+");
                for (String word : words) {
                    String cleanWord = word.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
                        searchPatterns.add(cleanWord);
                }
            }

            // 1단계: 문단 시작 문장에서만 검색 (가장 정확)
            for (int idx : paragraphStartIndices) {
                if (idx < lastFoundIndex) continue;

                String content = allSentences.get(idx).getContent().toUpperCase().trim();

                for (String pattern : searchPatterns) {
                    if (content.equals(pattern) ||
                        content.startsWith(pattern + " ") ||
                        content.startsWith(pattern + ".") ||
                        content.startsWith(pattern + ",") ||
                        content.startsWith(pattern + "_")) {
                        foundSentenceIndex = idx;
                        log.info("Found chapter '{}' at paragraph start {}: '{}'", title, idx,
                                content.substring(0, Math.min(50, content.length())));
                        break;
                    }

                    // 짧은 문장에서 패턴 포함 (챕터 제목은 보통 짧음)
                    if (content.length() < 80 && content.contains(pattern)) {
                        foundSentenceIndex = idx;
                        log.info("Found chapter '{}' (short paragraph) at {}: '{}'", title, idx, content);
                        break;
                    }
                }
                if (foundSentenceIndex != -1) break;
            }

            // 2단계: 못 찾으면 전체 문장에서 검색 (폴백)
            if (foundSentenceIndex == -1) {
                for (int i = lastFoundIndex; i < allSentences.size(); i++) {
                    String content = allSentences.get(i).getContent().toUpperCase().trim();

                    for (String pattern : searchPatterns) {
                        if (content.equals(pattern) || content.startsWith(pattern + " ")) {
                            foundSentenceIndex = i;
                            log.info("Found chapter '{}' (fallback) at sentence {}: '{}'", title, i,
                                    content.substring(0, Math.min(50, content.length())));
                            break;
                        }
                    }
                    if (foundSentenceIndex != -1) break;
                }
            }

            int startPage = 0;
            if (foundSentenceIndex != -1) {
                startPage = foundSentenceIndex / SENTENCES_PER_PAGE;
                lastFoundIndex = foundSentenceIndex + 1;
            } else {
                log.warn("Chapter '{}' (key: {}) not found in text", title, chapterKey);
            }

            chapters.add(ChapterInfo.builder()
                    .title(entry.getTitle())
                    .startPage(startPage)
                    .build());
        }

        return chapters;
    }

    private String addQueryParam(String url, String key, String value) {
        String encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8)
                .replace("+", "%20");
        return url + "&" + key + "=" + encodedValue;
    }
    private String generateS3Key(Long bookId, String language, String title) {
        return "books/" + bookId + "/" + language + "/" + title.replace(" ", "_") + ".json";
    }

    public AutocompleteResponse getAutocompleteData() {
        List<Book> books = bookRepository.findAll();
        List<AutocompleteResponse.AutocompleteItem> items = books.stream()
                .map(book -> AutocompleteResponse.AutocompleteItem.builder()
                        .id(book.getId())
                        .title(book.getTitle())
                        .author(book.getAuthor())
                        .build())
                .toList();
        return AutocompleteResponse.builder().items(items).build();
    }
}
