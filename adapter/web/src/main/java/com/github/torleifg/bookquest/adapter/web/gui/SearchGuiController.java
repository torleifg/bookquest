package com.github.torleifg.bookquest.adapter.web.gui;

import com.github.torleifg.bookquest.application.service.BookService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Locale;

@Controller
class SearchGuiController {
    private final BookService bookService;
    private final SearchViewMapper searchViewMapper;

    SearchGuiController(BookService bookService, SearchViewMapper searchViewMapper) {
        this.bookService = bookService;
        this.searchViewMapper = searchViewMapper;
    }

    @GetMapping("/change-language")
    public String changeLanguage() {
        return "redirect:/latest";
    }

    @GetMapping("/latest")
    public String lastModified(Model model, Locale locale) {
        final List<SearchView> dtos = bookService.lastModified().stream()
                .map(book -> searchViewMapper.from(book, locale))
                .toList();

        model.addAttribute("results", dtos);

        return "search";
    }

    @GetMapping
    public String search(Model model, @RequestParam(required = false) String query, Locale locale) {
        final List<SearchView> dtos;

        if (query != null && !query.isBlank()) {
            model.addAttribute("query", query);

            dtos = bookService.hybridSearch(query).stream()
                    .map(book -> searchViewMapper.from(book, locale))
                    .toList();
        } else {
            dtos = bookService.semanticSimilarity().stream()
                    .map(book -> searchViewMapper.from(book, locale))
                    .toList();
        }

        model.addAttribute("results", dtos);

        return "search";
    }
}
