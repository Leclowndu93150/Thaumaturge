package com.leclowndu93150.thaumaturge.server.command.admin;

import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.List;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;

final class ShowcaseSubcommand implements AdminSubcommand {
    private static final String POS = "pos";
    private static final SimpleCommandExceptionType TOO_TALL = new SimpleCommandExceptionType(Component.translatable("commands.thaumaturge.showcase.too_tall"));
    private static final SimpleCommandExceptionType UNLOADED = new SimpleCommandExceptionType(Component.translatable("commands.thaumaturge.showcase.unloaded"));

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build(CommandBuildContext context) {
        return Commands.literal("showcase").executes(ctx -> build(ctx.getSource(), BlockPos.containing(ctx.getSource().getPosition())))
                .then(Commands.argument(POS, BlockPosArgument.blockPos()).executes(ctx -> build(ctx.getSource(), BlockPosArgument.getLoadedBlockPos(ctx, POS))));
    }

    private static int build(CommandSourceStack source, BlockPos origin) throws CommandSyntaxException {
        List<Block> blocks = TCBlocks.BLOCKS.getEntries().stream().<Block>map(DeferredHolder::value).toList();
        List<Item> items = TCItems.ITEMS.getEntries().stream().<Item>map(DeferredHolder::value).toList();
        ShowcaseBuilder builder = new ShowcaseBuilder(source.getLevel(), origin, Direction.fromYRot(source.getRotation().y), blocks, items);
        if (!builder.fitsHeight()) {
            throw TOO_TALL.create();
        }
        if (!builder.isLoaded()) {
            throw UNLOADED.create();
        }
        builder.build();
        source.sendSuccess(() -> Component.translatable("commands.thaumaturge.showcase.built", blocks.size(), items.size(), origin.getX(), origin.getY(), origin.getZ()), true);
        return blocks.size() + items.size();
    }
}
