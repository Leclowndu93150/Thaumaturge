package com.leclowndu93150.thaumaturge.server.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class AspectPairArgumentsTest {
    private static final SuggestionProvider<Object> NO_SUGGESTIONS = (context, builder) -> builder.buildFuture();

    @Test
    void aspectArgumentIsFollowedByAnAmountParameter() {
        var dispatcher = new CommandDispatcher<Object>();
        dispatcher.register(LiteralArgumentBuilder.<Object>literal("reservoir").then(arguments(context -> 1)));

        var parse = dispatcher.parse("reservoir aer ", new Object());
        var activeNode = parse.getContext().getNodes().getLast().getNode();

        assertTrue(activeNode.getChild("amount") != null);
        assertTrue(dispatcher.getSmartUsage(activeNode, new Object()).values().stream()
                .anyMatch(usage -> usage.contains("<amount>")));

        var secondPair = dispatcher.parse("reservoir aer 5 ignis ", new Object());
        var secondAspect = secondPair.getContext().getNodes().getLast().getNode();
        assertTrue(secondAspect.getChild("amount_2") != null);
        assertTrue(dispatcher.getSmartUsage(secondAspect, new Object()).values().stream()
                .anyMatch(usage -> usage.contains("<amount_2>")));
    }

    @Test
    void parsesRepeatedAspectAmountPairsInOrder() throws Exception {
        AtomicReference<String> parsed = new AtomicReference<>();
        var dispatcher = new CommandDispatcher<Object>();
        dispatcher.register(LiteralArgumentBuilder.<Object>literal("reservoir").then(arguments(context -> {
            parsed.set(AspectPairArguments.getPairs(context));
            return 1;
        })));

        dispatcher.execute("reservoir aer 5 ignis 3 terra 2 ordo 1", new Object());

        assertEquals("aer 5 ignis 3 terra 2 ordo 1", parsed.get());
    }

    @Test
    void keepsParsedArgumentRangesOrderedWhenARepeatedPairIsIncomplete() {
        var dispatcher = new CommandDispatcher<Object>();
        dispatcher.register(LiteralArgumentBuilder.<Object>literal("reservoir").then(arguments(context -> 1)));

        var parse = dispatcher.parse("reservoir aer 5 ignis ", new Object());
        var arguments = parse.getContext().getArguments().values().stream().toList();

        for (int index = 1; index < arguments.size(); index++) {
            assertTrue(
                    arguments.get(index).getRange().getStart()
                            >= arguments.get(index - 1).getRange().getEnd(),
                    "Parsed argument ranges should stay in command order");
        }
    }

    private static RequiredArgumentBuilder<Object, String> arguments(com.mojang.brigadier.Command<Object> command) {
        return AspectPairArguments.create(3, NO_SUGGESTIONS, NO_SUGGESTIONS, command);
    }
}
