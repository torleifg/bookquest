package com.github.torleifg.bookquest.application.repository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public record ResumptionToken(String value, Instant modified) {

    public boolean isNotExpired(long ttl) {
        return !Instant.now().minus(ttl, ChronoUnit.MINUTES).isAfter(modified());
    }
}
