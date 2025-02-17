package com.digital_nomad.find_my_office.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {

    // 알 수 없는 서버오류
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error"),

    // 전국 상가정보 csv parsing 관련 에러
    CSV_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "No CSV files found in /data directory. Please refer to the README.md for downloading csv files.");

    private final HttpStatus httpStatus;
    private final String errorMessage;

}
