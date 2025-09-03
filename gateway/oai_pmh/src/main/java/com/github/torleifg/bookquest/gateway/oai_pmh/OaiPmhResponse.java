package com.github.torleifg.bookquest.gateway.oai_pmh;

import org.openarchives.oai._2.*;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

record OaiPmhResponse(OAIPMHtype oaipmHtype) {

    static OaiPmhResponse from(OAIPMHtype oaipmHtype) {
        return new OaiPmhResponse(oaipmHtype);
    }

    boolean hasErrors() {
        return !oaipmHtype.getError().isEmpty();
    }

    boolean hasBadResumptionTokenError() {
        return hasError(oaipmHtype, OAIPMHerrorcodeType.BAD_RESUMPTION_TOKEN);
    }

    boolean hasNoRecordsMatchError() {
        return hasError(oaipmHtype, OAIPMHerrorcodeType.NO_RECORDS_MATCH);
    }

    private boolean hasError(OAIPMHtype response, OAIPMHerrorcodeType errorCodeType) {
        return response.getError().stream().anyMatch(error -> error.getCode() == errorCodeType);
    }

    boolean hasRecords() {
        return oaipmHtype.getListRecords() != null && !oaipmHtype.getListRecords().getRecord().isEmpty();
    }

    List<RecordType> getRecords() {
        return Stream.ofNullable(oaipmHtype.getListRecords())
                .map(ListRecordsType::getRecord)
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(this::getLastModified, Comparator.nullsFirst(Comparator.reverseOrder())))
                .toList();
    }

    private Instant getLastModified(RecordType oaiPmhRecord) {
        return Optional.ofNullable(oaiPmhRecord.getHeader())
                .map(HeaderType::getDatestamp)
                .filter(datestamp -> !datestamp.isBlank())
                .map(Instant::parse)
                .orElse(null);
    }

    Optional<String> getResumptionToken() {
        return Optional.ofNullable(oaipmHtype.getListRecords())
                .map(ListRecordsType::getResumptionToken)
                .map(ResumptionTokenType::getValue)
                .filter(token -> !token.isBlank());
    }

    String errorsToString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("List of OAI-PMH Errors:\n");

        for (final var oaiPmhError : oaipmHtype.getError()) {
            sb.append(oaiPmhError.getCode()).append(" --> ").append(oaiPmhError.getValue()).append("\n");
        }

        return sb.toString();
    }
}
