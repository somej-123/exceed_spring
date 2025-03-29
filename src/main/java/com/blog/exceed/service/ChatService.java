package com.blog.exceed.service;

import com.blog.exceed.entity.ChatMessage;
import com.blog.exceed.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    
    private final ChatMessageRepository chatMessageRepository;
    
    public ChatMessage saveMessage(String content, String role) {
        ChatMessage message = new ChatMessage();
        message.setContent(content);
        message.setRole(role);
        return chatMessageRepository.save(message);
    }
    
    public List<ChatMessage> getAllMessages() {
        return chatMessageRepository.findAll();
    }
} 