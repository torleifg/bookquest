package com.github.torleifg.semanticsearchonnx.gateway.oai_pmh;

import com.github.torleifg.semanticsearchonnx.book.domain.Book;
import info.lc.xmlns.marcxchange_v1.RecordType;

interface OaiPmhMapper {
    Book from(String id, RecordType record);
    Book from(String id);
}
