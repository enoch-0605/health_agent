package ai_agent.chatmemory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.ai.chat.messages.UserMessage;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileBasedChatMemoryTest {

    @TempDir
    Path tempDir;

    @Test
    void persistsAndLimitsConversationWindow() {
        FileBasedChatMemory memory = new FileBasedChatMemory(tempDir.toString(), 2);
        memory.add("chat-1", new UserMessage("one"));
        memory.add("chat-1", new UserMessage("two"));
        memory.add("chat-1", new UserMessage("three"));

        FileBasedChatMemory reloaded = new FileBasedChatMemory(tempDir.toString(), 2);

        assertThat(reloaded.get("chat-1"))
                .extracting(message -> message.getText())
                .containsExactly("two", "three");
    }

    @Test
    void rejectsPathTraversalConversationIds() {
        FileBasedChatMemory memory = new FileBasedChatMemory(tempDir.toString(), 2);

        assertThatThrownBy(() -> memory.get("../../outside"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
