package com.blog.exceed.controller;

import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blog.exceed.dao.UserInfoDao;
import com.blog.exceed.service.UserInfoService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/users")
public class UserInfoController {

    private static final Logger logger = LoggerFactory.getLogger(UserInfoController.class);

    private final UserInfoService userInfoService;

    public UserInfoController(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    @PostMapping("/register")
    public void register(@RequestBody UserInfoDao userInfoDao) {
        //TODO: process POST request
        logger.info("register: " + userInfoDao.getClass().getName());
        logger.info("register: " + userInfoDao.toString());
        
        
        userInfoService.register(userInfoDao);
        
        
    }
    
}
