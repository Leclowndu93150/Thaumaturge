package com.leclowndu93150.thaumaturge.debug;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthRuntime;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthService;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthTuning;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeCells;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.LabyrinthDefinition;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.layout.LayoutContext;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.layout.LayoutPlanner;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.layout.LayoutResult;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.layout.RoomPlacement;
import com.leclowndu93150.thaumaturge.server.command.TTCommandRoot;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class LabyrinthPreviewCommand {
    private LabyrinthPreviewCommand() {}

    @SubscribeEvent
    public static void onRegister(RegisterCommandsEvent event) {
        event.getDispatcher()
                .register(TTCommandRoot.root()
                        .then(Commands.literal("labyrinth_preview")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("seed", IntegerArgumentType.integer())
                                        .executes(ctx -> preview(ctx, 0))
                                        .then(Commands.argument(
                                                        "size",
                                                        IntegerArgumentType.integer(
                                                                LayoutPlanner.MIN_SIZE, LayoutPlanner.MAX_SIZE))
                                                .executes(ctx ->
                                                        preview(ctx, IntegerArgumentType.getInteger(ctx, "size")))))));
    }

    private static int preview(CommandContext<CommandSourceStack> ctx, int size) {
        CommandSourceStack source = ctx.getSource();
        Optional<ServerLevel> outer = LabyrinthService.outer(source.getServer());
        Optional<LabyrinthRuntime> runtime = outer.flatMap(LabyrinthService::runtime);
        Optional<Holder.Reference<LabyrinthDefinition>> definition =
                LabyrinthService.definition(source.getServer(), Optional.empty());
        if (outer.isEmpty() || runtime.isEmpty() || definition.isEmpty()) {
            source.sendFailure(Component.literal("No Outer Lands or labyrinth definition"));
            return 0;
        }
        int seed = IntegerArgumentType.getInteger(ctx, "seed");
        LabyrinthDefinition base = definition.get().value();
        LabyrinthDefinition used = size == 0
                ? base
                : new LabyrinthDefinition(
                        ConstantInt.of(size),
                        base.layout(),
                        base.rooms(),
                        base.encounters(),
                        base.palette(),
                        base.decoration(),
                        base.guardians(),
                        base.gameplay());
        RandomSource random = outer.get()
                .getChunkSource()
                .randomState()
                .getOrCreateRandomFactory(TTIds.LABYRINTH_LAYOUT_RANDOM)
                .at(seed, 0, -seed);
        long start = System.nanoTime();
        LayoutResult result = LayoutPlanner.plan(new LayoutContext(
                used, LabyrinthTuning.DEFAULT, LabyrinthService.roomFilter(source.getServer(), runtime.get()), random));
        long micros = (System.nanoTime() - start) / 1000;
        String report = render(result) + "\n" + stats(result) + "\nplanned in " + micros + " us";
        Thaumaturge.LOGGER.info("Labyrinth preview seed {}:\n{}", seed, report);
        source.sendSuccess(() -> Component.literal(stats(result) + ", " + micros + " us (map in server log)"), false);
        return result.width();
    }

    private static String stats(LayoutResult result) {
        int open = 0;
        int edges = 0;
        int deadEnds = 0;
        for (int cell = 0; cell < result.edges().length; cell++) {
            int degree = Integer.bitCount(result.edges()[cell]);
            if (result.roomOfCell()[cell] >= 0) {
                open++;
            }
            edges += degree;
            if (degree == 1) {
                deadEnds++;
            }
        }
        edges /= 2;
        int reachable = reachable(result);
        Map<String, Integer> counts = new TreeMap<>();
        for (RoomPlacement placement : result.rooms()) {
            counts.merge(
                    placement
                            .room()
                            .unwrapKey()
                            .map(key -> key.location().getPath())
                            .orElse("?"),
                    1,
                    Integer::sum);
        }
        return result.width() + "x" + result.depth() + " open=" + open + " reachable=" + reachable + " deadEnds="
                + deadEnds + " loops=" + (edges - open + 1) + " keyDegree="
                + Integer.bitCount(result.edges()[result.keyCell()]) + " minimal=" + result.minimal() + " encounter="
                + result.encounter()
                        .flatMap(Holder::unwrapKey)
                        .map(key -> key.location().toString())
                        .orElse("-") + " rooms=" + counts;
    }

    private static int reachable(LayoutResult result) {
        boolean[] seen = new boolean[result.edges().length];
        IntArrayFIFOQueue queue = new IntArrayFIFOQueue();
        seen[result.portalCell()] = true;
        queue.enqueue(result.portalCell());
        int count = 0;
        while (!queue.isEmpty()) {
            int cell = queue.dequeueInt();
            count++;
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                if (!MazeCells.open(result.edges()[cell], direction)) {
                    continue;
                }
                int x = cell % result.width() + direction.getStepX();
                int z = cell / result.width() + direction.getStepZ();
                int next = x + z * result.width();
                if (x >= 0 && z >= 0 && x < result.width() && z < result.depth() && !seen[next]) {
                    seen[next] = true;
                    queue.enqueue(next);
                }
            }
        }
        return count;
    }

    private static String render(LayoutResult result) {
        StringBuilder out = new StringBuilder();
        for (int z = 0; z < result.depth(); z++) {
            StringBuilder top = new StringBuilder();
            StringBuilder mid = new StringBuilder();
            for (int x = 0; x < result.width(); x++) {
                int cell = x + z * result.width();
                int edges = result.edges()[cell];
                top.append('+').append(MazeCells.open(edges, Direction.NORTH) ? "   " : "---");
                mid.append(MazeCells.open(edges, Direction.WEST) ? ' ' : '|')
                        .append(' ')
                        .append(symbol(result, cell))
                        .append(' ');
            }
            out.append(top).append("+\n").append(mid).append("|\n");
        }
        out.append("+---".repeat(result.width())).append('+');
        return out.toString();
    }

    private static char symbol(LayoutResult result, int cell) {
        if (cell == result.portalCell()) {
            return 'P';
        }
        if (cell == result.keyCell()) {
            return 'K';
        }
        int room = result.roomOfCell()[cell];
        if (room < 0) {
            return '#';
        }
        if (room == result.roomOfCell()[result.hallCell()]) {
            return 'B';
        }
        int degree = Integer.bitCount(result.edges()[cell]);
        return degree == 1 ? 'd' : degree >= 3 ? 'J' : '.';
    }
}
