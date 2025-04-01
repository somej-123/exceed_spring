package com.blog.exceed.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.blog.exceed.dao.UserInfoDao;
import com.blog.exceed.mapper.UserInfoMapper;

@Service
public class UserInfoService {
    private final UserInfoMapper userInfoMapper;

    public UserInfoService(UserInfoMapper userInfoMapper) {
        this.userInfoMapper = userInfoMapper;
    }

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(UserInfoService.class);

    public void register(UserInfoDao userInfoDao) {

        logger.info("registerService: " + userInfoDao.toString());
        
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(userInfoDao.getPassword());
        userInfoDao.setPassword(encodedPassword);

        userInfoMapper.insertUserInfo(userInfoDao);
    }
    
}
