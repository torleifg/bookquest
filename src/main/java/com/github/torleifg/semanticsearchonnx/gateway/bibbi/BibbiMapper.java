package com.github.torleifg.semanticsearchonnx.gateway.bibbi;

import com.github.torleifg.semanticsearchonnx.book.service.MetadataDTO;
import no.bs.bibliografisk.model.GetV1PublicationsHarvest200ResponsePublicationsInner;

interface BibbiMapper {
    MetadataDTO from(GetV1PublicationsHarvest200ResponsePublicationsInner publication);

    MetadataDTO from(String id);
}
