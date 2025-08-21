package com.github.torleifg.bookquest.core.service;

import com.github.torleifg.bookquest.core.domain.Book;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

record ReciprocalRankFusion(List<RankedSearchHit> rankedSearchHits) {

    Map<Book, Double> compute() {
        final int K = 60;

        final Map<Book, Double> bookScores = rankedSearchHits.stream()
                .flatMap(rankedSearchHit -> rankedSearchHit.hits().keySet().stream())
                .distinct()
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
}
