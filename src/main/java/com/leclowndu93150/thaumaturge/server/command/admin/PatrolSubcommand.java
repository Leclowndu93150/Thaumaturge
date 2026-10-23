package com.leclowndu93150.thaumaturge.server.command.admin;

import com.leclowndu93150.thaumaturge.content.entity.ranger.CrimsonPatrols;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;

final class PatrolSubcommand implements AdminSubcommand {
    private static final String POS = "pos";
    private static final SimpleCommandExceptionType NO_ROOM =
            new SimpleCommandExceptionType(Component.translatable("commands.thaumaturge.patrol.failed"));

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build(CommandBuildContext context) {
        return Commands.literal("patrol")
                .executes(ctx -> spawn(ctx, BlockPos.containing(ctx.getSource().getPosition())))
                .then(Commands.argument(POS, BlockPosArgument.blockPos())
                        .executes(ctx -> spawn(ctx, BlockPosArgument.getSpawnablePos(ctx, POS))));
    }

    private static int spawn(CommandContext<CommandSourceStack> ctx, BlockPos pos) throws CommandSyntaxException {
        ServerLevel level = ctx.getSource().getLevel();
        if (!CrimsonPatrols.spawnPatrol(level, pos, level.getRandom(), MobSpawnType.COMMAND)) {
            throw NO_ROOM.create();
        }
        ctx.getSource()
                .sendSuccess(
                        () -> Component.translatable(
                                "commands.thaumaturge.patrol.spawned", pos.getX(), pos.getY(), pos.getZ()),
                        true);
        return 1;
    }
}
