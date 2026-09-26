package com.leclowndu93150.thaumaturge.server.command.admin;

import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.research.CategoryComponents;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.content.research.ResearchGrants;
import com.leclowndu93150.thaumaturge.content.research.ResearchProgression;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.List;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

final class ResearchSubcommand implements AdminSubcommand {
    private static final String TARGETS = "targets";
    private static final String ENTRY = "entry";
    private static final String CATEGORY = "category";

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build(CommandBuildContext context) {
        return Commands.literal("research")
                .then(Commands.argument(TARGETS, EntityArgument.players()).then(Commands.literal("reset").executes(ResearchSubcommand::reset))
                        .then(Commands.literal("everything").executes(ResearchSubcommand::everything))
                        .then(Commands.literal("stage").then(Commands.argument(CATEGORY, ResourceKeyArgument.key(IResearchCategory.REGISTRY_KEY)).executes(ResearchSubcommand::stage)))
                        .then(Commands.literal("category").then(Commands.argument(CATEGORY, ResourceKeyArgument.key(IResearchCategory.REGISTRY_KEY)).executes(ResearchSubcommand::category)))
                        .then(Commands.literal("grant").then(Commands.argument(ENTRY, ResourceKeyArgument.key(IResearchEntry.REGISTRY_KEY)).executes(ResearchSubcommand::grant)))
                        .then(Commands.literal("ready").then(Commands.argument(ENTRY, ResourceKeyArgument.key(IResearchEntry.REGISTRY_KEY)).executes(ResearchSubcommand::ready)))
                        .then(Commands.literal("revoke").then(Commands.argument(ENTRY, ResourceKeyArgument.key(IResearchEntry.REGISTRY_KEY)).executes(ResearchSubcommand::revoke)))
                        .then(Commands.literal("query").executes(ResearchSubcommand::query)));
    }

    private static int reset(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, TARGETS);
        targets.forEach(ResearchProgression::reset);
        ctx.getSource().sendSuccess(() -> Component.translatable("commands.thaumaturge.research.reset", CommandSupport.describe(targets)), true);
        return targets.size();
    }

    private static int everything(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, TARGETS);
        int completed = CommandSupport.forEach(targets, ResearchGrants::grantAll);
        ctx.getSource().sendSuccess(() -> Component.translatable("commands.thaumaturge.research.everything", CommandSupport.describe(targets), completed), true);
        return targets.size();
    }

    private static int stage(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, TARGETS);
        Holder.Reference<IResearchCategory> category = CommandSupport.holder(ctx, CATEGORY, IResearchCategory.REGISTRY_KEY, CommandSupport.UNKNOWN_CATEGORY);
        int completed = CommandSupport.forEach(targets, player -> ResearchProgression.setStage(player, category));
        ctx.getSource().sendSuccess(() -> Component.translatable("commands.thaumaturge.research.stage", CommandSupport.describe(targets), CategoryComponents.name(category.key()), completed), true);
        return targets.size();
    }

    private static int category(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, TARGETS);
        Holder.Reference<IResearchCategory> category = CommandSupport.holder(ctx, CATEGORY, IResearchCategory.REGISTRY_KEY, CommandSupport.UNKNOWN_CATEGORY);
        int completed = CommandSupport.forEach(targets, player -> ResearchProgression.completeCategory(player, category.key()));
        ctx.getSource().sendSuccess(() -> Component.translatable("commands.thaumaturge.research.category", CategoryComponents.name(category.key()), CommandSupport.describe(targets), completed), true);
        return targets.size();
    }

    private static int grant(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, TARGETS);
        Holder.Reference<IResearchEntry> entry = CommandSupport.holder(ctx, ENTRY, IResearchEntry.REGISTRY_KEY, CommandSupport.UNKNOWN_ENTRY);
        int completed = CommandSupport.forEach(targets, player -> ResearchProgression.grant(player, entry.key().identifier()));
        ctx.getSource().sendSuccess(() -> Component.translatable("commands.thaumaturge.research.grant", entryName(entry), CommandSupport.describe(targets), completed), true);
        return targets.size();
    }

    private static int ready(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, TARGETS);
        Holder.Reference<IResearchEntry> entry = CommandSupport.holder(ctx, ENTRY, IResearchEntry.REGISTRY_KEY, CommandSupport.UNKNOWN_ENTRY);
        int completed = CommandSupport.forEach(targets, player -> ResearchProgression.prepare(player, entry.key().identifier()));
        ctx.getSource().sendSuccess(() -> Component.translatable("commands.thaumaturge.research.ready", entryName(entry), CommandSupport.describe(targets), completed), true);
        return targets.size();
    }

    private static int revoke(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, TARGETS);
        Holder.Reference<IResearchEntry> entry = CommandSupport.holder(ctx, ENTRY, IResearchEntry.REGISTRY_KEY, CommandSupport.UNKNOWN_ENTRY);
        int removed = CommandSupport.forEach(targets, player -> ResearchProgression.revoke(player, entry.key().identifier()));
        ctx.getSource().sendSuccess(() -> Component.translatable("commands.thaumaturge.research.revoke", entryName(entry), removed, CommandSupport.describe(targets)), true);
        return targets.size();
    }

    private static int query(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, TARGETS);
        CommandSourceStack source = ctx.getSource();
        List<Holder.Reference<IResearchEntry>> entries = source.registryAccess().lookupOrThrow(IResearchEntry.REGISTRY_KEY).listElements().toList();
        List<Holder.Reference<IResearchCategory>> categories = CommandSupport.categories(source.registryAccess());
        for (ServerPlayer player : targets) {
            IPlayerKnowledge knowledge = KnowledgeAccess.of(player);
            int total = countComplete(knowledge, entries, null);
            source.sendSuccess(() -> Component.translatable("commands.thaumaturge.research.query", player.getDisplayName(), total, entries.size()), false);
            for (Holder.Reference<IResearchCategory> category : categories) {
                int inCategory = (int) entries.stream().filter(entry -> entry.value().category().is(category.key())).count();
                int done = countComplete(knowledge, entries, category);
                source.sendSuccess(() -> Component.translatable("commands.thaumaturge.research.query.category", CategoryComponents.name(category.key()), done, inCategory), false);
            }
        }
        return targets.size();
    }

    private static int countComplete(IPlayerKnowledge knowledge, List<Holder.Reference<IResearchEntry>> entries, Holder.@Nullable Reference<IResearchCategory> category) {
        int count = 0;
        for (Holder.Reference<IResearchEntry> entry : entries) {
            if ((category == null || entry.value().category().is(category.key())) && knowledge.isResearchComplete(entry.key().identifier())) {
                count++;
            }
        }
        return count;
    }

    private static Component entryName(Holder.Reference<IResearchEntry> entry) {
        return Component.translatable(entry.value().nameKey());
    }
}
