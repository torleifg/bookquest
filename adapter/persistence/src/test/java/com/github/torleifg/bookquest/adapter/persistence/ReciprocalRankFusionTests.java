package com.github.torleifg.bookquest.adapter.persistence;

import com.github.torleifg.bookquest.application.domain.Book;
import com.github.torleifg.bookquest.application.domain.Metadata;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReciprocalRankFusionTests {

    @Test
    void computeTest() {
        var firstBook = createBook("Book W");
        var secondBook = createBook("Book X");
        var thirdBook = createBook("Book Y");
        var fourthBook = createBook("Book Z");

        var fullTextSearchHits = List.of(firstBook, secondBook, thirdBook);
        var vectorSearchHits = List.of(thirdBook, fourthBook);

        var mergedSearchHits = new ReciprocalRankFusion("Book", fullTextSearchHits, vectorSearchHits);

        var rrf = mergedSearchHits.compute();

        System.out.println("Ordered by RRF Score:");
        rrf.forEach((book, score) ->
                System.out.println(book.getMetadata().getTitle() + " -> " + score)
        );

        assertEquals(4, rrf.size());

        var expectedRanking = List.of(thirdBook, firstBook, secondBook, fourthBook);

        var actualRanking = List.copyOf(rrf.keySet());

        for (int i = 0; i < expectedRanking.size(); i++) {
            assertEquals(expectedRanking.get(i), actualRanking.get(i),
                    "Mismatch at rank " + (i + 1) +
                            ": Expected " + expectedRanking.get(i).getMetadata().getTitle() +
                            " : Found " + actualRanking.get(i).getMetadata().getTitle());
        }
    }

    Book createBook(String title) {
        var book = new Book();

        var metadata = new Metadata();
        metadata.setTitle(title);

        book.setMetadata(metadata);

        return book;
    }
}
