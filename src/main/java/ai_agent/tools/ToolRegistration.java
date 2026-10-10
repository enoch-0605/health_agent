package ai_agent.tools;

import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 集中注册允许大模型调用的工具。 */
@Configuration
public class ToolRegistration {

    @Bean
    public ToolCallback[] allTools() {
        return ToolCallbacks.from(
                new HealthMetricsTool(),
                new TerminateTool()
        );
    }
}
