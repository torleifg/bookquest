package com.github.torleifg.bookquest.application.service;

import com.github.torleifg.bookquest.application.domain.Book;
import com.github.torleifg.bookquest.application.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MetadataService {
    private final MetadataGateway metadataGateway;
    private final BookRepository bookRepository;

    public MetadataService(MetadataGateway metadataGateway, BookRepository bookRepository) {
        this.metadataGateway = metadataGateway;
        this.bookRepository = bookRepository;
    }

    @Transactional
    public boolean findAndSave() {
        final List<Book> books = metadataGateway.find();

        if (books.isEmpty()) {
            return false;
        }

        bookRepository.save(books);

        return true;
    }
}
