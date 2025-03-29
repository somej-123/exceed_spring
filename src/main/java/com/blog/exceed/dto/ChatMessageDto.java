package com.blog.exceed.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessageDto {
    private Long id;
    private String content;
    private String role;
    private LocalDateTime createdAt;
} 