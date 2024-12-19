package com.github.torleifg.semanticsearch.gateway.common;

import java.time.Instant;
import java.util.Optional;

public interface LastModifiedRepository {

    Optional<Instant> get(String serviceUri);

    void save(String serviceUri, Instant lastModified);
}
