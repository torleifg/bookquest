package com.github.torleifg.bookquest.adapter.web.gui;

import com.github.torleifg.bookquest.core.domain.Suggestion;
import com.github.torleifg.bookquest.core.service.BookService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Locale;

@Controller
class SearchGuiController {
    @Value("${reciprocal-rank-fusion.keyword-full-text-search-weight}")
    private double keywordFullTextSearchWeight;

    @Value("${reciprocal-rank-fusion.keyword-semantic-search-weight}")
    private double keywordSemanticSearchWeight;

    private final BookService bookService;
    private final SearchViewMapper searchViewMapper;

    SearchGuiController(BookService bookService, SearchViewMapper searchViewMapper) {
        this.bookService = bookService;
        this.searchViewMapper = searchViewMapper;
    }

    @GetMapping
    public String latest(Model model, Locale locale, @RequestParam(required = false) String genre) {
        final List<SearchView> views = bookService.latest(genre).stream()
                .map(book -> searchViewMapper.from(book, locale))
                .toList();

        model.addAttribute("results", views);

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

        final List<SearchView> views;

        if (isKeywordQuery(query)) {
            views = bookService.hybridSearch(query, keywordFullTextSearchWeight, keywordSemanticSearchWeight).stream()
                    .map(book -> searchViewMapper.from(book, locale))
                    .toList();
        } else {
            views = bookService.hybridSearch(query).stream()
                    .map(book -> searchViewMapper.from(book, locale))
                    .toList();
        }

        model.addAttribute("query", query);
        model.addAttribute("results", views);

        return "search";
    }

    @GetMapping("/similar")
    public String similar(Model model, @RequestParam String isbn, Locale locale) {
        final List<SearchView> views = bookService.semanticSimilarity(isbn).stream()
                .map(book -> searchViewMapper.from(book, locale))
                .toList();

        model.addAttribute("results", views);

        return "search";
    }

    @ResponseBody
    @GetMapping("/autocomplete")
    public List<Suggestion> autocomplete(@RequestParam String term) {
        return bookService.autocomplete(term);
    }

    private boolean isKeywordQuery(String query) {
        return query.trim().split("\\s+").length <= 3;
    }
}
