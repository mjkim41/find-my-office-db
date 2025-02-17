package com.digital_nomad.find_my_office.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
// 에러가 발생했을 때 클라이언트에게 전송할 구체적인 에러내용들을 담은 JSON
public class ErrorResponse {

    private final LocalDateTime timestamp;
    private final int status; // 에러상태 코드
    private final String error; // 에러 이름
    private final String message; // 에러 메시지
    private final String path; //에러 발생 경로

}
