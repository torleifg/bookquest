package com.github.torleifg.bookquest.adapter.web.api;

import com.github.torleifg.bookquest.adapter.web.config.SecurityConfig;
import com.github.torleifg.bookquest.core.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = SearchApiController.class)
@ContextConfiguration(classes = {SearchApiController.class, SecurityConfig.class})
class SearchApiControllerTests {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    SearchDetailMapper searchDetailMapper;

    @MockitoBean
    BookService bookService;

    @Test
    void hybridTest() throws Exception {
        mockMvc.perform(get("/api/search").with(csrf())
                        .param("language", "en")
                        .param("query", "query string"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));

        verify(bookService).hybridSearch("query string");
    }

    @Test
    void fullTextTest() throws Exception {
        mockMvc.perform(get("/api/search/fullText").with(csrf())
                        .param("language", "en")
                        .param("query", "query string"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));

        verify(bookService).fullTextSearch("query string");
    }

    @Test
    void semanticTest() throws Exception {
        mockMvc.perform(get("/api/search/semantic").with(csrf())
                        .param("language", "en")
                        .param("query", "query string"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));

        verify(bookService).semanticSearch("query string");
    }

    @Test
    void latestTest() throws Exception {
        mockMvc.perform(get("/api/search/latest").with(csrf())
                        .param("language", "en"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));

        verify(bookService).lastModified();
    }

    @Test
    void similarTest() throws Exception {
        mockMvc.perform(get("/api/search/similar").with(csrf())
                        .param("language", "en")
                        .param("isbn", "12345"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));

        verify(bookService).semanticSimilarity("12345");
    }
}
