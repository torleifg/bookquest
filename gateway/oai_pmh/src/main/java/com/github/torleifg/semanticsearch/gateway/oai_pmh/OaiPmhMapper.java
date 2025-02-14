package com.github.torleifg.semanticsearch.gateway.oai_pmh;

import com.github.torleifg.semanticsearch.book.domain.Book;
import org.marc4j.marc.Record;

interface OaiPmhMapper {
    Book from(String id, Record record);

    Book from(String id);
}
