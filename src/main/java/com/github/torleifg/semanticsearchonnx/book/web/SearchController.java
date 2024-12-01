package com.github.torleifg.semanticsearchonnx.book.web;

import com.github.torleifg.semanticsearchonnx.book.domain.Book;
import com.github.torleifg.semanticsearchonnx.book.service.BookService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Controller
public class SearchController {
    private final BookService bookService;

    public SearchController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public String search(Model model) {
        model.addAttribute("searchType", null);
        model.addAttribute("results", bookService.passage());

        return "index";
    }

    @PostMapping
    public String search(Model model, @RequestParam(required = false) String query, @RequestParam String searchType) {
        final List<Book> results;

        if ("semantic".equals(searchType)) {
            if (isNotBlank(query)) {
                model.addAttribute("query", query);
                results = bookService.semanticSearch(query);
            } else {
                results = bookService.passage();
            }

        } else {
            if (isNotBlank(query)) {
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
