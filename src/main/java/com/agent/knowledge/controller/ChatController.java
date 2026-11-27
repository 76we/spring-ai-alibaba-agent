package com.agent.knowledge.controller;

import com.agent.knowledge.dto.ChatRequest;
import com.agent.knowledge.service.ChatSessionService;
import jakarta.validation.Valid;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;


@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatClient chatClient;

    private final ChatSessionService sessionService;

    public ChatController(ChatClient chatClient,ChatSessionService sessionService){
        this.chatClient=chatClient;
        this.sessionService=sessionService;
    }


    @PostMapping(value = "/stream",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestBody @Valid ChatRequest request){
        String sid = (request.sessionId()==null||request.sessionId().isBlank())
                ?"default":request.sessionId();
        List<Message> history = sessionService.getHistory(sid);
        history.add(new UserMessage(request.question()));

        StringBuilder answer = new StringBuilder();
        return chatClient.prompt()
                .messages(history)
                .stream()
                .content()
                .doOnNext(answer::append)
                .doOnComplete(()->sessionService.append(
                        sid,new AssistantMessage(answer.toString())
                ));
    }

 }
