package com.blog.exceed.mapper;

import com.blog.exceed.dao.ChatMessageDao;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface ChatMessageMapper {
    List<ChatMessageDao> selectAll();
    ChatMessageDao selectById(Long id);
    void insert(ChatMessageDao chatMessage);
    void update(ChatMessageDao chatMessage);
    void delete(Long id);
} 