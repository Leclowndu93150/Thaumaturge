package com.leclowndu93150.thaumaturge.server.command.admin;

import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.research.ResearchGrants;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;

final class AspectSubcommand implements AdminSubcommand {
    private static final String TARGETS = "targets";
    private static final String ASPECT = "aspect";
    private static final String AMOUNT = "amount";
    private static final int MAX_AMOUNT = 10000;

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build(CommandBuildContext context) {
        return Commands.literal("aspects")
                .then(Commands.argument(TARGETS, EntityArgument.players()).then(selection("give", true, AspectSubcommand::give)).then(selection("take", true, AspectSubcommand::take))
                        .then(selection("set", true, AspectSubcommand::set)).then(selection("discover", false, AspectSubcommand::discover))
                        .then(Commands.literal("reset").executes(AspectSubcommand::reset)).then(Commands.literal("query").executes(AspectSubcommand::query)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> selection(String name, boolean withAmount, AspectAction action) {
        return Commands.literal(name).then(tail(Commands.literal("all"), withAmount, ctx -> action.run(ctx, allAspects(ctx), true)))
                .then(tail(Commands.argument(ASPECT, ResourceKeyArgument.key(IAspect.REGISTRY_KEY)), withAmount,
                        ctx -> action.run(ctx, List.of(CommandSupport.holder(ctx, ASPECT, IAspect.REGISTRY_KEY, CommandSupport.UNKNOWN_ASPECT)), false)));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> tail(ArgumentBuilder<CommandSourceStack, ?> node, boolean withAmount, Command<CommandSourceStack> command) {
        if (withAmount) {
            return node.then(Commands.argument(AMOUNT, IntegerArgumentType.integer(0, MAX_AMOUNT)).executes(command));
        }
        return node.executes(command);
    }

    private static List<Holder.Reference<IAspect>> allAspects(CommandContext<CommandSourceStack> ctx) {
        return ctx.getSource().registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).listElements().toList();
    }

    private static int give(CommandContext<CommandSourceStack> ctx, List<Holder.Reference<IAspect>> aspects, boolean all) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, TARGETS);
        int amount = IntegerArgumentType.getInteger(ctx, AMOUNT);
        for (ServerPlayer player : targets) {
            if (all) {
                AspectPools.grantAllForCommand(player, amount);
            } else {
                AspectPools.grantForCommand(player, aspects.getFirst(), amount);
            }
        }
        ctx.getSource().sendSuccess(() -> Component.translatable("commands.thaumaturge.aspects.give", amount, name(aspects, all), CommandSupport.describe(targets)), true);
        return targets.size();
    }

    private static int take(CommandContext<CommandSourceStack> ctx, List<Holder.Reference<IAspect>> aspects, boolean all) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, TARGETS);
        int amount = IntegerArgumentType.getInteger(ctx, AMOUNT);
        targets.forEach(player -> AspectPools.takeForCommand(player, aspects, amount));
        ctx.getSource().sendSuccess(() -> Component.translatable("commands.thaumaturge.aspects.take", amount, name(aspects, all), CommandSupport.describe(targets)), true);
        return targets.size();
    }

    private static int set(CommandContext<CommandSourceStack> ctx, List<Holder.Reference<IAspect>> aspects, boolean all) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, TARGETS);
        int amount = IntegerArgumentType.getInteger(ctx, AMOUNT);
        targets.forEach(player -> AspectPools.setForCommand(player, aspects, amount));
        ctx.getSource().sendSuccess(() -> Component.translatable("commands.thaumaturge.aspects.set", name(aspects, all), amount, CommandSupport.describe(targets)), true);
        return targets.size();
    }

    private static int discover(CommandContext<CommandSourceStack> ctx, List<Holder.Reference<IAspect>> aspects, boolean all) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, TARGETS);
        targets.forEach(player -> ResearchGrants.discoverAspects(player, aspects));
        ctx.getSource().sendSuccess(() -> Component.translatable("commands.thaumaturge.aspects.discover", name(aspects, all), CommandSupport.describe(targets)), true);
        return targets.size();
    }

    private static int reset(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, TARGETS);
        targets.forEach(ResearchGrants::forgetAspects);
        ctx.getSource().sendSuccess(() -> Component.translatable("commands.thaumaturge.aspects.reset", CommandSupport.describe(targets)), true);
        return targets.size();
    }

    private static int query(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, TARGETS);
        CommandSourceStack source = ctx.getSource();
        HolderLookup.RegistryLookup<IAspect> registry = source.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY);
        for (ServerPlayer player : targets) {
            List<Component> parts = new ArrayList<>();
            for (Map.Entry<Identifier, Integer> entry : AspectPools.data(player).pool().entrySet()) {
                registry.get(ResourceKey.create(IAspect.REGISTRY_KEY, entry.getKey()))
                        .ifPresent(aspect -> parts.add(Component.translatable("commands.thaumaturge.aspects.query.entry", AspectComponents.trueName(aspect), entry.getValue())));
            }
            Component message = parts.isEmpty()
                    ? Component.translatable("commands.thaumaturge.aspects.query.none", player.getDisplayName())
                    : Component.translatable("commands.thaumaturge.aspects.query", player.getDisplayName(), parts.size(), ComponentUtils.formatList(parts, CommandSupport.separator()));
            source.sendSuccess(() -> message, false);
        }
        return targets.size();
    }

    private static Component name(List<Holder.Reference<IAspect>> aspects, boolean all) {
        return all ? Component.translatable("commands.thaumaturge.aspects.every") : AspectComponents.trueName(aspects.getFirst());
    }

    @FunctionalInterface
    private interface AspectAction {
        int run(CommandContext<CommandSourceStack> ctx, List<Holder.Reference<IAspect>> aspects, boolean all) throws CommandSyntaxException;
    }
}
