package com.github.torleifg.bookquest.adapter.persistence;

import com.github.torleifg.bookquest.core.domain.Book;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

record ReciprocalRankFusion(List<RankedSearchHit> rankedSearchHits) {

    Map<Book, Double> compute() {
        final int K = 60;

        final Set<Book> books = rankedSearchHits.stream()
                .flatMap(rankedSearchHit -> rankedSearchHit.hits().keySet().stream())
                .collect(toSet());

        final Map<Book, Double> bookScores = books.stream()
                .collect(toMap(
                        book -> book,
                        book -> calculateScore(book, K)
                ));

        return bookScores.entrySet().stream()
                .sorted(this::compareEntries)
                .collect(toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }

    private double calculateScore(Book book, int K) {
        return rankedSearchHits.stream()
                .mapToDouble(rankedSearchHit -> {
                    final Integer rank = rankedSearchHit.hits().get(book);

                    return rank != null ? rankedSearchHit.weight() * (1.0 / (K + rank)) : 0.0;
                })
                .sum();
    }

    private int compareEntries(Map.Entry<Book, Double> firstEntry, Map.Entry<Book, Double> secondEntry) {
        final int compared = secondEntry.getValue().compareTo(firstEntry.getValue());

        if (compared != 0) return compared;

        return firstEntry.getKey().getMetadata().getTitle()
                .compareTo(secondEntry.getKey().getMetadata().getTitle());
    }

    static RankedSearchHit toRankedSearchHit(List<Book> books, double weight) {
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
