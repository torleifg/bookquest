package com.github.torleifg.semanticsearch.gateway.oai_pmh;

import com.github.torleifg.semanticsearch.book.service.MetadataDTO;
import org.marc4j.marc.Record;

interface OaiPmhMapper {
    MetadataDTO from(String id, Record record);

    MetadataDTO from(String id);
}
