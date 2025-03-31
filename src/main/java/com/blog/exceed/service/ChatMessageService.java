package com.blog.exceed.service;

import com.blog.exceed.dao.ChatMessageDao;
import com.blog.exceed.mapper.ChatMessageMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatMessageService {
    private final ChatMessageMapper chatMessageMapper;
    private final RestTemplate restTemplate;

    @Value("${ai.api.base-url}")
    private String aiApiBaseUrl;

    @Value("${ai.api.endpoints.chat}")
    private String chatEndpoint;

    @Value("${ai.api.endpoints.models}")
    private String modelsEndpoint;
    
    private static final Logger logger = LoggerFactory.getLogger(ChatMessageService.class);

    public ChatMessageService(ChatMessageMapper chatMessageMapper, RestTemplate restTemplate) {
        this.chatMessageMapper = chatMessageMapper;
        this.restTemplate = restTemplate;
    }

    // 모델 목록 조회
    public Map<String, Object> getModels() {
        String url = aiApiBaseUrl + modelsEndpoint;
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        return response.getBody();
        
    }

    // // 메세지 전송
    public Map<String, Object> sendMessage(Map<String, Object> requestBody) {

        // logger.info("userMessageObj의 타입: " + userMessageObj.getClass().getName());

        //모델명
        String modelName = (String) requestBody.get("model");

        // userMessage의 type이 linkedHashMap이므로 타입 변환
        @SuppressWarnings("unchecked")
        Map<String, Object> saveRequestBody = (Map<String, Object>) requestBody.get("userMessage");
        saveRequestBody.put("model", modelName);

        // 메시지 저장
        chatMessageMapper.saveMessage(saveRequestBody);

        // 봇 메시지 요청
        String url = aiApiBaseUrl + chatEndpoint;
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            new HttpEntity<>(requestBody),
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");

        @SuppressWarnings("unchecked")
        Map<String, Object> botMessage = (Map<String, Object>) choices.get(0).get("message");
        botMessage.put("model", modelName);

        // 봇 메시지 저장
        chatMessageMapper.saveMessage(botMessage);

        return response.getBody();
    }
    
    

    // public List<ChatMessageDao> getAllMessages() {
    //     return chatMessageMapper.selectAll();
    // }



    // public ChatMessageDao getMessageById(Long id) {
    //     return chatMessageMapper.selectById(id);
    // }

    // public void saveMessage(ChatMessageDao chatMessage) {
    //     chatMessageMapper.insert(chatMessage);
    // }

    // public void updateMessage(ChatMessageDao chatMessage) {
    //     chatMessageMapper.update(chatMessage);
    // }

    // public void deleteMessage(Long id) {
    //     chatMessageMapper.delete(id);
    // }
} 