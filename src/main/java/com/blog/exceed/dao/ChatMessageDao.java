package com.blog.exceed.dao;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class ChatMessageDao {
    private Long id;
    private String content;
    private String role;
    private LocalDateTime createdAt;
} 