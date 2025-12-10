package com.example.backend.controller.utility;

import com.example.backend.dto.utility.ResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ExceptionController {

    /**
     * @Valid 어노테이션으로 인한 유효성 검사 실패 예외 처리
     * MethodArgumentNotValidException: RequestBody의 필드 검증 실패 시 발생
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDto<?>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        log.error("Validation error occurred", e.getMessage());

        return ResponseController.fail(e);
    }

    /**
     * 일반적인 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto<?>> handleException(Exception e) {
        log.error("Unexpected error occurred", e.getMessage());

        return ResponseController.fail(e);
    }

    /**
     * IllegalAccessException 처리
     */
    @ExceptionHandler(IllegalAccessException.class)
    public ResponseEntity<ResponseDto<?>> handleIllegalAccessException(IllegalAccessException e) {
        log.error("Access error occurred", e.getMessage());

        return ResponseController.fail(e);
    }

    /**
     * IllegalArgumentException 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseDto<?>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Invalid argument error occurred", e.getMessage());

        return ResponseController.fail(e);
    }
}

