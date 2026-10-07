package com.leclowndu93150.thaumaturge.server.command.admin;

import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

final class BuildSubcommand implements AdminSubcommand {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build(CommandBuildContext context) {
        return Commands.literal("build").requires(source -> source.getEntity() instanceof ServerPlayer player && player.isCreative())
                .then(Commands.literal("infusion_altar").executes(ctx -> buildAltar(ctx.getSource())));
    }

    private static int buildAltar(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = source.getLevel();
        BlockPos center = player.blockPosition().relative(player.getDirection(), 4);
        if (center.getY() < level.getMinY() || center.getY() + 2 > level.getMaxY()) {
            source.sendFailure(Component.translatable("commands.thaumaturge.build.infusion_altar.height"));
            return 0;
        }
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-3, 0, -3), center.offset(3, 2, 3))) {
            if (!level.hasChunkAt(pos)) {
                source.sendFailure(Component.translatable("commands.thaumaturge.build.infusion_altar.unloaded"));
                return 0;
            }
        }
        level.setBlockAndUpdate(center, TTBlocks.PEDESTAL_ARCANE.get().defaultBlockState());
        level.setBlockAndUpdate(center.above(2), TTBlocks.INFUSION_MATRIX.get().defaultBlockState());
        placePillar(level, center.offset(-1, 0, -1), Direction.NORTH);
        placePillar(level, center.offset(-1, 0, 1), Direction.WEST);
        placePillar(level, center.offset(1, 0, -1), Direction.EAST);
        placePillar(level, center.offset(1, 0, 1), Direction.SOUTH);
        for (int dx = -3; dx <= 3; dx += 3) {
            for (int dz = -3; dz <= 3; dz += 3) {
                if (dx != 0 || dz != 0) {
                    level.setBlockAndUpdate(center.offset(dx, 0, dz), TTBlocks.PEDESTAL_ARCANE.get().defaultBlockState());
                }
            }
        }
        source.sendSuccess(() -> Component.translatable("commands.thaumaturge.build.infusion_altar.success", center.getX(), center.getY(), center.getZ()), true);
        return Command.SINGLE_SUCCESS;
    }

    private static void placePillar(ServerLevel level, BlockPos pos, Direction facing) {
        level.setBlockAndUpdate(pos, TTBlocks.PILLAR_ARCANE.get().defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, facing));
        level.removeBlock(pos.above(), false);
    }
}
