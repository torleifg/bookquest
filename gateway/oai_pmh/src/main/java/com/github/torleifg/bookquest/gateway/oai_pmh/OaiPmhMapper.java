package com.github.torleifg.bookquest.gateway.oai_pmh;

import com.github.torleifg.bookquest.application.domain.Book;
import org.marc4j.marc.Record;

interface OaiPmhMapper {
    Book from(String id, Record record);

    Book from(String id);
}
