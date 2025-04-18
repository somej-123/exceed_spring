package com.blog.exceed.service;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * JWT 토큰 블랙리스트를 관리하는 서비스
 * 
 * 블랙리스트의 필요성:
 * 1. JWT 토큰은 발급 후 서버에서 직접 무효화할 수 없음
 * 2. 로그아웃하거나 토큰이 탈취된 경우에도 만료 시간까지 계속 유효
 * 3. 이러한 문제를 해결하기 위해 블랙리스트로 무효화된 토큰을 관리
 */
@Service
public class TokenBlacklistService {
    private static final Logger logger = LoggerFactory.getLogger(TokenBlacklistService.class);
    
    // 블랙리스트 저장소: 키는 토큰, 값은 만료 시간
    private final ConcurrentHashMap<String, Long> blacklist = new ConcurrentHashMap<>();

    public TokenBlacklistService() {
        // 만료된 토큰을 주기적으로 제거하는 스케줄러
        Executors.newSingleThreadScheduledExecutor()
                .scheduleAtFixedRate(this::removeExpiredTokens, 0, 1, TimeUnit.HOURS);
    }

    /**
     * 토큰을 블랙리스트에 추가
     * 
     * 사용 예시:
     * 1. 사용자가 로그아웃할 때
     * 2. 토큰이 탈취되어 관리자가 강제로 무효화할 때
     * 3. 사용자 계정이 비활성화될 때
     * 
     * @param token JWT 토큰
     * @param expirationTime 토큰 만료 시간 (밀리초)
     */
    public void addToBlacklist(String token, long expirationTime) {
        blacklist.put(token, expirationTime);
        logger.info("토큰이 블랙리스트에 추가됨. 만료 시간: {}", new java.util.Date(expirationTime));
    }

    /**
     * 토큰이 블랙리스트에 있는지 확인
     * 
     * 사용 예시:
     * 1. API 요청이 들어올 때마다 JWT 필터에서 확인
     * 2. 블랙리스트에 있다면 요청을 거부
     * 
     * @param token JWT 토큰
     * @return 블랙리스트에 있으면 true
     */
    public boolean isBlacklisted(String token) {
        boolean isBlacklisted = blacklist.containsKey(token);
        if (isBlacklisted) {
            logger.debug("블랙리스트에 있는 토큰으로 접근 시도됨");
        }
        return isBlacklisted;
    }

    /**
     * 만료된 토큰을 블랙리스트에서 제거
     * 
     * 메모리 관리를 위해 주기적으로 실행:
     * 1. 현재 시간보다 만료 시간이 이전인 토큰들을 제거
     * 2. 메모리 누수 방지
     */
    private void removeExpiredTokens() {
        long now = System.currentTimeMillis();
        int beforeSize = blacklist.size();
        
        blacklist.entrySet().removeIf(entry -> {
            boolean isExpired = entry.getValue() < now;
            if (isExpired) {
                logger.debug("만료된 토큰을 블랙리스트에서 제거: {}", 
                    entry.getKey().substring(0, Math.min(10, entry.getKey().length())) + "...");
            }
            return isExpired;
        });
        
        int removedCount = beforeSize - blacklist.size();
        if (removedCount > 0) {
            logger.info("만료된 토큰 {} 개가 블랙리스트에서 제거됨", removedCount);
        }
    }
} 