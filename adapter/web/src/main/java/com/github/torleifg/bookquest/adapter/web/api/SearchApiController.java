package com.github.torleifg.bookquest.adapter.web.api;

import com.github.torleifg.bookquest.application.service.BookService;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@Validated
@RestController
@RequestMapping("/api/search")
class SearchApiController {
    private final BookService bookService;
    private final SearchDetailMapper searchDetailMapper;

    SearchApiController(BookService bookService, SearchDetailMapper searchDetailMapper) {
        this.bookService = bookService;
        this.searchDetailMapper = searchDetailMapper;
    }

    @GetMapping
    public List<SearchDetail> hybridSearch(@RequestParam @NotBlank(message = "Required parameter 'query' is blank.") String query,
                                           @RequestParam @NotBlank(message = "Required parameter 'language' is blank.") String language) {
        final Locale locale = Locale.forLanguageTag(language);

        return bookService.hybridSearch(query).stream()
                .map(book -> searchDetailMapper.from(book, locale))
                .toList();
    }

    @GetMapping("/fullText")
    public List<SearchDetail> fullTextSearch(@RequestParam @NotBlank(message = "Required parameter 'query' is blank.") String query,
                                             @RequestParam @NotBlank(message = "Required parameter 'language' is blank.") String language) {
        final Locale locale = Locale.forLanguageTag(language);

        return bookService.fullTextSearch(query).stream()
                .map(book -> searchDetailMapper.from(book, locale))
                .toList();
    }

    @GetMapping("/semantic")
    public List<SearchDetail> semanticSearch(@RequestParam @NotBlank(message = "Required parameter 'query' is blank.") String query,
                                             @RequestParam @NotBlank(message = "Required parameter 'language' is blank.") String language) {
        final Locale locale = Locale.forLanguageTag(language);

        return bookService.semanticSearch(query).stream()
                .map(book -> searchDetailMapper.from(book, locale))
                .toList();
    }

    @GetMapping("/latest")
    public List<SearchDetail> lastModified(@RequestParam String language) {
        final Locale locale = Locale.forLanguageTag(language);

        return bookService.lastModified().stream()
                .map(book -> searchDetailMapper.from(book, locale))
                .toList();
    }

    @GetMapping("/similar")
    public List<SearchDetail> similar(@RequestParam @NotBlank(message = "Required parameter 'isbn' is blank.") String isbn,
                                      @RequestParam @NotBlank(message = "Required parameter 'language' is blank.") String language) {
        final Locale locale = Locale.forLanguageTag(language);

        return bookService.semanticSimilarity(isbn).stream()
                .map(book -> searchDetailMapper.from(book, locale))
                .toList();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail handleConstraintViolationException(ConstraintViolationException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
}
