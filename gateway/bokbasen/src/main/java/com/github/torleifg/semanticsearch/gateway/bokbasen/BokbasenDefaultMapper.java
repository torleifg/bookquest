package com.github.torleifg.semanticsearch.gateway.bokbasen;

import com.github.torleifg.semanticsearch.book.service.MetadataDTO;
import org.editeur.ns.onix._3_0.reference.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Component
@ConditionalOnProperty(prefix = "bokbasen", name = "mapper", havingValue = "default", matchIfMissing = true)
class BokbasenDefaultMapper implements BokbasenMapper {

    @Override
    public MetadataDTO from(String id) {
        final MetadataDTO metadata = new MetadataDTO();
        metadata.setExternalId(id);
        metadata.setDeleted(true);

        return metadata;
    }

    @Override
    public MetadataDTO from(Product product) {
        final MetadataDTO metadata = new MetadataDTO();
        metadata.setExternalId(product.getRecordReference().getValue());
        metadata.setDeleted(false);

        product.getProductIdentifier().stream()
                .filter(productIdentifier -> productIdentifier.getProductIDType().getValue() == List5.fromValue("15"))
                .map(ProductIdentifier::getIDValue)
                .map(IDValue::getValue)
                .findFirst()
                .ifPresent(metadata::setIsbn);

        final DescriptiveDetail descriptiveDetail = product.getDescriptiveDetail();

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

        getContributor(descriptiveDetail, List17.fromValue("A01"))
                .forEach(metadata.getAuthors()::add);

        getContributor(descriptiveDetail, List17.fromValue("B06"))
                .forEach(metadata.getTranslators()::add);

        getContributor(descriptiveDetail, List17.fromValue("A12"))
                .forEach(metadata.getIllustrators()::add);

        final List<Subject> subjects = descriptiveDetail.getSubject();

        if (subjects != null && !subjects.isEmpty()) {
            for (final Subject subject : subjects) {
                final List<Object> content = subject.getContent();

                if (content != null && !content.isEmpty()) {
                    SubjectSchemeIdentifier subjectSchemeIdentifier = null;
                    SubjectSchemeName subjectSchemeName = null;
                    SubjectHeadingText subjectHeadingText = null;

                    for (final Object object : content) {
                        switch (object) {
                            case SubjectSchemeIdentifier identifier -> subjectSchemeIdentifier = identifier;
                            case SubjectSchemeName name -> subjectSchemeName = name;
                            case SubjectHeadingText text -> subjectHeadingText = text;
                            default -> {
                            }
                        }
                    }

                    if (subjectHeadingText == null || subjectHeadingText.getValue().isEmpty()) {
                        continue;
                    }

                    final String text = subjectHeadingText.getValue();

                    if (subjectSchemeIdentifier != null) {
                        if (isProprietary(subjectSchemeIdentifier)) {
                            if (subjectSchemeName != null) {
                                final String name = subjectSchemeName.getValue();

                                if (name.equals("Bokbasen_Subject")) {
                                    metadata.getAbout().add(text);
                                }
                            }
                        }

                        if (isGenreAndForm(subjectSchemeIdentifier)) {
                            metadata.getGenreAndForm().add(text);
                        }
                    }
                }
            }
        }

        final PublishingDetail publishingDetail = product.getPublishingDetail();

        Stream.ofNullable(publishingDetail)
                .map(PublishingDetail::getContent)
                .flatMap(Collection::stream)
                .filter(Publisher.class::isInstance)
                .map(Publisher.class::cast)
                .map(Publisher::getContent)
                .flatMap(Collection::stream)
                .filter(PublisherName.class::isInstance)
                .map(PublisherName.class::cast)
                .map(PublisherName::getValue)
                .findFirst()
                .ifPresent(metadata::setPublisher);

        Stream.ofNullable(publishingDetail)
                .map(PublishingDetail::getContent)
                .flatMap(Collection::stream)
                .filter(PublishingDate.class::isInstance)
                .map(PublishingDate.class::cast)
                .filter(publishingDate -> publishingDate.getPublishingDateRole().getValue() == List163.fromValue("01"))
                .map(PublishingDate::getDate)
                .map(Date::getValue)
                .findFirst()
                .ifPresent(metadata::setPublishedYear);

        final CollateralDetail collateralDetail = product.getCollateralDetail();

        Stream.ofNullable(collateralDetail)
                .map(CollateralDetail::getTextContent)
                .flatMap(Collection::stream)
                .filter(textContent -> textContent.getTextType().getValue() == List153.fromValue("03"))
                .map(TextContent::getText)
                .flatMap(Collection::stream)
                .filter(text -> text.getTextformat().value().equals("06"))
                .map(Text::getContent)
                .flatMap(Collection::stream)
                .map(Serializable::toString)
                .findFirst()
                .ifPresent(metadata::setDescription);

        Stream.ofNullable(collateralDetail)
                .map(CollateralDetail::getSupportingResource)
                .flatMap(Collection::stream)
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
                .map(URI::create)
                .findFirst()
                .ifPresent(metadata::setThumbnailUrl);

        return metadata;
    }

    private static Stream<String> getContributor(DescriptiveDetail descriptiveDetail, List17 role) {
        return Stream.ofNullable(descriptiveDetail)
                .map(DescriptiveDetail::getContributor)
                .flatMap(Collection::stream)
                .map(Contributor::getContent)
                .filter(content -> {
                    for (final Object object : content) {
                        if (object instanceof ContributorRole contributorRole) {
                            return contributorRole.getValue() == role;
                        }
                    }

                    return false;
                })
                .map(content -> {
                    for (final Object object : content) {
                        if (object instanceof PersonNameInverted name) {
                            return name.getValue();
                        } else if (object instanceof PersonName name) {
                            return name.getValue();
                        }
                    }

                    return null;
                })
                .filter(Objects::nonNull);
    }

    private static boolean isProprietary(SubjectSchemeIdentifier subjectSchemeIdentifier) {
        return subjectSchemeIdentifier.getValue() == List27.fromValue("24");
    }

    private static boolean isGenreAndForm(SubjectSchemeIdentifier subjectSchemeIdentifier) {
        return subjectSchemeIdentifier.getValue() == List27.fromValue("C8");
    }

    private static Stream<Object> getTitleContent(DescriptiveDetail descriptiveDetail) {
        return Stream.ofNullable(descriptiveDetail)
                .map(DescriptiveDetail::getTitleDetail)
                .flatMap(Collection::stream)
                .filter(titleDetail -> titleDetail.getTitleType().getValue() == List15.fromValue("01"))
                .map(TitleDetail::getTitleElement)
                .flatMap(Collection::stream)
                .map(TitleElement::getContent)
                .flatMap(Collection::stream);
    }
}
