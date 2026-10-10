package ai_agent.chatmemory;

/**
 * @deprecated 请使用命名规范且可配置窗口大小的 {@link FileBasedChatMemory}。
 */
@Deprecated
public class filebaseChatMemory extends FileBasedChatMemory {

    public filebaseChatMemory(String directory) {
        super(directory, 10);
    }
}
