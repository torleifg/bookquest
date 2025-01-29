package com.github.torleifg.semanticsearch.adapter.web;

import com.github.torleifg.semanticsearch.book.domain.Book;
import com.github.torleifg.semanticsearch.book.service.BookService;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${language}")
    private String langauge;

    SearchController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public String search(Model model) {
        final List<Book> books = bookService.lastModified();

        for (final Book book : books) {
            book.getMetadata().languageFilter(langauge);
        }

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

        } else if ("full-text".equals(searchType)) {
            if (query != null && !query.isBlank()) {
                model.addAttribute("query", query);
                books = bookService.fullTextSearch(query);
            } else {
                books = new ArrayList<>();
            }
        } else {
            if (query != null && !query.isBlank()) {
                model.addAttribute("query", query);
                books = bookService.hybridSearch(query);
            } else {
                books = new ArrayList<>();
            }
        }

        for (final Book book : books) {
            book.getMetadata().languageFilter(langauge);
        }

        model.addAttribute("searchType", searchType);
        model.addAttribute("results", books);

        return "index";
    }
}
