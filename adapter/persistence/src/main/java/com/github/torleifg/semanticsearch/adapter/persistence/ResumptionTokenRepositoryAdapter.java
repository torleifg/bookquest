package com.github.torleifg.semanticsearch.adapter.persistence;

import com.github.torleifg.semanticsearch.gateway.common.repository.ResumptionToken;
import com.github.torleifg.semanticsearch.gateway.common.repository.ResumptionTokenRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class ResumptionTokenRepositoryAdapter implements ResumptionTokenRepository {
    private final JdbcClient jdbcClient;

    ResumptionTokenRepositoryAdapter(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Optional<ResumptionToken> get(String serviceUri) {
        return jdbcClient.sql("""
                        select * from resumption_token where service = ?
                        """)
                .param(serviceUri)
                .query(ResumptionToken.class)
                .optional();
    }

    @Override
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

    @Override
    public void delete(String serviceUri) {
        jdbcClient.sql("""
                        delete from resumption_token where service = ?
                        """)
                .param(serviceUri)
                .update();
    }
}
