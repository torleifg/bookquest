package com.github.torleifg.semanticsearchonnx.gateway.repository;

import com.github.torleifg.semanticsearchonnx.TestcontainersConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@Import(TestcontainersConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ResumptionTokenRepositoryTests {

    @Autowired
    JdbcClient client;

    ResumptionTokenRepository repository;

    @BeforeEach
    void init() {
        repository = new ResumptionTokenRepository(client);
    }

    @Test
    void getResumptionTokenTest() {
        var service = client.sql("insert into resumption_token (service, value) values ('service', 'value') returning service")
                .query(String.class)
                .single();

        assertTrue(repository.get(service).isPresent());
    }

    @Test
    void insertResumptionTokenTest() {
        repository.save("service", "value");

        var count = client.sql("select count(*) from resumption_token")
                .query(Integer.class)
                .optional();

        assertTrue(count.isPresent());
        assertEquals(1, count.get());
    }

    @Test
    void updateResumptionTokenTest() {
        client.sql("insert into resumption_token (service, value) values ('service', 'value')")
                .update();

        repository.save("service", "new value");

        var token = client.sql("select value from resumption_token where service = 'service'")
                .query(String.class)
                .optional();

        assertTrue(token.isPresent());
        assertEquals("new value", token.get());
    }

    @Test
    void deleteResumptionTokenTest() {
        client.sql("insert into resumption_token (service, value) values ('service', 'value')")
                .update();

        repository.delete("service");

        var count = client.sql("select count(*) from resumption_token")
                .query(Integer.class)
                .optional();

        assertTrue(count.isPresent());
        assertEquals(0, count.get());
    }
}
