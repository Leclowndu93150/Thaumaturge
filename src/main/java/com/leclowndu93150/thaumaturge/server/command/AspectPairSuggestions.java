package com.leclowndu93150.thaumaturge.server.command;

import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.SharedSuggestionProvider;

final class AspectPairSuggestions {
    private AspectPairSuggestions() {}

    static CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggest(
            SuggestionsBuilder builder, Iterable<String> aspects) {
        String remaining = builder.getRemaining();
        int tokenStart = remaining.lastIndexOf(' ') + 1;
        String completedTokens = remaining.substring(0, tokenStart).trim();
        int tokenCount = completedTokens.isEmpty() ? 0 : completedTokens.split("\\s+").length;
        if (tokenCount % 2 != 0) {
            return builder.buildFuture();
        }
        return SharedSuggestionProvider.suggest(aspects, builder.createOffset(builder.getStart() + tokenStart));
    }
}
