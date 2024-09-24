package com.github.torleifg.semanticsearchonnx.gateway.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ResumptionTokenRepository {
    private final JdbcClient jdbcClient;

    public ResumptionTokenRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<ResumptionToken> get(String serviceUri) {
        return jdbcClient.sql("""
                        select * from resumption_token where service = ?
                        """)
                .param(serviceUri)
                .query(ResumptionToken.class)
                .optional();
    }

    public void save(String serviceUri, String token) {
        jdbcClient.sql("""
                        insert into resumption_token(service, value) values (?, ?)
                        on conflict (service)
                        do update set (modified, value) =
                        (now(), excluded.value)
                        """)
                .param(serviceUri)
                .param(token)
                .update();
    }

    public void delete(String serviceUri) {
        jdbcClient.sql("""
                        delete from resumption_token where service = ?
                        """)
                .param(serviceUri)
                .update();
    }
}
