package com.blog.exceed.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.blog.exceed.dto.ErrorResponse;

/**
 * 애플리케이션 전역의 예외를 처리하는 핸들러
 * 각 예외 타입별로 적절한 응답을 생성하여 클라이언트에게 반환
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 사용자 중복 예외 처리
     * HTTP Status: 409 Conflict
     * @param e 발생한 DuplicateUserException 예외
     * @return 에러 응답 객체
     */
    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateUserException(DuplicateUserException e) {
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage(), "USER_DUPLICATE");
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * 기타 런타임 예외 처리
     * HTTP Status: 500 Internal Server Error
     * @param e 발생한 RuntimeException 예외
     * @return 에러 응답 객체
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage(), "INTERNAL_SERVER_ERROR");
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
} 