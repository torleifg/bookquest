package com.github.torleifg.bookquest.application.service;

import com.github.torleifg.bookquest.application.domain.Book;
import com.github.torleifg.bookquest.application.domain.BookFormat;
import com.github.torleifg.bookquest.application.domain.Classification;
import com.github.torleifg.bookquest.application.domain.Metadata;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

import static java.util.stream.Collectors.joining;

@Component
public class SearchMapper {
    private final MessageSource languageSource;
    private final MessageSource contributorSource;
    private final MessageSource formatSource;

    public SearchMapper(@Qualifier("languageSource") MessageSource languageSource,
                        @Qualifier("contributorSource") MessageSource contributorSource,
                        @Qualifier("formatSource") MessageSource formatSource) {
        this.languageSource = languageSource;
        this.contributorSource = contributorSource;
        this.formatSource = formatSource;
    }

    public SearchDTO from(Book book, Locale locale) {
        final Metadata metadata = book.getMetadata();

        final SearchDTO searchDTO = new SearchDTO();
        searchDTO.setIsbn(metadata.getIsbn());
        searchDTO.setTitle(metadata.getTitle());
        searchDTO.setPublisher(metadata.getPublisher());

        final String contributors = metadata.getContributors().stream()
                .map(contributor -> {
                    final String roles = contributor.roles().stream()
                            .map(role -> contributorSource.getMessage(role.name(), null, locale))
                            .collect(joining(", "));

                    return contributor.name() + " (" + roles + ")";
                })
                .collect(joining(" ; "));

        searchDTO.setContributors(contributors);

        searchDTO.setPublishedYear(metadata.getPublishedYear());
        searchDTO.setDescription(metadata.getDescription());

        final String languages = metadata.getLanguages().stream()
                .map(language -> languageSource.getMessage(language.name(), null, locale))
                .collect(joining(", "));

        searchDTO.setLanguages(languages);

        if (metadata.getFormat() != null && metadata.getFormat() != BookFormat.UNKNOWN) {
            searchDTO.setFormat(formatSource.getMessage(metadata.getFormat().name(), null, locale));
        }

        final String about = metadata.getAbout().stream()
                .filter(classification -> classification.language().equals(locale.getISO3Language()))
                .map(Classification::term)
                .collect(joining(", "));

        searchDTO.setAbout(about);

        final String genreAndForm = metadata.getGenreAndForm().stream()
                .filter(classification -> classification.language().equals(locale.getISO3Language()))
                .map(Classification::term)
                .collect(joining(", "));

        searchDTO.setGenreAndForm(genreAndForm);

        if (metadata.getThumbnailUrl() != null) {
            searchDTO.setThumbnailUrl(metadata.getThumbnailUrl().toString());
        }

        return searchDTO;
    }
}
