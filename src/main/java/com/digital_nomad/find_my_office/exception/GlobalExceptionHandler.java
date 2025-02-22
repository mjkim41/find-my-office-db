package com.digital_nomad.find_my_office.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(Exception e, HttpServletRequest request) {

        // 로그 생성
        log.error("Unexpected error occurred: {}", e.getMessage(), e);

        // 에러 응답 객체 반환
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .message(ErrorCode.INTERNAL_SERVER_ERROR.getErrorMessage())
                .path(request.getRequestURI())
                .error(ErrorCode.INTERNAL_SERVER_ERROR.name())
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus().value())
                .build();

        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // csv parsing 관련 예외 처리
    @ExceptionHandler(CsvParsingException.class)
    public ResponseEntity<?> handleCsvParsingException(Exception e, HttpServletRequest request) {

        // 로그 생성
        log.error("Csv parsing exception error occurred: {}", e.getMessage(), e);

        // 에러 응답 객체 반환
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .message(ErrorCode.INTERNAL_SERVER_ERROR.getErrorMessage())
                .path(request.getRequestURI())
                .error(ErrorCode.INTERNAL_SERVER_ERROR.name())
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus().value())
                .build();

        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // csv parsing 관련 예외 처리
    @ExceptionHandler(CrwalingException.class)
    public ResponseEntity<?> handleCrwalingException(Exception e, HttpServletRequest request) {

        // 로그 생성
        log.error("Error while crawling reviews: {}", e.getMessage(), e);

        // 에러 응답 객체 반환
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .message(ErrorCode.INTERNAL_SERVER_ERROR.getErrorMessage())
                .path(request.getRequestURI())
                .error(ErrorCode.INTERNAL_SERVER_ERROR.name())
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus().value())
                .build();

        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
