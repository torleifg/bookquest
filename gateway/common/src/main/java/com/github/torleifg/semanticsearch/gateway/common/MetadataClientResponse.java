package com.github.torleifg.semanticsearch.gateway.common;

import java.util.Optional;

public record MetadataClientResponse<T>(Optional<String> resumptionToken, T body) {
}
