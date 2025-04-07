package com.blog.exceed.exception;

/**
 * 사용자 중복 발생 시 던지는 커스텀 예외
 * RuntimeException을 상속받아 Unchecked Exception으로 구현
 * 주로 회원가입 시 동일한 사용자 ID가 이미 존재할 때 사용
 */
public class DuplicateUserException extends RuntimeException {
    public DuplicateUserException(String message) {
        super(message);
    }
} 