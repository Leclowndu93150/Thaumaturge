package com.leclowndu93150.thaumaturge.server.command;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.casters.FocusEngine;
import com.leclowndu93150.thaumaturge.registry.TCFocusElements;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class FocusElementArguments {
    public static final SuggestionProvider<CommandSourceStack> SUGGESTIONS =
            (ctx, builder) -> SharedSuggestionProvider.suggest(
                    TCFocusElements.registry().keySet().stream().map(ResourceLocation::toString), builder);

    private static final DynamicCommandExceptionType ERROR_UNKNOWN_ELEMENT =
            new DynamicCommandExceptionType(value -> Component.literal("Unknown focus element: " + value));

    private FocusElementArguments() {}

    public static List<ResourceLocation> parse(String raw) throws CommandSyntaxException {
        List<ResourceLocation> elements = new ArrayList<>();
        for (String token : raw.trim().split("\\s+")) {
            ResourceLocation id = token.contains(":")
                    ? ResourceLocation.parse(token)
                    : ResourceLocation.fromNamespaceAndPath(TCIds.MODID, token);
            if (FocusEngine.element(id) == null) {
                throw ERROR_UNKNOWN_ELEMENT.create(id);
            }
            elements.add(id);
        }
        return elements;
    }
}
