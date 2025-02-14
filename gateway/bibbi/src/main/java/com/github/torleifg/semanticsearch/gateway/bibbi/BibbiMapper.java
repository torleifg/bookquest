package com.github.torleifg.semanticsearch.gateway.bibbi;

import com.github.torleifg.semanticsearch.book.domain.Book;
import no.bs.bibliografisk.model.GetV1PublicationsHarvest200ResponsePublicationsInner;

interface BibbiMapper {
    Book from(GetV1PublicationsHarvest200ResponsePublicationsInner publication);

    Book from(String id);
}
