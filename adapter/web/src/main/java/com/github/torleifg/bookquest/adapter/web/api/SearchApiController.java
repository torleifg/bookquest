package com.github.torleifg.bookquest.adapter.web.api;

import com.github.torleifg.bookquest.application.service.BookService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/search")
class SearchApiController {
    private final BookService bookService;
    private final SearchDetailMapper searchDetailMapper;

    SearchApiController(BookService bookService, SearchDetailMapper searchDetailMapper) {
        this.bookService = bookService;
        this.searchDetailMapper = searchDetailMapper;
    }

    @GetMapping("/latest")
    public List<SearchDetail> getLastModified(@RequestParam String language) {
        final Locale locale = Locale.forLanguageTag(language);

        return bookService.lastModified().stream()
                .map(book -> searchDetailMapper.from(book, locale))
                .toList();
    }

    @GetMapping
    public List<SearchDetail> search(@RequestParam(required = false) String query, @RequestParam String language) {
        final Locale locale = Locale.forLanguageTag(language);

        if (query != null && !query.isBlank()) {
            return bookService.hybridSearch(query).stream()
                    .map(book -> searchDetailMapper.from(book, locale))
                    .toList();
        }

        return bookService.semanticSimilarity().stream()
                .map(book -> searchDetailMapper.from(book, locale))
                .toList();
    }
}
