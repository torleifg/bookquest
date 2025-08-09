package com.github.torleifg.bookquest.adapter.web.api;

import com.github.torleifg.bookquest.core.domain.Book;
import com.github.torleifg.bookquest.core.domain.BookFormat;
import com.github.torleifg.bookquest.core.domain.Metadata;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class SearchDetailMapper {
    private final MessageSource languageSource;
    private final MessageSource contributorSource;
    private final MessageSource formatSource;

    public SearchDetailMapper(@Qualifier("languageSource") MessageSource languageSource,
                              @Qualifier("contributorSource") MessageSource contributorSource,
                              @Qualifier("formatSource") MessageSource formatSource) {
        this.languageSource = languageSource;
        this.contributorSource = contributorSource;
        this.formatSource = formatSource;
    }

    public SearchDetail from(Book book, Locale locale) {
        final Metadata metadata = book.getMetadata();

        final SearchDetail searchDetail = new SearchDetail();
        searchDetail.setIsbn(metadata.getIsbn());
        searchDetail.setTitle(metadata.getTitle());
        searchDetail.setPublisher(metadata.getPublisher());

        metadata.getContributors().stream()
                .map(contributor -> {
                    final List<SearchDetail.ContributorRole> contributorRoles = contributor.roles().stream()
                            .map(role -> new SearchDetail.ContributorRole(role, contributorSource.getMessage(role.name(), null, locale)))
                            .toList();

                    return new SearchDetail.Contributor(contributorRoles, contributor.name());
                })
                .forEach(searchDetail.getContributors()::add);

        searchDetail.setPublishedYear(metadata.getPublishedYear());
        searchDetail.setDescription(metadata.getDescription());

        metadata.getLanguages().stream()
                .map(language -> new SearchDetail.Language(language, languageSource.getMessage(language.name(), null, locale)))
                .forEach(searchDetail.getLanguages()::add);

        if (metadata.getFormat() != null && metadata.getFormat() != BookFormat.UNKNOWN) {
            searchDetail.setBookFormat(new SearchDetail.BookFormat(metadata.getFormat(), formatSource.getMessage(metadata.getFormat().name(), null, locale)));
        }

        metadata.getAbout().stream()
                .filter(classification -> classification.hasLanguage(locale))
                .map(classification -> new SearchDetail.Classification(classification.id(), classification.term()))
                .forEach(searchDetail.getAbout()::add);

        metadata.getGenreAndForm().stream()
                .filter(classification -> classification.hasLanguage(locale))
                .map(classification -> new SearchDetail.Classification(classification.id(), classification.term()))
                .forEach(searchDetail.getGenreAndForm()::add);

        if (metadata.getThumbnailUrl() != null) {
            searchDetail.setThumbnailUrl(metadata.getThumbnailUrl());
        }

        return searchDetail;
    }
}
