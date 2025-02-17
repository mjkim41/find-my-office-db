package com.digital_nomad.find_my_office.exception;

import lombok.Getter;

@Getter
public class CsvParsingException  extends RuntimeException {

    private final ErrorCode errorCode;

    public CsvParsingException(ErrorCode errorCode) {
        super(errorCode.getErrorMessage());
        this.errorCode = errorCode;
    }

    public CsvParsingException(ErrorCode errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
    }

}
