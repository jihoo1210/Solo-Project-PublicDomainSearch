package com.example.backend.service.book;

import com.example.backend.dto.book.gutendex.GutendexDocumentDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.net.URL;
import java.time.Duration;

@Slf4j
@RequiredArgsConstructor
@Service
public class UploadService {

    private final S3Client s3Client;
    private final S3Presigner presigner;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private static final Duration PRESIGN_DURATION = Duration.ofMinutes(10);

    public void upload(GutendexDocumentDto dto, String key) throws JsonProcessingException {
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dto);
        URL presignedUrl = getPresignedPutUrl(key, "application/json");

        try {
            // HTTP 헤더 설정 (Content-Type은 Presigned URL 생성 시 지정한 것과 일치해야 함)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 요청 본문과 헤더를 포함한 HttpEntity 생성
            HttpEntity<String> requestEntity = new HttpEntity<>(json, headers);

            // RestTemplate으로 PUT 요청 전송
            ResponseEntity<Void> response = restTemplate.exchange(
                    presignedUrl.toURI(),
                    HttpMethod.PUT,
                    requestEntity,
                    Void.class
            );

            // 응답 상태 확인
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("S3 upload failed with response code: {}", response.getStatusCode());
            }
        } catch (RestClientException e) {
            log.error("S3 upload error: {}", e.getMessage());
        } catch (Exception e) {
            log.error("S3 upload URI conversion error: {}", e.getMessage());
        }
    }

    public boolean existsByKey(String key) {
        HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        try {
            s3Client.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    public String findByKey(String key) {
        URL presignedUrl = getPresignedGetUrl(key);

        try {
            // RestTemplate으로 GET 요청 전송
            ResponseEntity<String> response = restTemplate.exchange(
                    presignedUrl.toURI(),
                    HttpMethod.GET,
                    null,
                    String.class
            );

            // 응답 본문 반환
            return response.getBody();
        } catch (RestClientException e) {
            log.error("S3 get error: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("S3 get URI conversion error: {}", e.getMessage());
            return null;
        }
    }

    public URL getPresignedGetUrl(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(PRESIGN_DURATION)
                .getObjectRequest(getObjectRequest)
                .build();

        return presigner.presignGetObject(presignRequest).url();
    }

    public URL getPresignedPutUrl(String key, String contentType) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(PRESIGN_DURATION)
                .putObjectRequest(putObjectRequest)
                .build();

        return presigner.presignPutObject(presignRequest).url();
    }
}
