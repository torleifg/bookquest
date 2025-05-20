package com.github.torleifg.bookquest.adapter.persistence;

import com.github.torleifg.bookquest.core.domain.Book;
import com.github.torleifg.bookquest.core.domain.Metadata;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReciprocalRankFusionTests {

    @Test
    void computeTest() {
        var firstBook = createBook("Book A B C");
        var secondBook = createBook("Book D E F");
        var thirdBook = createBook("Book G H I");
        var fourthBook = createBook("Book J K L");

        var fullText = List.of(firstBook, secondBook, thirdBook);
        var semantic = List.of(thirdBook, fourthBook);

        List<RankedSearchHit> rankedSearchHit = List.of(
                ReciprocalRankFusion.toRankedSearchHit(fullText, 0.5),
                ReciprocalRankFusion.toRankedSearchHit(semantic, 0.5)
        );

        var rrf = new ReciprocalRankFusion(rankedSearchHit).compute();

        System.out.println("Ordered by RRF Score:");
        rrf.forEach((book, score) ->
                System.out.println(book.getMetadata().getTitle() + " -> " + score)
        );

        assertEquals(4, rrf.size());

        var expectedRanking = List.of(thirdBook, firstBook, secondBook);

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
