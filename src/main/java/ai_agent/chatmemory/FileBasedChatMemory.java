package ai_agent.chatmemory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/** 按会话持久化到本地文件的 ChatMemory。 */
public class FileBasedChatMemory implements ChatMemory {

    private static final Kryo KRYO = createKryo();

    private final Path baseDir;
    private final int maxMessages;

    public FileBasedChatMemory(String directory, int maxMessages) {
        if (maxMessages <= 0) {
            throw new IllegalArgumentException("maxMessages 必须大于 0");
        }
        this.baseDir = Path.of(directory).toAbsolutePath().normalize();
        this.maxMessages = maxMessages;
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new IllegalStateException("无法创建对话记忆目录: " + baseDir, e);
        }
    }

    @Override
    public synchronized void add(String conversationId, Message message) {
        List<Message> messages = getOrCreateConversation(conversationId);
        messages.add(message);
        saveConversation(conversationId, trim(messages));
    }

    @Override
    public synchronized void add(String conversationId, List<Message> messages) {
        List<Message> history = getOrCreateConversation(conversationId);
        history.addAll(messages);
        saveConversation(conversationId, trim(history));
    }

    @Override
    public synchronized List<Message> get(String conversationId) {
        return List.copyOf(trim(getOrCreateConversation(conversationId)));
    }

    @Override
    public synchronized void clear(String conversationId) {
        try {
            Files.deleteIfExists(getConversationFile(conversationId));
        } catch (IOException e) {
            throw new IllegalStateException("无法清除对话记忆", e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Message> getOrCreateConversation(String conversationId) {
        Path file = getConversationFile(conversationId);
        if (!Files.isRegularFile(file)) {
            return new ArrayList<>();
        }
        try (Input input = new Input(Files.newInputStream(file))) {
            synchronized (KRYO) {
                return KRYO.readObject(input, ArrayList.class);
            }
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("无法读取对话记忆: " + file, e);
        }
    }

    private void saveConversation(String conversationId, List<Message> messages) {
        Path file = getConversationFile(conversationId);
        Path tempFile = file.resolveSibling(file.getFileName() + ".tmp");
        try (Output output = new Output(Files.newOutputStream(tempFile))) {
            synchronized (KRYO) {
                KRYO.writeObject(output, new ArrayList<>(messages));
            }
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("无法写入对话记忆: " + file, e);
        }
        try {
            Files.move(tempFile, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException atomicMoveError) {
            try {
                Files.move(tempFile, file, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new IllegalStateException("无法保存对话记忆: " + file, e);
            }
        }
    }

    private List<Message> trim(List<Message> messages) {
        int fromIndex = Math.max(0, messages.size() - maxMessages);
        return new ArrayList<>(messages.subList(fromIndex, messages.size()));
    }

    private Path getConversationFile(String conversationId) {
        if (conversationId == null || !conversationId.matches("[A-Za-z0-9_-]{1,100}")) {
            throw new IllegalArgumentException("conversationId 只能包含字母、数字、下划线和短横线，长度 1-100");
        }
        return baseDir.resolve(conversationId + ".kryo");
    }

    private static Kryo createKryo() {
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
        return kryo;
    }
}
