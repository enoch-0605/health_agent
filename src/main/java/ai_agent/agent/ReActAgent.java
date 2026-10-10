package ai_agent.agent;

import lombok.extern.slf4j.Slf4j;

/**
 * ReAct 模式的代理抽象类
 * 实现思考行动的循环模式
 *
 */
@Slf4j
public abstract class ReActAgent extends BaseAgent{
    /**
     * 处理当前状态并决定下一步行动
     *
     * @return 是否执行行动
     */
    public  abstract  boolean think();
    /**
     * 执行决定的行动
     *
     * @return 执行行动结果
     */
    public abstract  String act();

    @Override
    public String step() {
        try {
            boolean shouldAct = think();
            if (!shouldAct){
                return "思考完成-无需行动";
            }
            //再行动
            return act();
        } catch (Exception e){
            log.error("{} 单步执行失败", getName(), e);
            throw new IllegalStateException("步骤执行失败：" + e.getMessage(), e);
        }
    }
}
