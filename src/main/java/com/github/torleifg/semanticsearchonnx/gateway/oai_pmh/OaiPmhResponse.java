package com.github.torleifg.semanticsearchonnx.gateway.oai_pmh;

import org.apache.commons.lang3.StringUtils;
import org.openarchives.oai._2.*;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

class OaiPmhResponse {
    private final OAIPMHtype oaipmHtype;

    private OaiPmhResponse(OAIPMHtype oaipmHtype) {
        this.oaipmHtype = oaipmHtype;
    }

    public static OaiPmhResponse from(OAIPMHtype oaipmHtype) {
        return new OaiPmhResponse(oaipmHtype);
    }

    public boolean hasErrors() {
        return !oaipmHtype.getError().isEmpty();
    }

    public boolean hasBadResumptionTokenError() {
        return hasError(oaipmHtype, OAIPMHerrorcodeType.BAD_RESUMPTION_TOKEN);
    }

    public boolean hasNoRecordsMatchError() {
        return hasError(oaipmHtype, OAIPMHerrorcodeType.NO_RECORDS_MATCH);
    }

    private boolean hasError(OAIPMHtype response, OAIPMHerrorcodeType errorCodeType) {
        return response.getError().stream().anyMatch(error -> error.getCode() == errorCodeType);
    }

    public boolean hasRecords() {
        return oaipmHtype.getListRecords() != null && !oaipmHtype.getListRecords().getRecord().isEmpty();
    }

    public List<RecordType> getRecords() {
        return Stream.ofNullable(oaipmHtype.getListRecords())
                .map(ListRecordsType::getRecord)
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(this::getLastModified))
                .toList();
    }

    private Instant getLastModified(org.openarchives.oai._2.RecordType oaiPmhRecord) {
        return Optional.ofNullable(oaiPmhRecord.getHeader())
                .map(HeaderType::getDatestamp)
                .map(Instant::parse)
                .orElseThrow();
    }

    public Optional<String> getResumptionToken() {
        return Optional.ofNullable(oaipmHtype.getListRecords())
                .map(ListRecordsType::getResumptionToken)
                .map(ResumptionTokenType::getValue)
                .filter(StringUtils::isNotBlank);
    }

    public String errorsToString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("List of OAI-PMH Errors:\n");

        for (final var oaiPmhError : oaipmHtype.getError()) {
            sb.append(oaiPmhError.getCode()).append(" --> ").append(oaiPmhError.getValue()).append("\n");
        }

        return sb.toString();
    }
}
