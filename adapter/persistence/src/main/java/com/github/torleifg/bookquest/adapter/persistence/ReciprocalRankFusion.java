package com.github.torleifg.bookquest.adapter.persistence;

import com.github.torleifg.bookquest.core.domain.Book;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toMap;

record ReciprocalRankFusion(List<Book> fullText, List<Book> semantic, double fullTextWeight, double semanticWeight) {

    Map<Book, Double> compute() {
        return mergeSearchHits(toRankedMap(fullText), toRankedMap(semantic), fullTextWeight, semanticWeight);
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

    private Map<Book, Double> mergeSearchHits(Map<Book, Integer> fullText, Map<Book, Integer> semantic, double fullTextWeight, double semanticWeight) {
        final int K = 60;

        final Map<Book, Double> mergedSearchHits = new HashMap<>();

        fullText.forEach((book, rank) ->
                mergedSearchHits.merge(book, fullTextWeight * (1.0 / (K + rank)), Double::sum)
        );

        semantic.forEach((book, rank) ->
                mergedSearchHits.merge(book, semanticWeight * (1.0 / (K + rank)), Double::sum)
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
}
