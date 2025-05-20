package com.github.torleifg.bookquest.adapter.persistence;

import com.github.torleifg.bookquest.core.domain.Book;

import java.util.Map;

record RankedSearchHit(Map<Book, Integer> hits, double weight) {
}
