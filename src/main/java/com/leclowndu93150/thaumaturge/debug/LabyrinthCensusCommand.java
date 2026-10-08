package com.leclowndu93150.thaumaturge.debug;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.server.command.TTCommandRoot;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class LabyrinthCensusCommand {
    private static final int TOP = 14;

    private LabyrinthCensusCommand() {}

    @SubscribeEvent
    public static void onRegister(RegisterCommandsEvent event) {
        event.getDispatcher()
                .register(TTCommandRoot.root()
                        .then(Commands.literal("labyrinth_census")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("from", BlockPosArgument.blockPos())
                                        .then(Commands.argument("to", BlockPosArgument.blockPos())
                                                .executes(LabyrinthCensusCommand::census)))));
    }

    private static int census(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerLevel level = ctx.getSource().getLevel();
        BlockPos from = BlockPosArgument.getLoadedBlockPos(ctx, "from");
        BlockPos to = BlockPosArgument.getLoadedBlockPos(ctx, "to");
        Map<String, Integer> blocks = new TreeMap<>();
        for (BlockPos pos : BlockPos.betweenClosed(from, to)) {
            blocks.merge(
                    BuiltInRegistries.BLOCK
                            .getKey(level.getBlockState(pos).getBlock())
                            .getPath(),
                    1,
                    Integer::sum);
        }
        Map<String, Integer> entities = new TreeMap<>();
        for (int cx = Math.min(from.getX(), to.getX()) >> 4; cx <= Math.max(from.getX(), to.getX()) >> 4; cx++) {
            for (int cz = Math.min(from.getZ(), to.getZ()) >> 4; cz <= Math.max(from.getZ(), to.getZ()) >> 4; cz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) {
                    continue;
                }
                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    entities.merge(
                            BuiltInRegistries.BLOCK_ENTITY_TYPE
                                    .getKey(blockEntity.getType())
                                    .getPath(),
                            1,
                            Integer::sum);
                }
            }
        }
        String blockReport = blocks.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(TOP)
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(", "));
        String report = "blocks: " + blockReport + " | block entities: " + entities;
        Thaumaturge.LOGGER.info("Labyrinth census {} to {}: {}", from, to, report);
        ctx.getSource().sendSuccess(() -> Component.literal(report), false);
        return blocks.size();
    }
}
