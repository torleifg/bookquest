package com.github.torleifg.bookquest.adapter.web.gui;

import com.github.torleifg.bookquest.adapter.web.config.SecurityConfig;
import com.github.torleifg.bookquest.core.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = SearchGuiController.class)
@ContextConfiguration(classes = {SearchGuiController.class, SecurityConfig.class})
class SearchGuiControllerTests {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    SearchViewMapper searchViewMapper;

    @MockitoBean
    BookService bookService;

    @Test
    void latestTest() throws Exception {
        mockMvc.perform(get("/").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"));

        when(bookService.lastModified()).thenReturn(List.of());
    }

    @Test
    void hybridSearchTest() throws Exception {
        mockMvc.perform(get("/search").with(csrf())
                        .param("query", "query string"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"));

        when(bookService.hybridSearch("query string")).thenReturn(List.of());
    }

    @Test
    void semanticSimilarityTest() throws Exception {
        mockMvc.perform(get("/search").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"));

        when(bookService.semanticSimilarity()).thenReturn(List.of());
    }
}
