package com.blog.exceed.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.blog.exceed.dao.UserInfoDao;

@Mapper
public interface UserInfoMapper {
    void insertUserInfo(UserInfoDao userInfoDao);
}
