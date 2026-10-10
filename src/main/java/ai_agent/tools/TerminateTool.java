package ai_agent.tools;

import org.springframework.ai.tool.annotation.Tool;

/** 让自主规划智能体在完成任务后显式结束。 */
public class TerminateTool {

    @Tool(description = "Terminate the interaction when the user's request is completed or cannot be continued safely.")
    public String doTerminate() {
        return "任务结束";
    }
}
