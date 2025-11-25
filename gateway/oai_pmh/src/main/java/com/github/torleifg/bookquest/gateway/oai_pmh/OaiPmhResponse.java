package com.github.torleifg.bookquest.gateway.oai_pmh;

import org.openarchives.oai._2.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

record OaiPmhResponse(OAIPMHtype oaipmHtype) {

    static OaiPmhResponse from(OAIPMHtype oaipmHtype) {
        return new OaiPmhResponse(oaipmHtype);
    }

    boolean hasUnrecoverableErrors() {
        return !oaipmHtype.getError().isEmpty() && hasNoneNoRecordsMatchError();
    }

    boolean hasRecords() {
        return hasNoneNoRecordsMatchError() && oaipmHtype.getListRecords() != null && !oaipmHtype.getListRecords().getRecord().isEmpty();
    }

    private boolean hasNoneNoRecordsMatchError() {
        return oaipmHtype.getError().stream()
                .noneMatch(error -> error.getCode() == OAIPMHerrorcodeType.NO_RECORDS_MATCH);
    }

    List<RecordType> getRecords() {
        return Stream.ofNullable(oaipmHtype.getListRecords())
                .map(ListRecordsType::getRecord)
                .flatMap(Collection::stream)
                .toList();
    }

    String getResumptionToken() {
        return Optional.ofNullable(oaipmHtype.getListRecords())
                .map(ListRecordsType::getResumptionToken)
                .map(ResumptionTokenType::getValue)
                .filter(token -> !token.isBlank())
                .orElse(null);
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
