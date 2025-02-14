package com.github.torleifg.semanticsearch.book.service;

import com.github.torleifg.semanticsearch.book.domain.Book;
import com.github.torleifg.semanticsearch.book.repository.BookRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Slf4j
@Service
public class BookService {
    private final MetadataGateway metadataGateway;
    private final BookRepository bookRepository;
    private final SearchMapper searchMapper;

    public BookService(MetadataGateway metadataGateway, BookRepository bookRepository, SearchMapper searchMapper) {
        this.metadataGateway = metadataGateway;
        this.bookRepository = bookRepository;
        this.searchMapper = searchMapper;
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

    public List<SearchDTO> lastModified(Locale locale) {
        return bookRepository.lastModified(20).stream()
                .map(book -> searchMapper.from(book, locale))
                .toList();
    }

    public List<Book> fullTextSearch(String query) {
        return bookRepository.fullTextSearch(query, 20);
    }

    public List<Book> semanticSearch(String query) {
        return bookRepository.semanticSearch(query, 20);
    }

    public List<SearchDTO> hybridSearch(String query, Locale locale) {
        return bookRepository.hybridSearch(query, 30).stream()
                .map(book -> searchMapper.from(book, locale))
                .toList();
    }

    public List<SearchDTO> semanticSimilarity(Locale locale) {
        return bookRepository.semanticSimilarity(20)
                .stream()
                .map(book -> searchMapper.from(book, locale))
                .toList();
    }
}
