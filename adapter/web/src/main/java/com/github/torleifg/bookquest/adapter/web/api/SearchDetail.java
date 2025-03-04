package com.github.torleifg.bookquest.adapter.web.api;

import com.github.torleifg.bookquest.application.domain.Role;
import lombok.Data;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Data
public class SearchDetail {
    private String isbn;
    private String title;
    private String publisher;

    private List<Contributor> contributors = new ArrayList<>();

    private String publishedYear;
    private String description;

    private List<Language> languages = new ArrayList<>();

    private BookFormat bookFormat;

    private List<Classification> about = new ArrayList<>();
    private List<Classification> genreAndForm = new ArrayList<>();

    private URI thumbnailUrl;

    public record Contributor(List<ContributorRole> roles, String name) {
    }

    public record ContributorRole(Role role, String label) {
    }

    public record Language(com.github.torleifg.bookquest.application.domain.Language language, String label) {
    }

    public record BookFormat(com.github.torleifg.bookquest.application.domain.BookFormat format, String label) {
    }

    public record Classification(String id, String term) {
    }
}
