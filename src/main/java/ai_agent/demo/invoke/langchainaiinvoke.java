package ai_agent.demo.invoke;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;

public class langchainaiinvoke {
    public static void main(String[] args) {
        QwenChatModel qwenChatModel = QwenChatModel.builder()
                .apiKey(tastAPIKey.API_KEY)
                .modelName("qwen-max")
                .build();
        String chat = qwenChatModel.chat("请给我一个减少久坐的入门建议。");
        System.out.print(chat);

    }
}
