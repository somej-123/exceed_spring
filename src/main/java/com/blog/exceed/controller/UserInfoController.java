package com.blog.exceed.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.blog.exceed.dao.UserInfoDao;
import com.blog.exceed.dto.RegisterResponse;
import com.blog.exceed.service.UserInfoService;
import com.blog.exceed.exception.DuplicateUserException;
import com.blog.exceed.exception.ErrorCode;
import com.blog.exceed.util.JwtUtil;
import com.blog.exceed.dto.LoginRequest;
import com.blog.exceed.dto.LoginResponse;
import com.blog.exceed.service.TokenBlacklistService;

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
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * UserInfoService를 주입받는 생성자
     * @param userInfoService 사용자 정보 관련 비즈니스 로직을 처리하는 서비스
     */
    public UserInfoController(UserInfoService userInfoService, JwtUtil jwtUtil, TokenBlacklistService tokenBlacklistService) {
        this.userInfoService = userInfoService;
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
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
    public ResponseEntity<Map<String, Object>> register(@RequestBody UserInfoDao userInfo) {
        logger.info("회원가입 요청 - userId: {}", userInfo.getUserId());
        
        try {
            userInfoService.register(userInfo);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "회원가입이 완료되었습니다.");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("회원가입 실패", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 로그인 API
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest loginRequest) {
        logger.info("로그인 요청 - userId: {}", loginRequest.getUserId());
        
        try {
            // 로그인 처리
            UserInfoDao userInfo = userInfoService.login(loginRequest.getUserId(), loginRequest.getPassword());
            
            // JWT 토큰 생성
            String token = jwtUtil.generateToken(userInfo.getUserId());
            
            // 응답 데이터 구성
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("userId", userInfo.getUserId());
            response.put("message", "로그인이 성공적으로 완료되었습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("로그인 실패", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 사용자 정보 조회 API
     * JWT 토큰이 필요한 보호된 엔드포인트
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMyInfo(@RequestAttribute("userId") String userId) {
        logger.info("사용자 정보 조회 요청 - userId: {}", userId);
        
        try {
            UserInfoDao userInfo = userInfoService.getUserInfo(userId);
            if (userInfo == null) {
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("userId", userInfo.getUserId());
            // 비밀번호는 응답에서 제외
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("사용자 정보 조회 실패", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 로그아웃 API
     * JWT 토큰이 필요한 보호된 엔드포인트
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestAttribute("userId") String userId,
                                                    @RequestHeader("Authorization") String authHeader) {
        logger.info("로그아웃 요청 - userId: {}", userId);
        
        try {
            // Bearer 토큰에서 실제 JWT 추출
            String token = authHeader.substring(7);
            
            // 토큰의 남은 유효 시간 계산
            Date expiration = jwtUtil.getExpirationDateFromToken(token);
            long expirationTime = expiration.getTime();
            
            // 토큰을 블랙리스트에 추가
            tokenBlacklistService.addToBlacklist(token, expirationTime);
            
            // 보안을 위해 현재 세션의 인증 정보 제거
            SecurityContextHolder.clearContext();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "로그아웃이 성공적으로 완료되었습니다.");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("로그아웃 실패", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 비밀번호 찾기 API
     */
    @PostMapping("/forgotPassword")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody UserInfoDao userInfo) {
        logger.info("비밀번호 찾기 요청 - userId: {}", userInfo.getUserId());
        logger.info("비밀번호 찾기 요청 - email: {}", userInfo.getEmail());
        
        try {
            // 비밀번호 찾기 처리
            boolean forgotPasswordResult = userInfoService.forgotPassword(userInfo);
            
            Map<String, Object> response = new HashMap<>();
            response.put("isSuccess", forgotPasswordResult);
            response.put("message", "아이디와 이메일 정보가 존재합니다.");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("아이디와 이메일 정보가 존재하지 않습니다.", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
