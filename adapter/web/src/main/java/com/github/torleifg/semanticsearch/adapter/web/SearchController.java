package com.github.torleifg.semanticsearch.adapter.web;

import com.github.torleifg.semanticsearch.book.domain.Book;
import com.github.torleifg.semanticsearch.book.service.BookService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class SearchController {
    private final BookService bookService;

    public SearchController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public String search(Model model) {
        model.addAttribute("searchType", null);
        model.addAttribute("results", bookService.semanticSimilarity());

        return "index";
    }

    @PostMapping
    public String search(Model model, @RequestParam(required = false) String query, @RequestParam String searchType) {
        final List<Book> results;

        if ("semantic".equals(searchType)) {
            if (query != null && !query.isBlank()) {
                model.addAttribute("query", query);
                results = bookService.semanticSearch(query);
            } else {
                results = bookService.semanticSimilarity();
            }

        } else {
            if (query != null && !query.isBlank()) {
                model.addAttribute("query", query);
                results = bookService.fullTextSearch(query);
            } else {
                results = new ArrayList<>();
            }
        }

        model.addAttribute("searchType", searchType);
        model.addAttribute("results", results);

        return "index";
    }
}
