package com.github.torleifg.bookquest.adapter.persistence;

import com.github.torleifg.bookquest.core.repository.LastModifiedRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

@Repository
class LastModifiedRepositoryAdapter implements LastModifiedRepository {
    private final JdbcClient jdbcClient;

    LastModifiedRepositoryAdapter(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Optional<Instant> get(String serviceUri) {
        return jdbcClient.sql("""
                        select value from last_modified where service = ?
                        """)
                .param(serviceUri)
                .query(Instant.class)
                .optional();
    }

    @Override
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
