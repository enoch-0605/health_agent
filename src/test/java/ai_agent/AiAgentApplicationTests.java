package ai_agent;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
        "app.rag.enabled=false",
        "app.rag.pgvector.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
})
class AiAgentApplicationTests {

    @MockitoBean
    private ChatModel chatModel;

    @Test
    void contextLoads() {
    }
}
