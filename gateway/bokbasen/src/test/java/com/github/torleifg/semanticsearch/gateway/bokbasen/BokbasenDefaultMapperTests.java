package com.github.torleifg.semanticsearch.gateway.bokbasen;

import org.editeur.ns.onix._3_0.reference.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BokbasenDefaultMapperTests {
    final BokbasenDefaultMapper mapper = new BokbasenDefaultMapper();

    final ObjectFactory objectFactory = new ObjectFactory();

    @Test
    void mapProductTest() {
        var product = objectFactory.createProduct();

        var recordReference = objectFactory.createRecordReference();
        recordReference.setValue("id");
        product.setRecordReference(recordReference);

        var isbn = objectFactory.createProductIdentifier();

        var productIDType = objectFactory.createProductIDType();
        productIDType.setValue(List5.fromValue("15"));
        isbn.setProductIDType(productIDType);

        var idValue = objectFactory.createIDValue();
        idValue.setValue("isbn");
        isbn.setIDValue(idValue);

        product.getProductIdentifier().add(isbn);

        var descriptiveDetail = objectFactory.createDescriptiveDetail();

        var title = objectFactory.createTitleDetail();

        var titleType = objectFactory.createTitleType();
        titleType.setValue(List15.fromValue("01"));
        title.setTitleType(titleType);

        var titleText = objectFactory.createTitleText();
        titleText.setValue("title");

        var remainderOfTitleText = objectFactory.createSubtitle();
        remainderOfTitleText.setValue("remainder of title");

        var titleElement = objectFactory.createTitleElement();
        titleElement.getContent().addAll(List.of(titleText, remainderOfTitleText));

        title.getTitleElement().add(titleElement);

        descriptiveDetail.getTitleDetail().add(title);

        var author = objectFactory.createContributor();

        var authorRole = objectFactory.createContributorRole();
        authorRole.setValue(List17.fromValue("A01"));
        author.getContent().add(authorRole);

        var authorName = objectFactory.createPersonNameInverted();
        authorName.setValue("author");
        author.getContent().add(authorName);

        var translator = objectFactory.createContributor();

        var translatorRole = objectFactory.createContributorRole();
        translatorRole.setValue(List17.fromValue("B06"));
        translator.getContent().add(translatorRole);

        var translatorName = objectFactory.createPersonNameInverted();
        translatorName.setValue("translator");
        translator.getContent().add(translatorName);

        var illustrator = objectFactory.createContributor();

        var illustratorRole = objectFactory.createContributorRole();
        illustratorRole.setValue(List17.fromValue("A12"));
        illustrator.getContent().add(illustratorRole);

        var illustratorName = objectFactory.createPersonNameInverted();
        illustratorName.setValue("illustrator");
        illustrator.getContent().add(illustratorName);

        descriptiveDetail.getContributor().addAll(List.of(author, translator, illustrator));

        var genre = objectFactory.createSubject();

        var genreSubjectSchemeIdentifier = objectFactory.createSubjectSchemeIdentifier();
        genreSubjectSchemeIdentifier.setValue(List27.fromValue("24"));
        genre.getContent().add(genreSubjectSchemeIdentifier);

        var genreSubjectSchemeName = objectFactory.createSubjectSchemeName();
        genreSubjectSchemeName.setValue("Bokbasen_Genre");
        genre.getContent().add(genreSubjectSchemeName);

        var genreSubjectHeadingText = objectFactory.createSubjectHeadingText();
        genreSubjectHeadingText.setValue("genre");
        genre.getContent().add(genreSubjectHeadingText);

        var genreAndForm = objectFactory.createSubject();

        var genreAndFormSubjectSchemeIdentifier = objectFactory.createSubjectSchemeIdentifier();
        genreAndFormSubjectSchemeIdentifier.setValue(List27.fromValue("C8"));
        genreAndForm.getContent().add(genreAndFormSubjectSchemeIdentifier);

        var genreAndFormSubjectHeadingText = objectFactory.createSubjectHeadingText();
        genreAndFormSubjectHeadingText.setValue("form");
        genreAndForm.getContent().add(genreAndFormSubjectHeadingText);

        var about = objectFactory.createSubject();

        var aboutSubjectSchemeIdentifier = objectFactory.createSubjectSchemeIdentifier();
        aboutSubjectSchemeIdentifier.setValue(List27.fromValue("24"));
        about.getContent().add(aboutSubjectSchemeIdentifier);

        var aboutSubjectSchemeName = objectFactory.createSubjectSchemeName();
        aboutSubjectSchemeName.setValue("Bokbasen_Subject");
        about.getContent().add(aboutSubjectSchemeName);

        var aboutSubjectHeadingText = objectFactory.createSubjectHeadingText();
        aboutSubjectHeadingText.setValue("about");
        about.getContent().add(aboutSubjectHeadingText);

        descriptiveDetail.getSubject().addAll(List.of(genre, genreAndForm, about));
        product.setDescriptiveDetail(descriptiveDetail);

        var publishingDetail = objectFactory.createPublishingDetail();

        var publisher = objectFactory.createPublisher();

        var publisherName = objectFactory.createPublisherName();
        publisherName.setValue("publisher");

        publisher.getContent().add(publisherName);

        var publishedYear = objectFactory.createPublishingDate();

        var publishingDateRole = objectFactory.createPublishingDateRole();
        publishingDateRole.setValue(List163.fromValue("01"));
        publishedYear.setPublishingDateRole(publishingDateRole);

        var date = objectFactory.createDate();
        date.setValue("1970");
        publishedYear.setDate(date);

        publishingDetail.getContent().addAll(List.of(publisher, publishedYear));
        product.setPublishingDetail(publishingDetail);

        var collateralDetail = objectFactory.createCollateralDetail();

        var description = objectFactory.createTextContent();

        var textType = objectFactory.createTextType();
        textType.setValue(List153.fromValue("03"));
        description.setTextType(textType);

        var text = objectFactory.createText();
        text.setTextformat(List34.fromValue("06"));
        text.getContent().add("description");
        description.getText().add(text);

        collateralDetail.getTextContent().add(description);

        var thumbnailUrl = objectFactory.createSupportingResource();

        var resourceContentType = objectFactory.createResourceContentType();
        resourceContentType.setValue(List158.fromValue("01"));
        thumbnailUrl.setResourceContentType(resourceContentType);

        var resourceVersion = objectFactory.createResourceVersion();

        var resourceVersionFeature = objectFactory.createResourceVersionFeature();

        var featureNote = objectFactory.createFeatureNote();
        featureNote.getContent().add("org.jpg");
        resourceVersionFeature.getFeatureNote().add(featureNote);

        resourceVersion.getResourceVersionFeature().add(resourceVersionFeature);

        var resourceLink = objectFactory.createResourceLink();
        resourceLink.setValue("http://thumbnailUrl");

        resourceVersion.getResourceLink().add(resourceLink);

        thumbnailUrl.getResourceVersion().add(resourceVersion);

        collateralDetail.getSupportingResource().add(thumbnailUrl);

        product.setCollateralDetail(collateralDetail);

        var metadata = mapper.from(product);

        assertFalse(metadata.isDeleted());

        assertEquals("id", metadata.getExternalId());
        assertEquals("isbn", metadata.getIsbn());
        assertEquals("title : remainder of title", metadata.getTitle());
        assertEquals("publisher", metadata.getPublisher());
        assertEquals(1, metadata.getAuthors().size());
        assertEquals(1, metadata.getTranslators().size());
        assertEquals(1, metadata.getIllustrators().size());
        assertEquals("1970", metadata.getPublishedYear());
        assertEquals("description", metadata.getDescription());
        assertEquals(1, metadata.getGenreAndForm().size());
        assertEquals(1, metadata.getAbout().size());
        assertEquals("http://thumbnailUrl", metadata.getThumbnailUrl().toString());
    }

    @Test
    void mapDeletedProductTest() {
        var metadata = mapper.from("id");

        assertTrue(metadata.isDeleted());
    }
}
