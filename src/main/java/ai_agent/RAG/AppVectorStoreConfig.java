package ai_agent.RAG;

import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.List;

//向量数据配置（基于内存）
@Configuration
@ConditionalOnProperty(name = "app.rag.enabled", havingValue = "true")
public class AppVectorStoreConfig {
    @Resource
    private AppDocumentLoader appDocumentLoader;
    @Bean("appVectorStore")
    VectorStore appVectorStore(DashScopeEmbeddingModel dashscopeEmbeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel).build();
        List<Document> documentList = appDocumentLoader.loadMarkdowns();
        if (documentList.isEmpty()) {
            throw new IllegalStateException("未找到健康知识库文档，请检查 classpath:document/*.md");
        }
        simpleVectorStore.add(documentList);
        return simpleVectorStore;

    }

}
