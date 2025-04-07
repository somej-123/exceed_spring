package com.blog.exceed.exception;

import lombok.Getter;

/**
 * 애플리케이션에서 발생할 수 있는 모든 에러를 정의하는 enum
 * 
 * 사용 방법:
 * 1. 에러 코드는 4자리 숫자로 구성
 * 2. 첫 번째 숫자는 에러 카테고리
 *    - 0: 성공
 *    - 1: 사용자 관련 에러
 *    - 9: 시스템 에러
 * 3. 나머지 3자리는 세부 에러 코드
 */
@Getter
public enum ErrorCode {
    // 성공 (0000)
    SUCCESS("0000", "성공적으로 처리되었습니다."),
    
    // 사용자 관련 에러 (1000번대)
    DUPLICATE_USER_ID("1001", "이미 사용 중인 아이디입니다."),
    USER_NOT_FOUND("1002", "존재하지 않는 사용자입니다."),
    INVALID_PASSWORD("1003", "비밀번호가 일치하지 않습니다."),
    
    // 시스템 에러 (9000번대)
    SERVER_ERROR("9001", "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),
    DATABASE_ERROR("9002", "데이터베이스 오류가 발생했습니다.");

    // 에러 코드 (4자리 숫자)
    private final String code;
    
    // 사용자에게 보여줄 에러 메시지
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
} 