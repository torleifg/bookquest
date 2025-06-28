package com.github.torleifg.bookquest.adapter.web.gui;

import com.github.torleifg.bookquest.core.domain.Book;
import com.github.torleifg.bookquest.core.domain.BookFormat;
import com.github.torleifg.bookquest.core.domain.Classification;
import com.github.torleifg.bookquest.core.domain.Metadata;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

import static java.util.stream.Collectors.joining;

@Component
public class SearchViewMapper {
    private final MessageSource languageSource;
    private final MessageSource contributorSource;
    private final MessageSource formatSource;

    public SearchViewMapper(@Qualifier("languageSource") MessageSource languageSource,
                            @Qualifier("contributorSource") MessageSource contributorSource,
                            @Qualifier("formatSource") MessageSource formatSource) {
        this.languageSource = languageSource;
        this.contributorSource = contributorSource;
        this.formatSource = formatSource;
    }

    public SearchView from(Book book, Locale locale) {
        final Metadata metadata = book.getMetadata();

        final SearchView searchView = new SearchView();
        searchView.setIsbn(metadata.getIsbn());
        searchView.setTitle(metadata.getTitle());
        searchView.setPublisher(metadata.getPublisher());

        final String contributors = metadata.getContributors().stream()
                .map(contributor -> {
                    final String roles = contributor.roles().stream()
                            .map(role -> contributorSource.getMessage(role.name(), null, locale))
                            .collect(joining(", "));

                    return contributor.name() + " (" + roles + ")";
                })
                .collect(joining(" ; "));

        searchView.setContributors(contributors);

        if (metadata.getPublishedYear() != null) {
            searchView.setPublishedYear(metadata.getPublishedYear().replaceAll("\\D", ""));
        }

        searchView.setDescription(metadata.getDescription());

        final String languages = metadata.getLanguages().stream()
                .map(language -> languageSource.getMessage(language.name(), null, locale))
                .collect(joining(", "));

        searchView.setLanguages(languages);

        if (metadata.getFormat() != null && metadata.getFormat() != BookFormat.UNKNOWN) {
            searchView.setBookFormat(formatSource.getMessage(metadata.getFormat().name(), null, locale));
        }

        final String about = metadata.getAbout().stream()
                .filter(classification -> classification.language().equals(locale.getISO3Language()))
                .map(Classification::term)
                .collect(joining(", "));

        searchView.setAbout(about);

        final String genreAndForm = metadata.getGenreAndForm().stream()
                .filter(classification -> classification.language().equals(locale.getISO3Language()))
                .map(Classification::term)
                .collect(joining(", "));

        searchView.setGenreAndForm(genreAndForm);

        if (metadata.getThumbnailUrl() != null) {
            searchView.setThumbnailUrl(metadata.getThumbnailUrl().toString());
        }

        return searchView;
    }
}
