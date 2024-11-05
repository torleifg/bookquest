package com.github.torleifg.semanticsearchonnx.gateway.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

@Repository
public class LastModifiedRepository {
    private final JdbcClient jdbcClient;

    public LastModifiedRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<Instant> get(String serviceUri) {
        return jdbcClient.sql("""
                        select value from last_modified where service = ?
                        """)
                .param(serviceUri)
                .query(Instant.class)
                .optional();
    }

    public void save(String serviceUri, Instant lastModified) {
        jdbcClient.sql("""
                        insert into last_modified(service, value) values (?, ?)
                        on conflict (service)
                        do update set (modified, value) =
                        (now(), excluded.value)
                        where excluded.value > last_modified.value
                        """)
                .param(serviceUri)
                .param(Timestamp.from(lastModified))
                .update();
    }
}
