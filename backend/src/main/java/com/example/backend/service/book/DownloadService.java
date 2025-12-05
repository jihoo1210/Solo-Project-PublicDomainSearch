package com.example.backend.service.book;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class DownloadService {

    private final WebClient webClient;

    public Mono<String> downloadFileAsPlainText(String url) {
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(e -> log.info("파일 다운로드 실패: {}", e.getMessage()));
    }
}
