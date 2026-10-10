package ai_agent.controller;

import ai_agent.agent.EncohManus;
import ai_agent.app.App;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

/** 健康生活指导 API。 */
@RestController
@RequestMapping("/ai/health")
public class AiController {

    private final App healthApp;
    private final ToolCallback[] allTools;
    private final ChatModel dashscopeChatModel;

    public AiController(App healthApp, ToolCallback[] allTools, ChatModel dashscopeChatModel) {
        this.healthApp = healthApp;
        this.allTools = allTools;
        this.dashscopeChatModel = dashscopeChatModel;
    }

    @PostMapping("/chat")
    public String chat(@Valid @RequestBody ChatRequest request) {
        return healthApp.doChat(request.message(), request.conversationId());
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatByPostStream(@Valid @RequestBody ChatRequest request) {
        return healthApp.doChatByStream(request.message(), request.conversationId());
    }

    @PostMapping("/report")
    public App.HealthReport report(@Valid @RequestBody ChatRequest request) {
        return healthApp.doChatWithReport(request.message(), request.conversationId());
    }

    @PostMapping("/rag")
    public String rag(@Valid @RequestBody ChatRequest request) {
        return healthApp.doChatWithRag(request.message(), request.conversationId());
    }

    @PostMapping("/tools")
    public String chatWithTools(@Valid @RequestBody ChatRequest request) {
        return healthApp.doChatWithTools(request.message(), request.conversationId());
    }

    @GetMapping(value = "/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatByStream(@RequestParam String message,
                                     @RequestParam String conversationId) {
        return healthApp.doChatByStream(message, conversationId);
    }

    @GetMapping(value = "/chat/server-sent-event", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatByServerSentEvent(@RequestParam String message,
                                                               @RequestParam String conversationId) {
        return healthApp.doChatByStream(message, conversationId)
                .map(chunk -> ServerSentEvent.builder(chunk).event("message").build());
    }

    @GetMapping(value = "/chat/sse-emitter", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatBySseEmitter(@RequestParam String message,
                                       @RequestParam String conversationId) {
        SseEmitter emitter = new SseEmitter(180_000L);
        healthApp.doChatByStream(message, conversationId)
                .subscribe(chunk -> send(emitter, chunk), emitter::completeWithError, emitter::complete);
        return emitter;
    }

    @GetMapping(value = "/agent", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter runHealthAgent(@RequestParam String message) {
        EncohManus agent = new EncohManus(allTools, dashscopeChatModel);
        return agent.runStream(message);
    }

    private static void send(SseEmitter emitter, String chunk) {
        try {
            emitter.send(chunk);
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }

    public record ChatRequest(
            @NotBlank(message = "message 不能为空") String message,
            @NotBlank(message = "conversationId 不能为空")
            @Pattern(regexp = "[A-Za-z0-9_-]{1,100}", message = "conversationId 格式不正确")
            String conversationId) {
    }
}
