package com.blog.exceed.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.postgresql.util.PSQLException;

import com.blog.exceed.dao.UserInfoDao;
import com.blog.exceed.mapper.UserInfoMapper;
import com.blog.exceed.exception.DuplicateUserException;

/**
 * 사용자 정보 관련 비즈니스 로직을 처리하는 서비스
 */
@Service
public class UserInfoService {
    private final UserInfoMapper userInfoMapper;

    public UserInfoService(UserInfoMapper userInfoMapper) {
        this.userInfoMapper = userInfoMapper;
    }

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(UserInfoService.class);

    /**
     * 새로운 사용자를 등록하는 메서드
     * @param userInfoDao 등록할 사용자 정보
     * @return 등록 성공 시 1, 실패 시 예외 발생
     * @throws DuplicateUserException 동일한 사용자 ID가 이미 존재하는 경우
     * @throws RuntimeException 기타 데이터베이스 오류 발생 시
     */
    public int register(UserInfoDao userInfoDao) {
        logger.info("registerService: " + userInfoDao.toString());
        
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(userInfoDao.getPassword());
        userInfoDao.setPassword(encodedPassword);

        try {
            int result = userInfoMapper.insertUserInfo(userInfoDao);
            // 데이터베이스 insert 실패 시
            if(result == 0) {
                throw new RuntimeException("회원가입 중 오류가 발생했습니다.");
            }
            return result;
        } catch (Exception e) {
            // PostgreSQL 예외 처리
            if (e.getCause() instanceof PSQLException) {
                PSQLException psqlException = (PSQLException) e.getCause();
                // 고유 제약 조건 위반 (사용자 ID 중복)
                if (psqlException.getMessage().contains("user_info_user_id_key")) {
                    throw new DuplicateUserException("이미 존재하는 사용자 ID입니다.");
                }
            }
            // 기타 예외 처리
            throw new RuntimeException("회원가입 중 오류가 발생했습니다.");
        }
    }
    
}
