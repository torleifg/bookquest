package com.github.torleifg.semanticsearch.adapter.web;

import com.github.torleifg.semanticsearch.book.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SearchController.class)
@ContextConfiguration(classes = {SearchController.class, SecurityConfig.class})
class SearchControllerTests {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    BookService bookService;

    @Test
    void fullTextSearchTest() throws Exception {
        mockMvc.perform(post("/")
                        .param("query", "query string")
                        .param("searchType", "fullText"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"));

        verify(bookService).fullTextSearch("query string");
    }

    @Test
    void semanticSearchTest() throws Exception {
        mockMvc.perform(post("/")
                        .param("query", "query string")
                        .param("searchType", "semantic"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"));

        verify(bookService).semanticSearch("query string");
    }

    @Test
    void semanticSimilarityTest() throws Exception {
        mockMvc.perform(post("/")
                        .param("searchType", "semantic"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"));

        verify(bookService).semanticSimilarity();
    }
}
