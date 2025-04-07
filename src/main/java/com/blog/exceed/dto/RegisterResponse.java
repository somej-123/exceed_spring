package com.blog.exceed.dto;

import com.blog.exceed.exception.ErrorCode;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

/**
 * 회원가입 API의 응답을 담는 클래스
 * 
 * 응답 예시:
 * 1. 성공 시:
 * {
 *     "isSuccess": true,
 *     "errorCode": "0000",
 *     "errorMessage": "성공적으로 처리되었습니다."
 * }
 * 
 * 2. 실패 시:
 * {
 *     "isSuccess": false,
 *     "errorCode": "1001",
 *     "errorMessage": "이미 사용 중인 아이디입니다."
 * }
 */
@Getter
@Setter
@Builder
public class RegisterResponse {
    /**
     * 처리 성공 여부
     * true: 성공, false: 실패
     */
    private boolean isSuccess;
    
    /**
     * 에러 코드
     * 성공 시: "0000"
     * 실패 시: 해당 에러 코드
     */
    private String errorCode;
    
    /**
     * 에러 메시지
     * 성공 시: "성공적으로 처리되었습니다."
     * 실패 시: 해당 에러 메시지
     */
    private String errorMessage;
    
    /**
     * 회원가입 성공 응답을 생성하는 메서드
     */
    public static RegisterResponse success() {
        return RegisterResponse.builder()
            .isSuccess(true)
            .errorCode(ErrorCode.SUCCESS.getCode())
            .errorMessage(ErrorCode.SUCCESS.getMessage())
            .build();
    }
    
    /**
     * 회원가입 실패 응답을 생성하는 메서드
     * @param errorCode 발생한 에러의 코드
     */
    public static RegisterResponse fail(ErrorCode errorCode) {
        return RegisterResponse.builder()
            .isSuccess(false)
            .errorCode(errorCode.getCode())
            .errorMessage(errorCode.getMessage())
            .build();
    }
} 