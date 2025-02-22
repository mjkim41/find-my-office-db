package com.digital_nomad.find_my_office.exception;

import lombok.Getter;

@Getter
public class CrwalingException extends RuntimeException {
    
    private final ErrorCode errorCode;
    
    public CrwalingException(ErrorCode errorCode) {
        super(errorCode.getErrorMessage());
        this.errorCode = errorCode;
    }
    
    public CrwalingException(ErrorCode errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
    }

    
}
