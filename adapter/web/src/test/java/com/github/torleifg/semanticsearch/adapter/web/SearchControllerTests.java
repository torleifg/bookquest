package com.github.torleifg.semanticsearch.adapter.web;

import com.github.torleifg.semanticsearch.book.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Locale;

import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = SearchController.class)
@ContextConfiguration(classes = {SearchController.class, SecurityConfig.class})
class SearchControllerTests {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    BookService bookService;

    @Test
    void hybridSearchTest() throws Exception {
        mockMvc.perform(post("/").with(csrf())
                        .param("query", "query string"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"));

        verify(bookService).hybridSearch("query string", Locale.of("en"));
    }

    @Test
    void semanticSimilarityTest() throws Exception {
        mockMvc.perform(post("/").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"));

        verify(bookService).semanticSimilarity(Locale.of("en"));
    }
}
