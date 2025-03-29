package com.blog.exceed.service;

import com.blog.exceed.dto.ChatMessageDto;
import com.blog.exceed.entity.ChatMessage;
import com.blog.exceed.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {
    
    private final ChatMessageRepository chatMessageRepository;
    
    @Override
    public ChatMessageDto saveMessage(String content, String role) {
        ChatMessage message = new ChatMessage();
        message.setContent(content);
        message.setRole(role);
        ChatMessage savedMessage = chatMessageRepository.save(message);
        return convertToDto(savedMessage);
    }
    
    @Override
    public List<ChatMessageDto> getAllMessages() {
        return chatMessageRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    private ChatMessageDto convertToDto(ChatMessage message) {
        ChatMessageDto dto = new ChatMessageDto();
        dto.setId(message.getId());
        dto.setContent(message.getContent());
        dto.setRole(message.getRole());
        dto.setCreatedAt(message.getCreatedAt());
        return dto;
    }
} 