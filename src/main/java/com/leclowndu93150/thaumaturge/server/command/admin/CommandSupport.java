package com.leclowndu93150.thaumaturge.server.command.admin;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToIntFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;

final class CommandSupport {
    static final DynamicCommandExceptionType UNKNOWN_ENTRY = new DynamicCommandExceptionType(id -> Component.translatable("commands.thaumaturge.research.unknown_entry", id));
    static final DynamicCommandExceptionType UNKNOWN_CATEGORY = new DynamicCommandExceptionType(id -> Component.translatable("commands.thaumaturge.research.unknown_category", id));
    static final DynamicCommandExceptionType UNKNOWN_ASPECT = new DynamicCommandExceptionType(id -> Component.translatable("commands.thaumaturge.aspects.unknown", id));

    private CommandSupport() {}

    static Component describe(Collection<ServerPlayer> targets) {
        if (targets.size() == 1) {
            return targets.iterator().next().getDisplayName();
        }
        return Component.translatable("commands.thaumaturge.targets", targets.size());
    }

    static Component separator() {
        return Component.translatable("commands.thaumaturge.separator");
    }

    static int forEach(Collection<ServerPlayer> targets, ToIntFunction<ServerPlayer> action) {
        int total = 0;
        for (ServerPlayer player : targets) {
            total += action.applyAsInt(player);
        }
        return total;
    }

    static <T> Holder.Reference<T> holder(CommandContext<CommandSourceStack> ctx, String name, ResourceKey<Registry<T>> registry, DynamicCommandExceptionType unknown) throws CommandSyntaxException {
        ResourceKey<T> key = ResourceKeyArgument.getRegistryKey(ctx, name, registry, unknown);
        return ctx.getSource().registryAccess().lookupOrThrow(registry).get(key).orElseThrow(() -> unknown.create(key.identifier()));
    }

    static <T> SuggestionProvider<CommandSourceStack> ownKeys(ResourceKey<? extends Registry<T>> registry) {
        return (ctx, builder) -> SharedSuggestionProvider
                .suggestResource(ctx.getSource().registryAccess().lookupOrThrow(registry).listElementIds().map(ResourceKey::identifier).filter(id -> id.getNamespace().equals(TCIds.MODID)), builder);
    }

    static List<Holder.Reference<IResearchCategory>> categories(RegistryAccess access) {
        return access.lookupOrThrow(IResearchCategory.REGISTRY_KEY).listElements().sorted(Comparator.comparingInt(category -> category.value().index())).toList();
    }
}
