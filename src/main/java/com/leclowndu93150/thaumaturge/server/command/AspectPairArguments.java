package com.leclowndu93150.thaumaturge.server.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.ArrayList;
import java.util.List;

final class AspectPairArguments {
    private AspectPairArguments() {}

    static <S> RequiredArgumentBuilder<S, String> create(
            int visiblePairCount,
            SuggestionProvider<S> aspectSuggestions,
            SuggestionProvider<S> continuationSuggestions,
            Command<S> execute) {
        if (visiblePairCount < 1) {
            throw new IllegalArgumentException("At least one aspect pair must be visible");
        }
        return pair(1, visiblePairCount, aspectSuggestions, continuationSuggestions, execute);
    }

    static String getPairs(CommandContext<?> context) {
        List<String> tokens = new ArrayList<>();
        String input = context.getInput();
        context.getNodes().forEach(parsed -> {
            String name = parsed.getNode().getName();
            if (name.equals("aspect")
                    || name.startsWith("aspect_")
                    || name.equals("amount")
                    || name.startsWith("amount_")
                    || name.equals("additional_aspects")) {
                tokens.add(input.substring(
                        parsed.getRange().getStart(), parsed.getRange().getEnd()));
            }
        });
        return String.join(" ", tokens);
    }

    private static <S> RequiredArgumentBuilder<S, String> pair(
            int pairNumber,
            int visiblePairCount,
            SuggestionProvider<S> aspectSuggestions,
            SuggestionProvider<S> continuationSuggestions,
            Command<S> execute) {
        var amount = RequiredArgumentBuilder.<S, Integer>argument(
                        argumentName("amount", pairNumber), IntegerArgumentType.integer(1))
                .executes(execute);
        if (pairNumber < visiblePairCount) {
            amount.then(pair(pairNumber + 1, visiblePairCount, aspectSuggestions, continuationSuggestions, execute));
        } else {
            amount.then(
                    RequiredArgumentBuilder.<S, String>argument("additional_aspects", StringArgumentType.greedyString())
                            .suggests(continuationSuggestions)
                            .executes(execute));
        }
        return RequiredArgumentBuilder.<S, String>argument(
                        argumentName("aspect", pairNumber), StringArgumentType.word())
                .suggests(aspectSuggestions)
                .then(amount);
    }

    private static String argumentName(String base, int pairNumber) {
        return pairNumber == 1 ? base : base + "_" + pairNumber;
    }
}
