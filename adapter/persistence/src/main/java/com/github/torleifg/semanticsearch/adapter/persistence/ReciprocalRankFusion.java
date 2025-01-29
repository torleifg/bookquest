package com.github.torleifg.semanticsearch.adapter.persistence;

import com.github.torleifg.semanticsearch.book.domain.Book;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toMap;

record ReciprocalRankFusion(String query, List<Book> firstSearchHits, List<Book> secondSearchHits) {

    Map<Book, Double> compute() {
        final double weight = computeFullTextWeight(query);

        return mergeSearchHits(
                toRankedMap(firstSearchHits),
                toRankedMap(secondSearchHits),
                weight);
    }

    private Map<Book, Integer> toRankedMap(List<Book> sortedList) {
        return IntStream.range(0, sortedList.size())
                .boxed()
                .collect(Collectors.toMap(
                        sortedList::get,
                        index -> index + 1,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }

    private Map<Book, Double> mergeSearchHits(Map<Book, Integer> fullTextResults, Map<Book, Integer> vectorResults, double fullTextWeight) {
        final int K = 60;

        final Map<Book, Double> mergedSearchHits = new HashMap<>();

        fullTextResults.forEach((book, rank) ->
                mergedSearchHits.merge(book, fullTextWeight * (1.0 / (K + rank)), Double::sum)
        );

        vectorResults.forEach((book, rank) ->
                mergedSearchHits.merge(book, (1 - fullTextWeight) * (1.0 / (K + rank)), Double::sum)
        );

        return mergedSearchHits.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }

    private double computeFullTextWeight(String text) {
        if (text == null || text.isBlank()) {
            return 0.5;
        }

        final String[] words = text.trim().split("\\s+");

        return words.length > 3 ? 0.5 : 0.6;
    }
}
