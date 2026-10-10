package ai_agent.agent.model;

/**
 * Agent状态
 */

public enum AgentState {
    /**
     * 空闲状态
     */
    IDLE,
    /**
     * 运行中
     */
    RUNNING,
    /**
     * 已完成
     */
    FINISHED,
    /**
     * 错误状态
     */
    ERROR;
}
