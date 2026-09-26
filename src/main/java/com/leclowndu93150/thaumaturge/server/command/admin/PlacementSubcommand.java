package com.leclowndu93150.thaumaturge.server.command.admin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.commands.PlaceCommand;

final class PlacementSubcommand<T> implements AdminSubcommand {
    private static final String ID = "id";
    private static final String POS = "pos";

    private final String name;
    private final ResourceKey<? extends Registry<T>> registry;
    private final Placer placer;

    private PlacementSubcommand(String name, ResourceKey<? extends Registry<T>> registry, Placer placer) {
        this.name = name;
        this.registry = registry;
        this.placer = placer;
    }

    static PlacementSubcommand<?> structures() {
        return new PlacementSubcommand<>("structure", Registries.STRUCTURE, (ctx, pos) -> PlaceCommand.placeStructure(ctx.getSource(), ResourceKeyArgument.getStructure(ctx, ID), pos));
    }

    static PlacementSubcommand<?> features() {
        return new PlacementSubcommand<>("feature", Registries.CONFIGURED_FEATURE, (ctx, pos) -> PlaceCommand.placeFeature(ctx.getSource(), ResourceKeyArgument.getConfiguredFeature(ctx, ID), pos));
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build(CommandBuildContext context) {
        return Commands.literal(name)
                .then(Commands.argument(ID, ResourceKeyArgument.key(registry)).suggests(CommandSupport.ownKeys(registry))
                        .executes(ctx -> placer.place(ctx, BlockPos.containing(ctx.getSource().getPosition())))
                        .then(Commands.argument(POS, BlockPosArgument.blockPos()).executes(ctx -> placer.place(ctx, BlockPosArgument.getLoadedBlockPos(ctx, POS)))));
    }

    @FunctionalInterface
    private interface Placer {
        int place(CommandContext<CommandSourceStack> ctx, BlockPos pos) throws CommandSyntaxException;
    }
}
