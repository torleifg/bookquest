package com.github.torleifg.semanticsearchonnx.gateway.bokbasen;

import com.github.torleifg.semanticsearchonnx.book.domain.Book;
import org.editeur.ns.onix._3_0.reference.Product;

public interface BokbasenMapper {
    Book from(Product product);
    Book from(String id);
}
