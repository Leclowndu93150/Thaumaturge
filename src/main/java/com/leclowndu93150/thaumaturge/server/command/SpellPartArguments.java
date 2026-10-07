package com.leclowndu93150.thaumaturge.server.command;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.spell.CastStyle;
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public final class SpellPartArguments {
    public static final SuggestionProvider<CommandSourceStack> SUGGESTIONS = (ctx, builder) -> SharedSuggestionProvider
            .suggest(ctx.getSource().registryAccess().lookupOrThrow(SpellPart.REGISTRY_KEY).listElementIds().map(key -> key.identifier().toString()), builder);

    private static final String ASPECT_SEPARATOR = "@";
    private static final DynamicCommandExceptionType UNKNOWN_PART = new DynamicCommandExceptionType(value -> Component.literal("Unknown spell part: " + value));

    private SpellPartArguments() {}

    public static Spell chain(String raw, HolderLookup.Provider registries) throws CommandSyntaxException {
        List<SpellNode> nodes = new ArrayList<>();
        HolderLookup.RegistryLookup<SpellPart> parts = registries.lookupOrThrow(SpellPart.REGISTRY_KEY);
        CastStyle style = CastStyle.INSTANT;
        for (String token : raw.trim().split("\\s+")) {
            Optional<CastStyle> named = styleNamed(token);
            if (nodes.isEmpty() && named.isPresent()) {
                style = named.get();
                continue;
            }
            String[] pieces = token.split(ASPECT_SEPARATOR, 2);
            ResourceKey<SpellPart> key = ResourceKey.create(SpellPart.REGISTRY_KEY, id(pieces[0]));
            if (parts.get(key).isEmpty()) {
                throw UNKNOWN_PART.create(key.identifier());
            }
            Optional<ResourceKey<IAspect>> aspect = pieces.length > 1 ? Optional.of(ResourceKey.create(IAspect.REGISTRY_KEY, id(pieces[1]))) : Optional.empty();
            nodes.add(SpellNode.of(key).withAspect(aspect));
        }
        SpellNode chain = null;
        for (int index = nodes.size() - 1; index >= 0; index--) {
            chain = chain == null ? nodes.get(index) : nodes.get(index).then(chain);
        }
        return new Spell(style, SpellNode.of(Spell.ORIGIN).then(chain));
    }

    private static Optional<CastStyle> styleNamed(String token) {
        for (CastStyle style : CastStyle.values()) {
            if (style.getSerializedName().equals(token)) {
                return Optional.of(style);
            }
        }
        return Optional.empty();
    }

    private static Identifier id(String token) {
        return token.contains(":") ? Identifier.parse(token) : Identifier.fromNamespaceAndPath(TTIds.MODID, token);
    }
}
