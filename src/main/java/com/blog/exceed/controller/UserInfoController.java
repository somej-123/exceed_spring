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
import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpHeaders;

import com.blog.exceed.dao.UserInfoDao;
import com.blog.exceed.dto.RegisterResponse;
import com.blog.exceed.service.UserInfoService;
import com.blog.exceed.exception.DuplicateUserException;
import com.blog.exceed.exception.ErrorCode;
import com.blog.exceed.util.JwtUtil;
import com.blog.exceed.dto.LoginRequest;
import com.blog.exceed.dto.LoginResponse;
import com.blog.exceed.service.TokenBlacklistService;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;


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
    @Transactional
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest loginRequest) {
        logger.info("로그인 요청 - userId: {}", loginRequest.getUserId());
        
        try {
            // 로그인 처리
            UserInfoDao userInfo = userInfoService.login(loginRequest.getUserId(), loginRequest.getPassword());
            
            // JWT 토큰 생성
            String token = jwtUtil.generateToken(userInfo.getUserId());
            String refreshToken = jwtUtil.generateRefreshToken(userInfo.getUserId());
            userInfoService.updateRefreshToken(userInfo.getUserId(), refreshToken);

            // 쿠키로 내려주기
            // TODO: 실제 배포 환경에서는 secure=true, sameSite=Strict 또는 None (None 사용 시 Secure 필수) 사용 고려
            //       @Value 등을 사용하여 프로필에 따라 동적으로 설정하는 것이 좋음
            boolean isSecure = true; // 개발 환경에서는 false
            String sameSitePolicy = "Lax"; // 개발 및 대부분의 환경에서 호환성을 위해 Lax 사용

            ResponseCookie accessCookie = ResponseCookie.from("accessToken", token)
                .httpOnly(true).secure(isSecure).path("/").maxAge(60 * 60).sameSite(sameSitePolicy).build();
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true).secure(isSecure).path("/").maxAge(14 * 24 * 60 * 60).sameSite(sameSitePolicy).build();

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userInfo.getUserId());
            response.put("message", "로그인 성공");

            return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(response);
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
    @Transactional
    public ResponseEntity<Map<String, Object>> getMyInfo(@RequestAttribute("userId") String userId) {
        logger.info("사용자 정보 조회 요청 - userId: {}", userId);
        
        try {
            UserInfoDao userInfo = userInfoService.getUserInfo(userId);
            if (userInfo == null) {
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("userId", userInfo.getUserId());
            response.put("email", userInfo.getEmail());
            response.put("nickname", userInfo.getNickname());
            response.put("createdAt", userInfo.getCreatedAt());
            response.put("updatedAt", userInfo.getUpdatedAt());
            response.put("isActive", userInfo.getIsActive());
            response.put("role", userInfo.getRole());

            
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
    @Transactional
    public ResponseEntity<Map<String, Object>> logout(@RequestAttribute("userId") String userId,
                                                    @RequestHeader(value = "Authorization", required = false) String authHeader) {
        logger.info("로그아웃 요청 - userId: {}", userId);
        try {
            String token = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
            // 토큰의 남은 유효 시간 계산 및 블랙리스트 추가
            if (token != null) {
                Date expiration = jwtUtil.getExpirationDateFromToken(token);
                long expirationTime = expiration.getTime();
                tokenBlacklistService.addToBlacklist(token, expirationTime);
            }
            // 인증 정보 제거
            SecurityContextHolder.clearContext();
            // accessToken, refreshToken 쿠키 만료
            ResponseCookie expiredAccess = ResponseCookie.from("accessToken", null)
                .httpOnly(true).secure(true).path("/").maxAge(0).sameSite("Strict").build();
            ResponseCookie expiredRefresh = ResponseCookie.from("refreshToken", null)
                .httpOnly(true).secure(true).path("/").maxAge(0).sameSite("Strict").build();
            Map<String, Object> response = new HashMap<>();
            response.put("message", "로그아웃이 성공적으로 완료되었습니다.");
            return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expiredAccess.toString())
                .header(HttpHeaders.SET_COOKIE, expiredRefresh.toString())
                .body(response);
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
    @Transactional
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

    /**
     * 비밀번호 변경 API
     */
    @PostMapping("/changePassword")
    @Transactional
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody UserInfoDao userInfo) {
        logger.info("비밀번호 변경 요청 - userId: {}", userInfo.getUserId());
        
        try {
            // 비밀번호 찾기 처리
            int changePasswordResult = userInfoService.changePassword(userInfo);
            
            Map<String, Object> response = new HashMap<>();
            response.put("isSuccess", changePasswordResult);
            response.put("message", "비밀번호 변경 완료");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("비밀번호 변경 실패하였습니다.", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 액세스 토큰 재발급 API (refresh 토큰 사용, 쿠키 기반)
     */
    @PostMapping("/refresh")
    @Transactional
    public ResponseEntity<?> refresh(@CookieValue(value = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "refreshToken is missing"));
        }
        // 1. refreshToken 유효성 검증
        if (!jwtUtil.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid refresh token"));
        }
        // 2. refreshToken에서 userId 추출
        String userId = jwtUtil.getUserIdFromToken(refreshToken);
        // 3. DB에 저장된 refreshToken과 일치하는지 확인
        String savedRefreshToken = userInfoService.getRefreshTokenByUserId(userId);
        if (savedRefreshToken == null || !refreshToken.equals(savedRefreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Refresh token mismatch"));
        }
        // 4. 새 accessToken, 새 refreshToken 발급
        String newAccessToken = jwtUtil.generateToken(userId);
        String newRefreshToken = jwtUtil.generateRefreshToken(userId);
        userInfoService.updateRefreshToken(userId, newRefreshToken);
        // httpOnly, secure, path, maxAge 등 옵션 설정 (운영환경에서는 secure(true) 권장, 개발환경은 false로 테스트 가능)
        ResponseCookie cookie = ResponseCookie.from("refreshToken", newRefreshToken)
                .httpOnly(true)
                .secure(true) // 운영환경에서는 true, 개발환경에서는 false로 변경
                .path("/")
                .maxAge(14 * 24 * 60 * 60)
                .sameSite("Strict")
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("accessToken", newAccessToken));
    }

    @PutMapping("/me")
    @Transactional
    public ResponseEntity<Map<String, Object>> updateUserInfo(@RequestBody UserInfoDao userInfo) {
        logger.info("회원수정 요청 - userId: {}", userInfo.getUserId());
        logger.info("회원수정 요청 - email: {}", userInfo.getEmail());
        logger.info("회원수정 요청 - nickname: {}", userInfo.getNickname());
        
        try {
            userInfoService.updateUserInfo(userInfo);
            
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
}
