package com.github.torleifg.semanticsearch.adapter.persistence;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

class TestcontainersConfig {

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> pgvector() {
        return new PostgreSQLContainer<>("pgvector/pgvector:pg17");
    }
}
