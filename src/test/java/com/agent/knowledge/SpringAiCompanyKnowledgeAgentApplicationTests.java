package com.agent.knowledge;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Spring Boot 上下文加载测试。
 * 作用：验证 Spring 容器能正常启动、所有 Bean 能被装配，不真正发起外部 AI / 数据库调用。
 * 注意：若本机未启动 PostgreSQL / 未配置 DeepseekAPIKey，上下文加载可能失败，
 *       此时可先看下方的 PlainUnitTest 纯单元测试。
 */
@SpringBootTest
class SpringAiCompanyKnowledgeAgentApplicationTests {

    @Test
    void contextLoads() {
        // 走到这里且不抛异常，说明 Spring 上下文加载成功
    }
}
