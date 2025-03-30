package com.blog.exceed.controller;

import com.blog.exceed.dao.ChatMessageDao;
import com.blog.exceed.service.ChatMessageService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class ChatMessageController {
    // private static final Logger logger = LoggerFactory.getLogger(ChatMessageController.class);
    private final ChatMessageService chatMessageService;

    public ChatMessageController(ChatMessageService chatMessageService) {
        this.chatMessageService = chatMessageService;
    }

    @GetMapping("/models")
    public ResponseEntity<List<Map<String, Object>>> getModels() {
        return ResponseEntity.ok(chatMessageService.getModels());
    }

    @GetMapping
    public ResponseEntity<List<ChatMessageDao>> getAllMessages() {
        List<ChatMessageDao> messages = chatMessageService.getAllMessages();
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChatMessageDao> getMessageById(@PathVariable Long id) {
        ChatMessageDao message = chatMessageService.getMessageById(id);
        return message != null ? ResponseEntity.ok(message) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Void> saveMessage(@RequestBody ChatMessageDao chatMessage) {
        chatMessageService.saveMessage(chatMessage);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateMessage(@PathVariable Long id, @RequestBody ChatMessageDao chatMessage) {
        chatMessage.setId(id);
        chatMessageService.updateMessage(chatMessage);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        chatMessageService.deleteMessage(id);
        return ResponseEntity.ok().build();
    }
} 