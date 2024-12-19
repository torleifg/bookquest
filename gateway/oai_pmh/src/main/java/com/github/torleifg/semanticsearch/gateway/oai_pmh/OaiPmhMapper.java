package com.github.torleifg.semanticsearch.gateway.oai_pmh;

import com.github.torleifg.semanticsearch.book.service.MetadataDTO;
import info.lc.xmlns.marcxchange_v1.RecordType;

interface OaiPmhMapper {
    MetadataDTO from(String id, RecordType record);

    MetadataDTO from(String id);
}
