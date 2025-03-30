package com.blog.exceed.service;

import com.blog.exceed.dao.ChatMessageDao;
import com.blog.exceed.mapper.ChatMessageMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class ChatMessageService {
    private final ChatMessageMapper chatMessageMapper;

    @Value("${ai.api.url}")
    private String aiApiUrl;

    @Autowired
    RestTemplate restTemplate;

    public ChatMessageService(ChatMessageMapper chatMessageMapper) {
        this.chatMessageMapper = chatMessageMapper;
    }

    public List<ChatMessageDao> getAllMessages() {
        return chatMessageMapper.selectAll();
    }

    public List<Map<String, Object>> getModels() {
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
            aiApiUrl,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );
        return response.getBody();
    }

    public ChatMessageDao getMessageById(Long id) {
        return chatMessageMapper.selectById(id);
    }

    public void saveMessage(ChatMessageDao chatMessage) {
        chatMessageMapper.insert(chatMessage);
    }

    public void updateMessage(ChatMessageDao chatMessage) {
        chatMessageMapper.update(chatMessage);
    }

    public void deleteMessage(Long id) {
        chatMessageMapper.delete(id);
    }
} 