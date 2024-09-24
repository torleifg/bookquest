package com.github.torleifg.semanticsearchonnx.gateway.bibbi;

import com.github.torleifg.semanticsearchonnx.book.domain.Book;
import no.bs.bibliografisk.model.GetV1PublicationsHarvest200ResponsePublicationsInner;

interface BibbiMapper {
    Book from(GetV1PublicationsHarvest200ResponsePublicationsInner publication);
    Book from(String id);
}
