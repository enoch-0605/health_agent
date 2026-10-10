package ai_agent.RAG;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import static org.assertj.core.api.Assertions.assertThat;

class AppDocumentLoaderTest {

    @Test
    void loadsHealthKnowledgeDocuments() {
        AppDocumentLoader loader = new AppDocumentLoader(new PathMatchingResourcePatternResolver());

        var documents = loader.loadMarkdowns();

        assertThat(documents).isNotEmpty();
        assertThat(documents)
                .anyMatch(document -> document.getText().contains("健康") || document.getText().contains("运动"));
    }
}
