package com.blog.exceed.controller;

import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.blog.exceed.dao.UserInfoDao;
import com.blog.exceed.dto.RegisterResponse;
import com.blog.exceed.service.UserInfoService;
import com.blog.exceed.exception.DuplicateUserException;
import com.blog.exceed.exception.ErrorCode;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 사용자 관련 API를 처리하는 컨트롤러
 * 
 * 주요 기능:
 * 1. 회원가입 (/api/users/register)
 * 2. 로그인 (추후 구현 예정)
 * 3. 사용자 정보 조회 (추후 구현 예정)
 */
@RestController
@RequestMapping("/api/users")
public class UserInfoController {

    private static final Logger logger = LoggerFactory.getLogger(UserInfoController.class);

    private final UserInfoService userInfoService;

    /**
     * UserInfoService를 주입받는 생성자
     * @param userInfoService 사용자 정보 관련 비즈니스 로직을 처리하는 서비스
     */
    public UserInfoController(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    /**
     * 회원가입 API
     * 
     * 요청 예시:
     * POST /api/users/register
     * {
     *     "userId": "user123",
     *     "email": "user@example.com",
     *     "password": "password123"
     * }
     * 
     * 응답 예시:
     * 1. 성공 시 (HTTP 200):
     * {
     *     "isSuccess": true,
     *     "errorCode": "0000",
     *     "errorMessage": "성공적으로 처리되었습니다."
     * }
     * 
     * 2. 아이디 중복 시 (HTTP 409):
     * {
     *     "isSuccess": false,
     *     "errorCode": "1001",
     *     "errorMessage": "이미 사용 중인 아이디입니다."
     * }
     * 
     * 3. 서버 오류 시 (HTTP 500):
     * {
     *     "isSuccess": false,
     *     "errorCode": "9001",
     *     "errorMessage": "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
     * }
     */
    @PostMapping("/register")
    @Transactional
    public ResponseEntity<RegisterResponse> register(@RequestBody UserInfoDao userInfoDao) {
        // 요청 로깅
        logger.info("회원가입 요청 - 사용자: {}", userInfoDao.getUserId());
        
        try {
            // 회원가입 처리
            userInfoService.register(userInfoDao);
            
            // 성공 응답 반환
            return ResponseEntity.ok(RegisterResponse.success());
            
        } catch (DuplicateUserException e) {
            // 아이디 중복 에러 처리
            logger.warn("회원가입 실패 - 중복 아이디: {}", userInfoDao.getUserId());
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(RegisterResponse.fail(ErrorCode.DUPLICATE_USER_ID));
                
        } catch (Exception e) {
            // 기타 에러 처리
            logger.error("회원가입 실패 - 서버 오류: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(RegisterResponse.fail(ErrorCode.SERVER_ERROR));
        }
    }
}
