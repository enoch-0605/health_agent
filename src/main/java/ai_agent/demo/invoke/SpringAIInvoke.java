package ai_agent.demo.invoke;

import jakarta.annotation.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "app.demo.spring-ai-invoke.enabled",
        havingValue = "true"
)
public class SpringAIInvoke implements CommandLineRunner {
    @Resource
    private ChatModel dashscopeChatModel;


    @Override
    public void run(String... args) throws Exception {
        var response = dashscopeChatModel.call(new Prompt("请给我一个减少久坐的入门建议。"));
        System.out.println(response);
    }
}
