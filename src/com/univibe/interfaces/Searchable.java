package com.univibe.interfaces;

/**
 * Interface representing searchable entities within the UniVibe catalog.
 * Demonstrates: Interfaces, Method Contracts.
 */
public interface Searchable {
    boolean matchesKeyword(String keyword);
    String getSearchCategory();
}
