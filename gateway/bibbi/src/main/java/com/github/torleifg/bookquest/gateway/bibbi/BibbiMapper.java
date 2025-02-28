package com.github.torleifg.bookquest.gateway.bibbi;

import com.github.torleifg.bookquest.application.domain.Book;
import no.bs.bibliografisk.model.GetV1PublicationsHarvest200ResponsePublicationsInner;

interface BibbiMapper {
    Book from(GetV1PublicationsHarvest200ResponsePublicationsInner publication);

    Book from(String id);
}
