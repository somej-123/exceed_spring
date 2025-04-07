package com.blog.exceed.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 에러 발생 시 클라이언트에게 전달할 응답 형식을 정의하는 DTO
 * 프론트엔드에서 에러 처리를 위해 사용
 */
@Getter
@Setter
public class ErrorResponse {
    /**
     * 사용자에게 보여줄 에러 메시지
     * 예: "이미 존재하는 사용자 ID입니다."
     */
    private String message;

    /**
     * 에러 종류를 구분하기 위한 코드
     * 예: "USER_DUPLICATE", "INTERNAL_SERVER_ERROR"
     */
    private String code;

    public ErrorResponse(String message, String code) {
        this.message = message;
        this.code = code;
    }
} 