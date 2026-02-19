package com.github.torleifg.bookquest.adapter.web.gui;

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

@WebMvcTest(value = SearchGuiController.class)
@ContextConfiguration(classes = {SearchGuiController.class, SecurityConfig.class})
@org.springframework.test.context.TestPropertySource(properties = {
        "reciprocal-rank-fusion.keyword-full-text-search-weight=0.7",
        "reciprocal-rank-fusion.keyword-semantic-search-weight=0.3"
})
class SearchGuiControllerTests {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    SearchViewMapper searchViewMapper;

    @MockitoBean
    BookService bookService;

    @Test
    void latestTest() throws Exception {
        mockMvc.perform(get("/").with(csrf())
                        .param("genre", "genre string"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"));

        verify(bookService).latest("genre string");
    }

    @Test
    void keywordSearchTest() throws Exception {
        mockMvc.perform(get("/search").with(csrf())
                        .param("query", "query string"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"));

        verify(bookService).hybridSearch("query string", 0.7, 0.3);
    }

    @Test
    void hybridSearchTest() throws Exception {
        mockMvc.perform(get("/search").with(csrf())
                        .param("query", "query string string string string"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"));

        verify(bookService).hybridSearch("query string string string string");
    }

    @Test
    void semanticSimilarityTest() throws Exception {
        mockMvc.perform(get("/similar").with(csrf())
                        .param("isbn", "isbn"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"));

        verify(bookService).semanticSimilarity("isbn");
    }

    @Test
    void autocompleteTest() throws Exception {
        mockMvc.perform(get("/autocomplete").with(csrf())
                        .param("term", "term"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));

        verify(bookService).autocomplete("term");
    }
}
