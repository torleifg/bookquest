package com.github.torleifg.semanticsearchonnx.gateway.bokbasen;

import com.github.torleifg.semanticsearchonnx.book.service.MetadataDTO;
import org.editeur.ns.onix._3_0.reference.Product;

public interface BokbasenMapper {
    MetadataDTO from(Product product);

    MetadataDTO from(String id);
}
