package ai_agent.RAG;

import jakarta.annotation.Resource;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;
import java.util.Map;

@SpringBootTest
@Disabled("需要显式启用 PGVector 并提供可用数据库，作为手动集成测试运行")
class PgVectorStoreConfigTest {
    @Resource
    private PgVectorStore pgVectorStore;

    @Test
    void pgVectorStore() {
        List<Document> documents = List.of(
                new Document("RAG教程", Map.of("meta1", "meta1")),
                new Document("RAG学习大全"),
                new Document("规律运动有助于改善健康", Map.of("meta2", "meta2")));

// Add the documents to PGVector
        pgVectorStore.add(documents);

// Retrieve documents similar to a query
        List<Document> results = this.pgVectorStore.similaritySearch(SearchRequest.builder().query("运动").topK(5).build());
        Assertions.assertThat(results).isNotNull().isNotEmpty().hasSizeLessThanOrEqualTo(5);
    }
}
