package com.github.torleifg.bookquest.core.service;

import com.github.torleifg.bookquest.core.repository.ResumptionToken;

import java.time.Instant;
import java.util.Optional;

public record HarvestState(Optional<ResumptionToken> resumptionToken, Optional<Instant> lastModified) {
}
