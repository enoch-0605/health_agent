package ai_agent.agent;

import ai_agent.agent.model.AgentState;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/** 管理智能体状态、上下文和分步执行流程的基础类。 */
@Data
@Slf4j
public abstract class BaseAgent {

    private String name;
    private String systemPrompt;
    private String nextStepPrompt;
    private AgentState state = AgentState.IDLE;
    private int currentStep;
    private int maxSteps = 10;
    private ChatClient chatClient;
    private List<Message> messageList = new ArrayList<>();

    public String run(String userPrompt) {
        validateBeforeRun(userPrompt);
        state = AgentState.RUNNING;
        messageList.add(new UserMessage(userPrompt));
        List<String> results = new ArrayList<>();
        try {
            for (int i = 0; i < maxSteps && state == AgentState.RUNNING; i++) {
                currentStep = i + 1;
                log.info("{} executing step {}/{}", name, currentStep, maxSteps);
                results.add("Step " + currentStep + ": " + step());
            }
            if (state == AgentState.RUNNING) {
                state = AgentState.FINISHED;
                results.add("执行结束：达到最大步骤（" + maxSteps + "）");
            }
            return String.join("\n", results);
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("{} 执行失败", name, e);
            return "执行错误：" + e.getMessage();
        } finally {
            cleanup();
        }
    }

    /** 以 SSE 逐步返回智能体执行结果。每个请求应使用新的智能体实例。 */
    public SseEmitter runStream(String userPrompt) {
        SseEmitter emitter = new SseEmitter(300_000L);
        CompletableFuture.runAsync(() -> executeStream(userPrompt, emitter));
        emitter.onTimeout(() -> {
            state = AgentState.ERROR;
            cleanup();
            emitter.complete();
        });
        return emitter;
    }

    private void executeStream(String userPrompt, SseEmitter emitter) {
        try {
            validateBeforeRun(userPrompt);
            state = AgentState.RUNNING;
            messageList.add(new UserMessage(userPrompt));
            for (int i = 0; i < maxSteps && state == AgentState.RUNNING; i++) {
                currentStep = i + 1;
                String result = "Step " + currentStep + ": " + step();
                emitter.send(SseEmitter.event().name("step").data(result));
            }
            if (state == AgentState.RUNNING) {
                state = AgentState.FINISHED;
                emitter.send(SseEmitter.event().name("complete")
                        .data("执行结束：达到最大步骤（" + maxSteps + "）"));
            }
            emitter.complete();
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("{} 流式执行失败", name, e);
            try {
                emitter.send(SseEmitter.event().name("error").data("执行错误：" + e.getMessage()));
                emitter.complete();
            } catch (IOException sendError) {
                emitter.completeWithError(sendError);
            }
        } finally {
            cleanup();
        }
    }

    private void validateBeforeRun(String userPrompt) {
        if (state != AgentState.IDLE) {
            throw new IllegalStateException("无法从状态 " + state + " 启动智能体");
        }
        if (StrUtil.isBlank(userPrompt)) {
            throw new IllegalArgumentException("用户问题不能为空");
        }
        if (chatClient == null) {
            throw new IllegalStateException("ChatClient 尚未初始化");
        }
    }

    public abstract String step();

    protected void cleanup() {
        // 子类按需释放资源
    }
}
