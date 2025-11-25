package com.github.torleifg.bookquest.core.service;

import com.github.torleifg.bookquest.core.domain.Book;

import java.time.Instant;
import java.util.List;

public record GatewayResponse(String requestUri, List<Book> books, String resumptionToken, Instant lastModified) {
}
