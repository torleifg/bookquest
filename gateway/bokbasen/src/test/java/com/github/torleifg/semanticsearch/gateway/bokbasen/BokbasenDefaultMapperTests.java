package com.github.torleifg.semanticsearch.gateway.bokbasen;

import com.github.torleifg.semanticsearch.book.domain.BookFormat;
import com.github.torleifg.semanticsearch.book.domain.Contributor;
import com.github.torleifg.semanticsearch.book.domain.Language;
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

        var productForm = objectFactory.createProductForm();
        productForm.setValue(List150.fromValue("BB"));
        descriptiveDetail.setProductForm(productForm);

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

        var authorRole = objectFactory.createContributorRole();
        authorRole.setValue(List17.fromValue("A01"));
        contributor.getContent().add(authorRole);

        var illustratorRole = objectFactory.createContributorRole();
        illustratorRole.setValue(List17.fromValue("A12"));
        contributor.getContent().add(illustratorRole);

        var contributorName = objectFactory.createPersonNameInverted();
        contributorName.setValue("contributor");
        contributor.getContent().add(contributorName);

        descriptiveDetail.getContributor().add(contributor);

        var language = objectFactory.createLanguage();

        var languageRole = objectFactory.createLanguageRole();
        languageRole.setValue(List22.fromValue("01"));
        language.setLanguageRole(languageRole);

        var languageCode = objectFactory.createLanguageCode();
        languageCode.setValue(List74.fromValue("eng"));
        language.setLanguageCode(languageCode);

        descriptiveDetail.getLanguage().add(language);

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

        var genreAndForm = objectFactory.createSubject();

        var genreAndFormSubjectSchemeIdentifier = objectFactory.createSubjectSchemeIdentifier();
        genreAndFormSubjectSchemeIdentifier.setValue(List27.fromValue("C8"));
        genreAndForm.getContent().add(genreAndFormSubjectSchemeIdentifier);

        final SubjectCode genreSubjectCode = objectFactory.createSubjectCode();
        genreSubjectCode.setValue("id");
        genreAndForm.getContent().add(genreSubjectCode);

        var genreAndFormSubjectHeadingText = objectFactory.createSubjectHeadingText();
        genreAndFormSubjectHeadingText.setLanguage(List74.NNO);
        genreAndFormSubjectHeadingText.setValue("genre");
        genreAndForm.getContent().add(genreAndFormSubjectHeadingText);

        descriptiveDetail.getSubject().addAll(List.of(about, genreAndForm));
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

        var book = mapper.from(product);
        assertEquals("id", book.getExternalId());
        assertFalse(book.isDeleted());

        var metadata = book.getMetadata();

        assertEquals("isbn", metadata.getIsbn());
        assertEquals("title : remainder of title", metadata.getTitle());
        assertEquals("publisher", metadata.getPublisher());
        assertEquals(1, metadata.getContributors().size());
        assertEquals(2, metadata.getContributors().getFirst().roles().size());
        assertEquals(Contributor.Role.AUT, metadata.getContributors().getFirst().roles().getFirst());
        assertEquals(Contributor.Role.ILL, metadata.getContributors().getFirst().roles().getLast());
        assertEquals("1970", metadata.getPublishedYear());
        assertEquals("description", metadata.getDescription());
        assertEquals(1, metadata.getLanguages().size());
        assertEquals(Language.ENG, metadata.getLanguages().getFirst());
        assertEquals(BookFormat.HARDCOVER, metadata.getFormat());
        assertEquals(1, metadata.getAbout().size());
        assertNull(metadata.getAbout().getFirst().id());
        assertEquals("Bokbasen_Subject", metadata.getAbout().getFirst().source());
        assertEquals("nob", metadata.getAbout().getFirst().language());
        assertEquals("about", metadata.getAbout().getFirst().term());
        assertEquals(1, metadata.getGenreAndForm().size());
        assertEquals("id", metadata.getGenreAndForm().getFirst().id());
        assertEquals("ntsf", metadata.getGenreAndForm().getFirst().source());
        assertEquals("nno", metadata.getGenreAndForm().getFirst().language());
        assertEquals("genre", metadata.getGenreAndForm().getFirst().term());
        assertEquals("http://thumbnailUrl", metadata.getThumbnailUrl().toString());
    }

    @Test
    void mapDeletedProductTest() {
        var metadata = mapper.from("id");

        assertTrue(metadata.isDeleted());
    }
}
