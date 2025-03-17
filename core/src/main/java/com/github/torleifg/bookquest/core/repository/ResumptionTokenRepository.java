package com.github.torleifg.bookquest.core.repository;

import java.util.Optional;

public interface ResumptionTokenRepository {

    Optional<ResumptionToken> get(String serviceUri);

    void save(String serviceUri, String token);

    void delete(String serviceUri);
}
