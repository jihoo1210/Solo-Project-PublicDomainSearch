package com.example.backend.controller.utility;

import com.example.backend.dto.utility.ResponseDto;
import org.springframework.http.ResponseEntity;

public class ResponseController {

    public static <T> ResponseEntity<ResponseDto<T>> success(T dto) {
        ResponseDto<T> response = ResponseDto.<T>builder().result(dto).build();
        return ResponseEntity.ok().body(response);
    }

    public static ResponseEntity<ResponseDto<?>> fail(Exception e) {
        ResponseDto<?> response = ResponseDto.builder().message(e.getMessage()).build();
        return ResponseEntity.badRequest().body(response);
    }
}
