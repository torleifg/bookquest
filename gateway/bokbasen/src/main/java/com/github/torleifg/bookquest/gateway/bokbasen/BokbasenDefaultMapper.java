package com.github.torleifg.bookquest.gateway.bokbasen;

import com.github.torleifg.bookquest.core.domain.*;
import org.editeur.ns.onix._3_1.reference.*;
import org.editeur.ns.onix._3_1.reference.Contributor;
import org.editeur.ns.onix._3_1.reference.Language;

import java.io.Serializable;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

class BokbasenDefaultMapper implements BokbasenMapper {
    private static final String SOURCE = "bokbasen";

    @Override
    public Book from(String id) {
        final Book book = new Book();
        book.setSource(SOURCE);
        book.setExternalId(id);
        book.setDeleted(true);

        return book;
    }

    @Override
    public Book from(Product product) {
        final Book book = new Book();
        book.setSource(SOURCE);
        book.setExternalId(product.getRecordReference().getValue());
        book.setDeleted(false);

        final Metadata metadata = new Metadata();

        product.getProductIdentifier().stream()
                .filter(productIdentifier -> productIdentifier.getProductIDType().getValue() == List5.fromValue("15"))
                .map(ProductIdentifier::getIDValue)
                .map(IDValue::getValue)
                .findFirst()
                .ifPresent(metadata::setIsbn);

        final DescriptiveDetail descriptiveDetail = product.getDescriptiveDetail();

        if (descriptiveDetail == null) {
            return book;
        }

        final Optional<String> title = getTitleContent(descriptiveDetail)
                .filter(TitleText.class::isInstance)
                .map(TitleText.class::cast)
                .map(TitleText::getValue)
                .findFirst();

        final Optional<String> remainderOfTitle = getTitleContent(descriptiveDetail)
                .filter(Subtitle.class::isInstance)
                .map(Subtitle.class::cast)
                .map(Subtitle::getValue)
                .findFirst();

        if (title.isPresent() && remainderOfTitle.isPresent()) {
            metadata.setTitle(title.get() + " : " + remainderOfTitle.get());
        } else title.ifPresent(metadata::setTitle);

        final List<Contributor> contributors = Optional.ofNullable(descriptiveDetail.getContributor())
                .stream()
                .flatMap(Collection::stream)
                .toList();

        for (final Contributor contributor : contributors) {
            final List<Role> roles = contributor.getContent().stream()
                    .filter(ContributorRole.class::isInstance)
                    .map(ContributorRole.class::cast)
                    .map(ContributorRole::getValue)
                    .map(List17::value)
                    .map(role -> {
                        try {
                            return ContributorRoleMapping.valueOf(role);
                        } catch (IllegalArgumentException e) {
                            return ContributorRoleMapping.Z99;
                        }
                    })
                    .map(ContributorRoleMapping::getCode)
                    .toList();

            if (roles.isEmpty()) {
                continue;
            }

            final Optional<String> name = contributor.getContent().stream()
                    .map(content -> switch (content) {
                        case PersonNameInverted personNameInverted -> personNameInverted.getValue();
                        case PersonName personName -> personName.getValue();
                        case CorporateNameInverted corporateNameInverted -> corporateNameInverted.getValue();
                        case CorporateName corporateName -> corporateName.getValue();
                        default -> null;
                    })
                    .filter(Objects::nonNull)
                    .findFirst();

            if (name.isEmpty()) {
                continue;
            }

            metadata.getContributors().add(new com.github.torleifg.bookquest.core.domain.Contributor(roles, name.get()));
        }

        descriptiveDetail.getLanguage().stream()
                .filter(language -> language.getLanguageRole().getValue() == List22.fromValue("01"))
                .map(Language::getLanguageCode)
                .map(LanguageCode::getValue)
                .map(List74::value)
                .map(com.github.torleifg.bookquest.core.domain.Language::fromTag)
                .forEach(language -> metadata.getLanguages().add(language));

        Optional.ofNullable(descriptiveDetail.getProductForm())
                .map(ProductForm::getValue)
                .map(List150::value)
                .ifPresentOrElse(productForm -> {
                    switch (productForm) {
                        case "BB" -> metadata.setFormat(BookFormat.HARDCOVER);
                        case "BC" -> metadata.setFormat(BookFormat.PAPERBACK);
                        case "ED" -> metadata.setFormat(BookFormat.EBOOK);
                        case "AJ" -> metadata.setFormat(BookFormat.AUDIOBOOK);
                        default -> metadata.setFormat(BookFormat.UNKNOWN);
                    }
                }, () -> metadata.setFormat(BookFormat.UNKNOWN));

        final List<Subject> subjects = descriptiveDetail.getSubject();

        if (subjects != null && !subjects.isEmpty()) {
            for (final Subject subject : subjects) {
                final List<Object> content = subject.getContent();

                if (content != null && !content.isEmpty()) {
                    SubjectSchemeIdentifier subjectSchemeIdentifier = null;
                    SubjectSchemeName subjectSchemeName = null;
                    SubjectCode subjectCode = null;
                    SubjectHeadingText subjectHeadingText = null;

                    for (final Object object : content) {
                        switch (object) {
                            case SubjectSchemeIdentifier identifier -> subjectSchemeIdentifier = identifier;
                            case SubjectCode code -> subjectCode = code;
                            case SubjectHeadingText text -> subjectHeadingText = text;
                            default -> {
                            }
                        }
                    }

                    if (subjectHeadingText == null || subjectHeadingText.getValue().isEmpty()) {
                        continue;
                    }

                    final String language = subjectHeadingText.getLanguage() != null ? subjectHeadingText.getLanguage().value() : "nob";

                    final String code = Optional.ofNullable(subjectCode)
                            .map(SubjectCode::getValue)
                            .orElse(null);

                    final String text = subjectHeadingText.getValue();

                    if (subjectSchemeIdentifier != null) {
                        if (isSubjects(subjectSchemeIdentifier)) {
                            metadata.getAbout().add(new Classification(code, "neo", language, com.github.torleifg.bookquest.core.domain.Language.fromTag(language), text));
                        }

                        if (isGenreAndForm(subjectSchemeIdentifier)) {
                            metadata.getGenreAndForm().add(new Classification(code, "ntsf", language, com.github.torleifg.bookquest.core.domain.Language.fromTag(language), text));
                        }
                    }
                }
            }
        }

        final PublishingDetail publishingDetail = product.getPublishingDetail();

        if (publishingDetail != null) {
            publishingDetail.getContent().stream()
                    .filter(Publisher.class::isInstance)
                    .map(Publisher.class::cast)
                    .map(Publisher::getContent)
                    .flatMap(Collection::stream)
                    .filter(PublisherName.class::isInstance)
                    .map(PublisherName.class::cast)
                    .map(PublisherName::getValue)
                    .findFirst()
                    .ifPresent(metadata::setPublisher);

            publishingDetail.getContent().stream()
                    .filter(PublishingDate.class::isInstance)
                    .map(PublishingDate.class::cast)
                    .filter(publishingDate -> publishingDate.getPublishingDateRole().getValue() == List163.fromValue("01"))
                    .map(PublishingDate::getDate)
                    .map(Date::getValue)
                    .findFirst()
                    .ifPresent(metadata::setPublishedYear);
        }

        final CollateralDetail collateralDetail = product.getCollateralDetail();

        if (collateralDetail != null) {
            collateralDetail.getTextContent().stream()
                    .filter(textContent -> textContent.getTextType().getValue() == List153.fromValue("03"))
                    .map(TextContent::getText)
                    .flatMap(Collection::stream)
                    .filter(text -> text.getTextformat().value().equals("06"))
                    .map(Text::getContent)
                    .flatMap(Collection::stream)
                    .map(Serializable::toString)
                    .findFirst()
                    .ifPresent(metadata::setDescription);

            collateralDetail.getSupportingResource().stream()
                    .filter(supportingResource -> supportingResource.getResourceContentType().getValue() == List158.fromValue("01"))
                    .map(SupportingResource::getResourceVersion)
                    .flatMap(Collection::stream)
                    .filter(resourceVersion -> resourceVersion.getResourceVersionFeature().stream()
                            .map(ResourceVersionFeature::getFeatureNote)
                            .flatMap(Collection::stream)
                            .map(FeatureNote::getContent)
                            .flatMap(Collection::stream)
                            .map(Serializable::toString)
                            .anyMatch("org.jpg"::equals))
                    .map(ResourceVersion::getResourceLink)
                    .flatMap(Collection::stream)
                    .map(ResourceLink::getValue)
                    .map(uri -> {
                        try {
                            return URI.create(uri);
                        } catch (IllegalArgumentException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .findFirst()
                    .ifPresent(metadata::setThumbnailUrl);
        }

        book.setMetadata(metadata);

        return book;
    }

    private static boolean isSubjects(SubjectSchemeIdentifier subjectSchemeIdentifier) {
        return subjectSchemeIdentifier.getValue() == List27.fromValue("D3");
    }

    private static boolean isGenreAndForm(SubjectSchemeIdentifier subjectSchemeIdentifier) {
        return subjectSchemeIdentifier.getValue() == List27.fromValue("C8");
    }

    private static Stream<Object> getTitleContent(DescriptiveDetail descriptiveDetail) {
        return Optional.ofNullable(descriptiveDetail.getTitleDetail())
                .stream()
                .flatMap(Collection::stream)
                .filter(titleDetail -> titleDetail.getTitleType().getValue() == List15.fromValue("01"))
                .map(TitleDetail::getTitleElement)
                .flatMap(Collection::stream)
                .map(TitleElement::getContent)
                .flatMap(Collection::stream);
    }
}
