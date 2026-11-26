package com.agent.knowledge;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 冒烟测试：不写接口，先验证「ChatClient -> 通义千问」最简调用链。
 * 前置：已配置环境变量 DeepseekAPIKey，且模型服务可访问。
 */
@SpringBootTest
class SmokeTest {

    @Autowired
    ChatClient chatClient;

    @Test
    void sayHello() {
        String text = chatClient.prompt()
                .user("用一句话介绍你自己")
                .call()
                .content();
        Assertions.assertNotNull(text);
        System.out.println(text);
    }
}
