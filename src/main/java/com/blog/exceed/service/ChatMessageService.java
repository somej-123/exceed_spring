package com.blog.exceed.service;

import com.blog.exceed.dao.ChatMessageDao;
import com.blog.exceed.mapper.ChatMessageMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ChatMessageService {
    private final ChatMessageMapper chatMessageMapper;

    public ChatMessageService(ChatMessageMapper chatMessageMapper) {
        this.chatMessageMapper = chatMessageMapper;
    }

    public List<ChatMessageDao> getAllMessages() {
        return chatMessageMapper.selectAll();
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