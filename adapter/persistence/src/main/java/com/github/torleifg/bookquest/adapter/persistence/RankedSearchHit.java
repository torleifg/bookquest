package com.github.torleifg.bookquest.adapter.persistence;

import com.github.torleifg.bookquest.core.domain.Book;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toMap;

record RankedSearchHit(Map<Book, Integer> hits, double weight) {

    static RankedSearchHit from(List<Book> books, double weight) {
        final Map<Book, Integer> ranked = IntStream.range(0, books.size())
                .boxed()
                .collect(toMap(
                        books::get,
                        index -> index + 1,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));

        return new RankedSearchHit(ranked, weight);
    }
}
