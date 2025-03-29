package com.blog.exceed.controller;

import com.blog.exceed.entity.ChatMessage;
import com.blog.exceed.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000") // React 개발 서버 주소
public class ChatController {
    
    private final ChatService chatService;
    
    @PostMapping("/messages")
    public ResponseEntity<ChatMessage> saveMessage(@RequestBody ChatMessage message) {
        ChatMessage savedMessage = chatService.saveMessage(message.getContent(), message.getRole());
        return ResponseEntity.ok(savedMessage);
    }
    
    @GetMapping("/messages")
    public ResponseEntity<List<ChatMessage>> getAllMessages() {
        List<ChatMessage> messages = chatService.getAllMessages();
        return ResponseEntity.ok(messages);
    }
} 