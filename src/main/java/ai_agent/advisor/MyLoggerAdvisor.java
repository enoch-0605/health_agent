package ai_agent.advisor;

import java.util.function.Function;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import reactor.core.publisher.Flux;

import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.model.ChatResponse;

@Slf4j
public class MyLoggerAdvisor implements CallAdvisor, StreamAdvisor {

	@Override
	public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
		logRequest(chatClientRequest);

		ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(chatClientRequest);

		logResponse(chatClientResponse);

		return chatClientResponse;
	}

	@Override
	public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest,
			StreamAdvisorChain streamAdvisorChain) {
		logRequest(chatClientRequest);

		Flux<ChatClientResponse> chatClientResponses = streamAdvisorChain.nextStream(chatClientRequest);

		return new ChatClientMessageAggregator().aggregateChatClientResponse(chatClientResponses, this::logResponse);
	}

	protected void logRequest(ChatClientRequest request) {
		log.info("AI request: {}", request.context());

	}

	protected void logResponse(ChatClientResponse chatClientResponse) {
		log.info("AI response: {}",chatClientResponse.context());
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public int getOrder() {
		return 100;
	}

	@Override
	public String toString() {
		return SimpleLoggerAdvisor.class.getSimpleName();
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {

		private Function<ChatClientRequest, String> requestToString;

		private Function<ChatResponse, String> responseToString;

		private int order = 0;

		private Builder() {
		}

		public Builder requestToString(Function<ChatClientRequest, String> requestToString) {
			this.requestToString = requestToString;
			return this;
		}

		public Builder responseToString(Function<ChatResponse, String> responseToString) {
			this.responseToString = responseToString;
			return this;
		}

		public Builder order(int order) {
			this.order = order;
			return this;
		}

		public SimpleLoggerAdvisor build() {
			return new SimpleLoggerAdvisor(this.requestToString, this.responseToString, this.order);
		}

	}

}
