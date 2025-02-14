package com.github.torleifg.semanticsearch.adapter.web;

import com.github.torleifg.semanticsearch.book.service.BookService;
import com.github.torleifg.semanticsearch.book.service.SearchDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Locale;

@Controller
class SearchController {
    private final BookService bookService;

    SearchController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public String search(Model model, Locale locale) {
        final List<SearchDTO> dtos = bookService.lastModified(locale);

        model.addAttribute("results", dtos);

        return "index";
    }

    @PostMapping
    public String search(Model model, @RequestParam(required = false) String query, Locale locale) {
        final List<SearchDTO> dtos;

        if (query != null && !query.isBlank()) {
            model.addAttribute("query", query);

            dtos = bookService.hybridSearch(query, locale);
        } else {
            dtos = bookService.semanticSimilarity(locale);
        }

        model.addAttribute("results", dtos);

        return "index";
    }
}
