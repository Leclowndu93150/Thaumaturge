package com.leclowndu93150.thaumaturge.server.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.List;
import org.junit.jupiter.api.Test;

class AspectPairSuggestionsTest {
    @Test
    void suggestsNextAspectAfterCompletedPair() {
        var suggestions = AspectPairSuggestions.suggest(
                        new SuggestionsBuilder("aer 5 i", 0), List.of("aer", "ignis", "terra"))
                .join();

        assertEquals(
                List.of("ignis"),
                suggestions.getList().stream()
                        .map(suggestion -> suggestion.getText())
                        .toList());
        assertEquals(6, suggestions.getRange().getStart());
    }

    @Test
    void suggestsFirstAspectWhenInputEndsAfterAmount() {
        var suggestions = AspectPairSuggestions.suggest(new SuggestionsBuilder("aer 5 ", 0), List.of("aer", "ignis"))
                .join();

        assertEquals(
                List.of("aer", "ignis"),
                suggestions.getList().stream()
                        .map(suggestion -> suggestion.getText())
                        .toList());
        assertEquals(6, suggestions.getRange().getStart());
    }

    @Test
    void doesNotSuggestAspectsWhereAnAmountIsExpected() {
        var suggestions = AspectPairSuggestions.suggest(new SuggestionsBuilder("aer ", 0), List.of("aer", "ignis"))
                .join();

        assertTrue(suggestions.isEmpty());
    }
}
