package com.blog.exceed.service;

import com.blog.exceed.dto.ChatMessageDto;
import java.util.List;

public interface ChatMessageService {
    ChatMessageDto saveMessage(String content, String role);
    List<ChatMessageDto> getAllMessages();
} 