package com.github.torleifg.bookquest.adapter.persistence;

import com.github.torleifg.bookquest.core.repository.ResumptionToken;
import com.github.torleifg.bookquest.core.repository.ResumptionTokenRepository;
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
                        select * from resumption_token where service = :serviceUrl
                        """)
                .param("serviceUrl", serviceUri)
                .query(ResumptionToken.class)
                .optional();
    }

    @Override
    public void save(String serviceUri, String token) {
        jdbcClient.sql("""
                        insert into resumption_token(service, value)
                        values (:serviceUrl, :token)
                        on conflict (service)
                        do update set (modified, value) =
                        (now(), excluded.value)
                        """)
                .param("serviceUrl", serviceUri)
                .param("token", token)
                .update();
    }

    @Override
    public void delete(String serviceUri) {
        jdbcClient.sql("""
                        delete from resumption_token where service = :serviceUrl
                        """)
                .param("serviceUrl", serviceUri)
                .update();
    }
}
