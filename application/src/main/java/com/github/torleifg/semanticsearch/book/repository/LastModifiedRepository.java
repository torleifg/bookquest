package com.github.torleifg.semanticsearch.book.repository;

import java.time.Instant;
import java.util.Optional;

public interface LastModifiedRepository {

    Optional<Instant> get(String serviceUri);

    void save(String serviceUri, Instant lastModified);
}
