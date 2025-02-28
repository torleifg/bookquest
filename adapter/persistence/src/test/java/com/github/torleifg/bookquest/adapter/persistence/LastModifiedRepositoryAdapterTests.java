package com.github.torleifg.bookquest.adapter.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ContextConfiguration;

import java.sql.Timestamp;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@Import(TestcontainersConfig.class)
@ContextConfiguration(classes = LastModifiedRepositoryAdapter.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LastModifiedRepositoryAdapterTests {

    @Autowired
    JdbcClient client;

    LastModifiedRepositoryAdapter adapter;

    @BeforeEach
    void init() {
        adapter = new LastModifiedRepositoryAdapter(client);

        client.sql("truncate table last_modified").update();
    }

    @Test
    void getLastModifiedTest() {
        var service = client.sql("insert into last_modified (service, value) values ('service', ?) returning service")
                .param(Timestamp.from(Instant.ofEpochSecond(10)))
                .query(String.class)
                .single();

        assertTrue(adapter.get(service).isPresent());
    }

    @Test
    void insertLastModifiedTest() {
        adapter.save("service", Instant.ofEpochSecond(10));

        var count = client.sql("select count(*) from last_modified")
                .query(Integer.class)
                .optional();

        assertTrue(count.isPresent());
        assertEquals(1, count.get());
    }

    @Test
    void updateLastModifiedTest() {
        var modified = Instant.ofEpochSecond(10);

        client.sql("insert into last_modified (service, value) values ('service', ?)")
                .param(Timestamp.from(modified))
                .update();

        var lastModified = modified.plusSeconds(10);

        adapter.save("service", lastModified);

        var token = client.sql("select value from last_modified where service = 'service'")
                .query(Instant.class)
                .optional();

        assertTrue(token.isPresent());
        assertEquals(lastModified, token.get());
    }
}
