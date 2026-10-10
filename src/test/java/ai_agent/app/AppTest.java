package ai_agent.app;

import ai_agent.AiAgentApplication;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;


@SpringBootTest(
        classes = AiAgentApplication.class,
        properties = "app.rag.enabled=true"
)
@EnabledIfSystemProperty(named = "ai.integration.enabled", matches = "true")
class AppTest {
    @Resource
    private App app;
    @Test
    void doChatWithReport() {
        String conversationId = UUID.randomUUID().toString();

        // 第一轮
        App.HealthReport response = app.doChatWithReport(
                "我久坐且睡眠不规律，希望制定一周改善计划。",
                conversationId
        );

        Assertions.assertNotNull(response);

    }


    @Test
    void doChatWithRAG() {
        String conversationId = UUID.randomUUID().toString();

        // 第一轮
        String response = app.doChatWithRag(
                "成年人每周应该如何逐步增加运动量？", conversationId
        );

        Assertions.assertNotNull(response);
    }
}
