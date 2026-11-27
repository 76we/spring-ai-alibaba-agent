package com.agent.knowledge.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChatSessionService {
    private static final String PREFIX = "chat:session";
    private static final int MAX_MESSAGES = 12;
    private static final Duration TTL = Duration.ofDays(7);

    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;

    public ChatSessionService(StringRedisTemplate redis, ObjectMapper mapper) {
        this.redis = redis;
        this.mapper = mapper;
    }

    public record StoredMsg(String role, String text) {

    }

    public List<Message> getHistory(String sid) {
        List<StoredMsg> raw = readRaw(sid);
        List<Message> out = new ArrayList<>(raw.size());
        for (StoredMsg m : raw) {
            out.add("user".equals(m.role())
                    ? new UserMessage(m.text())
                    : new AssistantMessage(m.text()));
        }
        return out;
    }

    public void append(String sid, Message msg) {
        List<StoredMsg> raw = readRaw(sid);
        String role = (msg instanceof UserMessage) ? "user" : "assistant";
        raw.add(new StoredMsg(role, msg.getText()));
        if (raw.size() > MAX_MESSAGES) {
            raw.subList(0, raw.size() - MAX_MESSAGES).clear();
        }
        try {
            redis.opsForValue().set(PREFIX + sid, mapper.writeValueAsString(raw), TTL);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("会话序列失败", e);
        }
    }

    private List<StoredMsg> readRaw(String sid) {
        String json = redis.opsForValue().get(PREFIX + sid);
        if (json == null) {
            return new ArrayList<>();
        }
        try {
            return mapper.readValue(json,
                    new TypeReference<List<StoredMsg>>() {
                    });
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }
}
