package com.github.torleifg.semanticsearch.adapter.web;

import com.github.torleifg.semanticsearch.book.domain.Book;
import com.github.torleifg.semanticsearch.book.service.BookService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

        model.addAttribute("results", books);

        return "index";
    }

    @PostMapping
    public String search(Model model, @RequestParam(required = false) String query) {
        final List<Book> books;

        if (query != null && !query.isBlank()) {
            model.addAttribute("query", query);
            books = bookService.hybridSearch(query);
        } else {
            books = bookService.semanticSimilarity();
        }

        for (final Book book : books) {
            book.getMetadata().languageFilter(langauge);
        }

        model.addAttribute("results", books);

        return "index";
    }
}
