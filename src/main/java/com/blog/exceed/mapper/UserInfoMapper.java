package com.blog.exceed.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.blog.exceed.dao.UserInfoDao;

@Mapper
public interface UserInfoMapper {
    int insertUserInfo(UserInfoDao userInfoDao);
    UserInfoDao selectUserInfo(String userId);
    boolean forgotPassword(UserInfoDao userInfo);
    int changePassword(UserInfoDao userInfo);
    String getRefreshTokenByUserId(String userId);
    int updateRefreshToken(UserInfoDao userInfoDao);
    int updateUserInfo(UserInfoDao userInfo);
}
