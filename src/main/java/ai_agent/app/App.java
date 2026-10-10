package ai_agent.app;

import ai_agent.advisor.MyLoggerAdvisor;
import ai_agent.chatmemory.FileBasedChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 健康生活指导应用。
 *
 * <p>只提供一般性的生活方式建议，不替代医生诊断、处方或急救服务。</p>
 */
@Component
@Slf4j
public class App {

    private static final String SYSTEM_PROMPT = """
            你是一名谨慎、友善、循证的健康生活指导助手，帮助用户改善饮食、运动、睡眠、压力管理和日常习惯。
            回答时遵守以下规则：
            1. 先了解用户的目标、年龄段、日常作息、活动基础，以及已知疾病、伤病、孕产状态、过敏或用药等会影响建议的情况；不要索要姓名、证件号等无关隐私。
            2. 建议要具体、低门槛、可执行，优先给出一到三个本周就能开始的小步骤，并说明如何逐步增加。
            3. 不诊断疾病，不替代医生，不建议用户自行停药、换药或调整剂量；涉及疾病、孕产、未成年人、老年人或伤病时，明确建议咨询合格医疗专业人员。
            4. 不承诺快速减重、治愈或确定疗效，不推荐极端饮食、危险训练、排毒方案或来源不明的补充剂。
            5. 若用户描述胸痛、严重呼吸困难、意识异常、卒中征象、自伤/伤人风险或其他可能危及生命的情况，应停止普通生活建议，提示立即联系当地急救服务并寻求身边人员帮助。
            6. 清楚区分通用原则与个体化建议；信息不足时先提问，避免编造。
            默认使用中文回答，结构清晰，语气支持而不评判。
            """;

    private final ChatClient chatClient;
    private final VectorStore appVectorStore;
    private final ToolCallback[] allTools;

    public App(ChatModel dashscopeChatModel,
               @Qualifier("appVectorStore") ObjectProvider<VectorStore> vectorStoreProvider,
               ObjectProvider<ToolCallback[]> toolCallbacksProvider) {
        this.appVectorStore = vectorStoreProvider.getIfAvailable();
        this.allTools = toolCallbacksProvider.getIfAvailable(() -> new ToolCallback[0]);

        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
        ChatMemory chatMemory = new FileBasedChatMemory(fileDir, 20);
        this.chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new MyLoggerAdvisor()
                )
                .build();
    }

    /** 基础多轮对话。 */
    public String doChat(String message, String conversationId) {
        validateInput(message, conversationId);
        ChatResponse response = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("health chat completed, conversationId={}", conversationId);
        return content;
    }

    /** 基础多轮对话（SSE 流式输出）。 */
    public Flux<String> doChatByStream(String message, String conversationId) {
        validateInput(message, conversationId);
        return chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content();
    }

    public record HealthReport(
            String title,
            String summary,
            List<String> weeklyGoals,
            List<String> dietSuggestions,
            List<String> exerciseSuggestions,
            List<String> sleepSuggestions,
            List<String> precautions
    ) {
    }

    /** 生成结构化健康生活计划。 */
    public HealthReport doChatWithReport(String message, String conversationId) {
        validateInput(message, conversationId);
        HealthReport report = chatClient.prompt()
                .system(SYSTEM_PROMPT + """

                        请生成结构化健康生活计划。title 使用“健康生活计划”；summary 简要复述用户目标和限制；
                        weeklyGoals 只放 1-3 个可衡量的小目标；其余建议保持温和、可执行；
                        precautions 必须列出适用限制、需要专业评估的情况或急症提醒，不能写诊断结论。
                        """)
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .entity(HealthReport.class);
        log.info("health report generated, conversationId={}", conversationId);
        return report;
    }

    /** 使用本地健康知识库进行问答。 */
    public String doChatWithRag(String message, String conversationId) {
        validateInput(message, conversationId);
        if (appVectorStore == null) {
            throw new IllegalStateException(
                    "RAG 未启用，请设置环境变量 AI_AGENT_RAG_ENABLED=true 并配置 DashScope API Key");
        }
        ChatResponse response = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .advisors(QuestionAnswerAdvisor.builder(appVectorStore).build())
                .call()
                .chatResponse();
        return response.getResult().getOutput().getText();
    }

    /** 允许模型调用经过注册的健康领域工具。 */
    public String doChatWithTools(String message, String conversationId) {
        validateInput(message, conversationId);
        ChatClient.ChatClientRequestSpec request = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, conversationId));
        if (allTools.length > 0) {
            request.toolCallbacks(allTools);
        }
        return request.call().content();
    }

    private static void validateInput(String message, String conversationId) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message 不能为空");
        }
        if (conversationId == null || conversationId.isBlank()) {
            throw new IllegalArgumentException("conversationId 不能为空");
        }
    }
}
