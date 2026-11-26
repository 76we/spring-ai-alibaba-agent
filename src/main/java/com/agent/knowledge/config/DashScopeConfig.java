package com.agent.knowledge.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DashScopeConfig {
    @Bean
    public ChatModel chatModel(){
        DashScopeApi api = DashScopeApi.builder()
                .apiKey(System.getenv("DeepseekAPIKey"))
                .build();
        return DashScopeChatModel.builder()
                .dashScopeApi(api)
                .build();
    }

    @Bean
    public ChatClient chatClient(ChatModel chatModel){
        return ChatClient.builder(chatModel).build();
    }
}
