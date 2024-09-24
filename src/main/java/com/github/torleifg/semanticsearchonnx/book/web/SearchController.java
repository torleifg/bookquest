package com.github.torleifg.semanticsearchonnx.book.web;

import com.github.torleifg.semanticsearchonnx.book.service.BookService;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Controller
public class SearchController {
    private final BookService bookService;

    public SearchController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping(path = "/")
    public String search() {
        return "index";
    }

    @PostMapping(path = "/")
    public String searchRequest(Model model, @RequestParam(required = false) String query) {
        final List<Document> documents;

        if (isNotBlank(query)) {
            model.addAttribute("query", query);
            documents = bookService.search(query);
        } else {
            documents = bookService.passage();
        }

        final List<Map<String, Object>> results = new ArrayList<>();

        for (final Document document : documents) {
            results.add(document.getMetadata());
        }

        model.addAttribute("results", results);

        return "index";
    }
}
