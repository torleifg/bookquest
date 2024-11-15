package com.github.torleifg.semanticsearchonnx.book.repository;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class VectorRepository {
    private final VectorStore vectorStore;
    private final JdbcClient jdbcClient;

    public VectorRepository(VectorStore vectorStore, JdbcClient jdbcClient) {
        this.vectorStore = vectorStore;
        this.jdbcClient = jdbcClient;
    }

    public void save(Document document) {
        vectorStore.add(List.of(document));
    }

    public void delete(UUID vectorId) {
        vectorStore.delete(List.of(vectorId.toString()));
    }

    public List<Document> query(String query, int limit) {
        return vectorStore.similaritySearch(SearchRequest.defaults()
                .withQuery("query: " + query)
                .withTopK(limit));
    }

    public List<Document> passage(int limit) {
        final Optional<Document> randomDocument = jdbcClient.sql("""
                        select * from vector_store order by random() limit 1
                        """)
                .query((resultSet, rowNum) -> new Document(resultSet.getString("content")))
                .optional();

        if (randomDocument.isEmpty()) {
            return List.of();
        }

        return vectorStore.similaritySearch(SearchRequest.defaults()
                .withQuery("passage: " + randomDocument.get().getContent())
                .withSimilarityThreshold(0.8)
                .withTopK(limit));
    }
}
