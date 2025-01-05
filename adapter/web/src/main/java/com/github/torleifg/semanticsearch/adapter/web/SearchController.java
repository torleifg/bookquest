package com.github.torleifg.semanticsearch.adapter.web;

import com.github.torleifg.semanticsearch.book.domain.Book;
import com.github.torleifg.semanticsearch.book.domain.Metadata;
import com.github.torleifg.semanticsearch.book.service.BookService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
class SearchController {
    private final BookService bookService;

    SearchController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public String search(Model model) {
        final List<Book> books = bookService.lastModified();

        filter(books, "nob");

        model.addAttribute("searchType", null);
        model.addAttribute("results", books);

        return "index";
    }

    @PostMapping
    public String search(Model model, @RequestParam(required = false) String query, @RequestParam String searchType) {
        final List<Book> books;

        if ("semantic".equals(searchType)) {
            if (query != null && !query.isBlank()) {
                model.addAttribute("query", query);
                books = bookService.semanticSearch(query);
            } else {
                books = bookService.semanticSimilarity();
            }

        } else {
            if (query != null && !query.isBlank()) {
                model.addAttribute("query", query);
                books = bookService.fullTextSearch(query);
            } else {
                books = new ArrayList<>();
            }
        }

        filter(books, "nob");

        model.addAttribute("searchType", searchType);
        model.addAttribute("results", books);

        return "index";
    }

    private static void filter(List<Book> books, String language) {
        for (final Book book : books) {
            final Metadata metadata = book.getMetadata();

            metadata.getAbout().removeIf(classification -> !classification.language().equals(language));
            metadata.getGenreAndForm().removeIf(classification -> !classification.language().equals(language));
        }
    }
}
