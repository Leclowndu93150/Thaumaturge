package com.leclowndu93150.thaumaturge.server.command.admin;

import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import com.leclowndu93150.thaumaturge.api.research.CategoryComponents;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.IntBinaryOperator;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.level.ServerPlayer;

final class KnowledgeSubcommand implements AdminSubcommand {
    private static final String TARGETS = "targets";
    private static final String CATEGORY = "category";
    private static final String POINTS = "points";
    private static final int MAX_POINTS = 1000;

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build(CommandBuildContext context) {
        RequiredArgumentBuilder<CommandSourceStack, EntitySelector> targets = Commands.argument(TARGETS, EntityArgument.players());
        for (Operation operation : Operation.values()) {
            targets.then(operation(operation));
        }
        targets.then(Commands.literal("query").executes(KnowledgeSubcommand::query));
        return Commands.literal("knowledge").then(targets);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> operation(Operation operation) {
        LiteralArgumentBuilder<CommandSourceStack> node = Commands.literal(operation.literal);
        for (KnowledgeType type : KnowledgeType.values()) {
            node.then(Commands.literal(type.getSerializedName())
                    .then(Commands.literal("all").then(Commands.argument(POINTS, IntegerArgumentType.integer(0, MAX_POINTS)).executes(ctx -> apply(ctx, operation, type, true))))
                    .then(Commands.argument(CATEGORY, ResourceKeyArgument.key(IResearchCategory.REGISTRY_KEY))
                            .then(Commands.argument(POINTS, IntegerArgumentType.integer(0, MAX_POINTS)).executes(ctx -> apply(ctx, operation, type, false)))));
        }
        return node;
    }

    private static int apply(CommandContext<CommandSourceStack> ctx, Operation operation, KnowledgeType type, boolean all) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, TARGETS);
        int points = IntegerArgumentType.getInteger(ctx, POINTS);
        List<Holder.Reference<IResearchCategory>> categories = all
                ? CommandSupport.categories(ctx.getSource().registryAccess())
                : List.of(CommandSupport.holder(ctx, CATEGORY, IResearchCategory.REGISTRY_KEY, CommandSupport.UNKNOWN_CATEGORY));
        int raw = points * type.progression();
        for (ServerPlayer player : targets) {
            IPlayerKnowledge knowledge = KnowledgeAccess.of(player);
            for (Holder.Reference<IResearchCategory> category : categories) {
                knowledge.addKnowledge(type, category.key(), operation.delta.applyAsInt(knowledge.rawKnowledge(type, category.key()), raw));
            }
            knowledge.sync(player);
        }
        Component where = all ? Component.translatable("commands.thaumaturge.knowledge.all_categories") : CategoryComponents.name(categories.getFirst().key());
        ctx.getSource().sendSuccess(() -> Component.translatable(operation.messageKey, points, Component.translatable(type.translationKey()), where, CommandSupport.describe(targets)), true);
        return targets.size();
    }

    private static int query(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, TARGETS);
        CommandSourceStack source = ctx.getSource();
        List<Holder.Reference<IResearchCategory>> categories = CommandSupport.categories(source.registryAccess());
        for (ServerPlayer player : targets) {
            IPlayerKnowledge knowledge = KnowledgeAccess.of(player);
            source.sendSuccess(() -> Component.translatable("commands.thaumaturge.knowledge.query", player.getDisplayName()), false);
            for (Holder.Reference<IResearchCategory> category : categories) {
                List<Component> parts = new ArrayList<>();
                for (KnowledgeType type : KnowledgeType.values()) {
                    parts.add(Component.translatable("commands.thaumaturge.knowledge.query.entry", knowledge.knowledge(type, category.key()), Component.translatable(type.translationKey())));
                }
                Component line = Component.translatable("commands.thaumaturge.knowledge.query.category", CategoryComponents.name(category.key()),
                        ComponentUtils.formatList(parts, CommandSupport.separator()));
                source.sendSuccess(() -> line, false);
            }
        }
        return targets.size();
    }

    private enum Operation {
        ADD("add", "commands.thaumaturge.knowledge.add", (current, amount) -> amount), REMOVE("remove", "commands.thaumaturge.knowledge.remove", (current, amount) -> -amount), SET("set",
                "commands.thaumaturge.knowledge.set", (current, amount) -> amount - current);

        private final String literal;
        private final String messageKey;
        private final IntBinaryOperator delta;

        Operation(String literal, String messageKey, IntBinaryOperator delta) {
            this.literal = literal;
            this.messageKey = messageKey;
            this.delta = delta;
        }
    }
}
