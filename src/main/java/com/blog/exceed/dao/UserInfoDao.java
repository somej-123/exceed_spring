package com.blog.exceed.dao;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserInfoDao {
    
    private String userId;
    private String password;
    private String email;
    private String nickname;
    private Date createdAt;
    private Date updatedAt;
    private Boolean isActive;
    private String role;
    private String refreshToken;
    
}
