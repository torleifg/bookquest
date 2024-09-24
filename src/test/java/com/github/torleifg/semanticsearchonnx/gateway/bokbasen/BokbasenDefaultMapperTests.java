package com.github.torleifg.semanticsearchonnx.gateway.bokbasen;

import org.editeur.ns.onix._3_0.reference.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BokbasenDefaultMapperTests {
    BokbasenDefaultMapper mapper = new BokbasenDefaultMapper();

    ObjectFactory objectFactory = new ObjectFactory();

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

        var contributor = objectFactory.createContributor();

        var contributorRole = objectFactory.createContributorRole();
        contributorRole.setValue(List17.fromValue("A01"));
        contributor.getContent().add(contributorRole);

        var personNameInverted = objectFactory.createPersonNameInverted();
        personNameInverted.setValue("name");
        contributor.getContent().add(personNameInverted);

        descriptiveDetail.getContributor().add(contributor);

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

        var form = objectFactory.createSubject();

        var formSubjectSchemeIdentifier = objectFactory.createSubjectSchemeIdentifier();
        formSubjectSchemeIdentifier.setValue(List27.fromValue("24"));
        form.getContent().add(formSubjectSchemeIdentifier);

        var formSubjectSchemeName = objectFactory.createSubjectSchemeName();
        formSubjectSchemeName.setValue("Bokbasen_Form");
        form.getContent().add(formSubjectSchemeName);

        var formSubjectHeadingText = objectFactory.createSubjectHeadingText();
        formSubjectHeadingText.setValue("form");
        form.getContent().add(formSubjectHeadingText);

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

        descriptiveDetail.getSubject().addAll(List.of(genre, form, about));
        product.setDescriptiveDetail(descriptiveDetail);

        var publishingDetail = objectFactory.createPublishingDetail();

        var publishedYear = objectFactory.createPublishingDate();

        var publishingDateRole = objectFactory.createPublishingDateRole();
        publishingDateRole.setValue(List163.fromValue("01"));
        publishedYear.setPublishingDateRole(publishingDateRole);

        var date = objectFactory.createDate();
        date.setValue("1970");
        publishedYear.setDate(date);

        publishingDetail.getContent().add(publishedYear);
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

        assertEquals("id", metadata.getCode());
        assertEquals("isbn", metadata.getIsbn());
        assertEquals("title : remainder of title", metadata.getTitle());
        assertEquals(1, metadata.getAuthors().size());
        assertEquals("1970", metadata.getPublishedYear());
        assertEquals("description", metadata.getDescription());
        assertEquals(2, metadata.getGenreAndForm().size());
        assertEquals(1, metadata.getAbout().size());
        assertEquals("http://thumbnailUrl", metadata.getThumbnailUrl().toString());
    }

    @Test
    void mapDeletedProductTest() {
        var metadata = mapper.from("id");

        assertTrue(metadata.isDeleted());
    }
}
