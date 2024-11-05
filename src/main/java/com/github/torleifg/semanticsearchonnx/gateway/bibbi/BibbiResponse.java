package com.github.torleifg.semanticsearchonnx.gateway.bibbi;

import no.bs.bibliografisk.model.BibliographicRecordMetadata;
import no.bs.bibliografisk.model.GetV1PublicationsHarvest200Response;
import no.bs.bibliografisk.model.GetV1PublicationsHarvest200ResponsePublicationsInner;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

class BibbiResponse {
    private final GetV1PublicationsHarvest200Response getV1PublicationsHarvest200Response;

    private BibbiResponse(GetV1PublicationsHarvest200Response getV1PublicationsHarvest200Response) {
        this.getV1PublicationsHarvest200Response = getV1PublicationsHarvest200Response;
    }

    public static BibbiResponse from(GetV1PublicationsHarvest200Response response) {
        return new BibbiResponse(response);
    }

    public Optional<String> getResumptionToken() {
        return Optional.ofNullable(getV1PublicationsHarvest200Response.getResumptionToken())
                .filter(StringUtils::isNotBlank);
    }

    public boolean hasPublications() {
        return getV1PublicationsHarvest200Response.getPublications() != null && !getV1PublicationsHarvest200Response.getPublications().isEmpty();
    }

    public List<GetV1PublicationsHarvest200ResponsePublicationsInner> getPublications() {
        return Stream.ofNullable(getV1PublicationsHarvest200Response.getPublications())
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(this::getLastModified))
                .toList();
    }

    private Instant getLastModified(GetV1PublicationsHarvest200ResponsePublicationsInner publication) {
        return Optional.ofNullable(publication.getBibliographicRecord())
                .map(BibliographicRecordMetadata::getModified)
                .map(OffsetDateTime::toInstant)
                .orElse(Instant.MIN);
    }
}
