package com.github.torleifg.semanticsearch.gateway.bibbi;

import com.github.torleifg.semanticsearch.book.service.MetadataDTO;
import no.bs.bibliografisk.model.GetV1PublicationsHarvest200ResponsePublicationsInner;

interface BibbiMapper {
    MetadataDTO from(GetV1PublicationsHarvest200ResponsePublicationsInner publication);

    MetadataDTO from(String id);
}
