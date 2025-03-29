package com.blog.exceed.controller;

import com.blog.exceed.dto.ChatMessageDto;
import com.blog.exceed.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000") // React 개발 서버 주소
public class ChatController {
    
    private final ChatMessageService chatMessageService;
    
    @PostMapping("/messages")
    public ResponseEntity<ChatMessageDto> saveMessage(@RequestBody ChatMessageDto messageDto) {
        ChatMessageDto savedMessage = chatMessageService.saveMessage(messageDto.getContent(), messageDto.getRole());
        return ResponseEntity.ok(savedMessage);
    }
    
    @GetMapping("/messages")
    public ResponseEntity<List<ChatMessageDto>> getAllMessages() {
        List<ChatMessageDto> messages = chatMessageService.getAllMessages();
        return ResponseEntity.ok(messages);
    }
} 