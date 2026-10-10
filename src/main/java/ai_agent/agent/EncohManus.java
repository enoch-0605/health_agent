package ai_agent.agent;
import ai_agent.advisor.MyLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;

/** 健康生活领域的自主规划智能体。每次请求创建一个新实例。 */
public class EncohManus extends ToolCallAgent {
    public EncohManus(ToolCallback[] allTools, ChatModel dashscopeChatModel) {
        super(allTools);
        this.setName("healthManus");
        String SYSTEM_PROMPT = """
                你是一名健康生活规划智能体，帮助用户把饮食、运动、睡眠和压力管理目标拆成安全、可执行的小步骤。
                你只提供一般生活方式信息，不诊断疾病、不调整药物，也不承诺治疗效果。
                如果出现胸痛、严重呼吸困难、意识异常、卒中征象、自伤或伤人风险等紧急情况，
                应立即建议用户联系当地急救服务，不再执行普通健康计划。
                """;
        this.setSystemPrompt(SYSTEM_PROMPT);
        String NEXT_STEP_PROMPT = """
                根据用户目标选择必要的工具。只有在信息足够且工具确有帮助时才调用工具。
                先确认会影响安全的重要限制，再给出低门槛的一周计划和追踪方式。
                BMI 等计算结果只能作为成人粗略筛查信息，必须说明局限，不能据此诊断。
                完成任务或无法安全继续时，调用 doTerminate 工具结束。
                """;
        this.setNextStepPrompt(NEXT_STEP_PROMPT);
        this.setMaxSteps(20);
        // 初始化 AI 对话客户端
        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(new MyLoggerAdvisor())
                .build();
        this.setChatClient(chatClient);
    }


}
