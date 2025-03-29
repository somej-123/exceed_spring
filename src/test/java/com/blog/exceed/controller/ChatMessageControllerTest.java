package com.blog.exceed.controller;

import com.blog.exceed.dao.ChatMessageDao;
import com.blog.exceed.service.ChatMessageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ChatMessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChatMessageService chatMessageService;

    @Test
    @DisplayName("전체 메시지 조회 테스트")
    void getAllMessagesTest() throws Exception {
        // given
        ChatMessageDao message = new ChatMessageDao();
        message.setContent("테스트 메시지");
        message.setRole("user");
        message.setCreatedAt(LocalDateTime.now());
        chatMessageService.saveMessage(message);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/messages")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").exists())
                .andExpect(jsonPath("$[0].role").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("메시지 저장 테스트")
    void saveMessageTest() throws Exception {
        // given
        String content = "{\"content\":\"새로운 메시지\",\"role\":\"user\"}";

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("메시지 수정 테스트")
    void updateMessageTest() throws Exception {
        // given
        ChatMessageDao message = new ChatMessageDao();
        message.setContent("원본 메시지");
        message.setRole("user");
        message.setCreatedAt(LocalDateTime.now());
        chatMessageService.saveMessage(message);

        String content = "{\"content\":\"수정된 메시지\",\"role\":\"user\"}";

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.put("/api/messages/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("메시지 삭제 테스트")
    void deleteMessageTest() throws Exception {
        // given
        ChatMessageDao message = new ChatMessageDao();
        message.setContent("삭제할 메시지");
        message.setRole("user");
        message.setCreatedAt(LocalDateTime.now());
        chatMessageService.saveMessage(message);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/messages/1"))
                .andExpect(status().isOk())
                .andDo(print());
    }
} 