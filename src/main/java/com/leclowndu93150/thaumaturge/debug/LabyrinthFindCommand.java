package com.leclowndu93150.thaumaturge.debug;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.labyrinth.MazeId;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthService;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazePlan;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeRecord;
import com.leclowndu93150.thaumaturge.server.command.TTCommandRoot;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class LabyrinthFindCommand {
    private static final int LIMIT = 6;

    private LabyrinthFindCommand() {}

    @SubscribeEvent
    public static void onRegister(RegisterCommandsEvent event) {
        event.getDispatcher()
                .register(TTCommandRoot.root()
                        .then(Commands.literal("labyrinth_find")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("id", IntegerArgumentType.integer(0))
                                        .then(Commands.argument("room", StringArgumentType.greedyString())
                                                .executes(LabyrinthFindCommand::find)))));
    }

    private static int find(CommandContext<CommandSourceStack> ctx) {
        Optional<MazeRecord> record = LabyrinthService.byId(
                ctx.getSource().getServer(), new MazeId(IntegerArgumentType.getInteger(ctx, "id")));
        if (record.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("No such labyrinth"));
            return 0;
        }
        String wanted = StringArgumentType.getString(ctx, "room");
        MazePlan plan = record.get().plan();
        List<String> hits = new ArrayList<>();
        for (int index = 0; index < plan.rooms().length && hits.size() < LIMIT; index++) {
            MazePlan.PlacedRoom room = plan.placedRoom(index);
            if (room.type().location().getPath().contains(wanted)) {
                BlockPos min = plan.geometry().cellMin(room.anchorX(), room.anchorZ());
                hits.add(room.type().location().getPath() + "@" + min.getX() + "," + min.getZ());
            }
        }
        ctx.getSource().sendSuccess(() -> Component.literal(String.join(" ", hits)), false);
        return hits.size();
    }
}
