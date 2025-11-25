package com.github.torleifg.bookquest.gateway.bibbi;

import no.bs.bibliografisk.model.GetV1PublicationsHarvest200Response;
import no.bs.bibliografisk.model.GetV1PublicationsHarvest200ResponsePublicationsInner;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

record BibbiResponse(GetV1PublicationsHarvest200Response getV1PublicationsHarvest200Response) {

    static BibbiResponse from(GetV1PublicationsHarvest200Response response) {
        return new BibbiResponse(response);
    }

    String getResumptionToken() {
        return Optional.ofNullable(getV1PublicationsHarvest200Response.getResumptionToken())
                .filter(StringUtils::isNotBlank)
                .orElse(null);
    }

    boolean hasPublications() {
        return getV1PublicationsHarvest200Response.getPublications() != null && !getV1PublicationsHarvest200Response.getPublications().isEmpty();
    }

    List<GetV1PublicationsHarvest200ResponsePublicationsInner> getPublications() {
        return Stream.ofNullable(getV1PublicationsHarvest200Response.getPublications())
                .flatMap(Collection::stream)
                .toList();
    }
}
