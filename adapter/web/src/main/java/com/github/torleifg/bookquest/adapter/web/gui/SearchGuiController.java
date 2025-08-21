package com.github.torleifg.bookquest.adapter.web.gui;

import com.github.torleifg.bookquest.core.service.BookService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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

    @GetMapping
    public String lastModified(Model model, Locale locale) {
        final List<SearchView> dtos = bookService.lastModified().stream()
                .map(book -> searchViewMapper.from(book, locale))
                .toList();

        model.addAttribute("results", dtos);

        return "search";
    }

    @GetMapping("/change-language")
    public String changeLanguage() {
        return "redirect:/";
    }

    @GetMapping("/search")
    public String search(Model model, @RequestParam String query, Locale locale) {
        if (query == null || query.isBlank()) {
            return "redirect:/";
        }

        final int wordCount = query.trim().split("\\s+").length;

        final List<SearchView> dtos;

        if (wordCount > 3) {
            dtos = bookService.hybridSearch(query).stream()
                    .map(book -> searchViewMapper.from(book, locale))
                    .toList();
        } else {
            dtos = bookService.fullTextSearch(query).stream()
                    .map(book -> searchViewMapper.from(book, locale))
                    .toList();
        }

        model.addAttribute("query", query);
        model.addAttribute("results", dtos);

        return "search";
    }

    @GetMapping("/similar")
    public String similar(Model model, @RequestParam String isbn, Locale locale) {
        final List<SearchView> dtos = bookService.semanticSimilarity(isbn).stream()
                .map(book -> searchViewMapper.from(book, locale))
                .toList();

        model.addAttribute("results", dtos);

        return "search";
    }

    @ResponseBody
    @GetMapping("/autocomplete")
    public List<String> autocomplete(@RequestParam String term) {
        return bookService.autocomplete(term);
    }
}
