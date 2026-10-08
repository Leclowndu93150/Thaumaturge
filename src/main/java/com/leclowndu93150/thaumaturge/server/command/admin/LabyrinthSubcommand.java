package com.leclowndu93150.thaumaturge.server.command.admin;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthEncounter;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthLandmarks;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthPhase;
import com.leclowndu93150.thaumaturge.api.labyrinth.MazeId;
import com.leclowndu93150.thaumaturge.content.eldritch.EldritchPlacement;
import com.leclowndu93150.thaumaturge.content.eldritch.encounter.BoundEntity;
import com.leclowndu93150.thaumaturge.content.eldritch.encounter.EncounterPhases;
import com.leclowndu93150.thaumaturge.content.eldritch.encounter.EncounterState;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthData;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthService;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeGeometry;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazePlan;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeRecord;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.LabyrinthDefinition;
import com.leclowndu93150.thaumaturge.content.eldritch.portal.ArrivalPad;
import com.leclowndu93150.thaumaturge.content.eldritch.portal.PortalLink;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;

final class LabyrinthSubcommand implements AdminSubcommand {
    private static final DynamicCommandExceptionType UNKNOWN =
            new DynamicCommandExceptionType(id -> Component.translatable("commands.thaumaturge.labyrinth.unknown", id));
    private static final SimpleCommandExceptionType FAILED =
            new SimpleCommandExceptionType(Component.translatable("commands.thaumaturge.labyrinth.create.failed"));
    private static final DynamicCommandExceptionType UNKNOWN_PHASE = new DynamicCommandExceptionType(
            name -> Component.translatable("commands.thaumaturge.labyrinth.phase.unknown", name));
    private static final DynamicCommandExceptionType UNKNOWN_ENCOUNTER = new DynamicCommandExceptionType(
            id -> Component.translatable("commands.thaumaturge.labyrinth.encounter.unknown", id));
    private static final SimpleCommandExceptionType NOT_INSIDE =
            new SimpleCommandExceptionType(Component.translatable("commands.thaumaturge.labyrinth.not_inside"));

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build(CommandBuildContext context) {
        return Commands.literal("labyrinth")
                .then(Commands.literal("create")
                        .executes(ctx -> create(ctx, Optional.empty()))
                        .then(Commands.argument("definition", ResourceLocationArgument.id())
                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggestResource(
                                        ctx.getSource()
                                                .getServer()
                                                .registryAccess()
                                                .lookupOrThrow(LabyrinthDefinition.REGISTRY_KEY)
                                                .listElementIds()
                                                .map(ResourceKey::location),
                                        builder))
                                .executes(ctx ->
                                        create(ctx, Optional.of(ResourceLocationArgument.getId(ctx, "definition"))))))
                .then(Commands.literal("list").executes(LabyrinthSubcommand::list))
                .then(Commands.literal("info")
                        .executes(LabyrinthSubcommand::infoHere)
                        .then(Commands.argument("id", IntegerArgumentType.integer(0))
                                .executes(ctx -> info(ctx.getSource(), record(ctx)))))
                .then(Commands.literal("tp")
                        .then(Commands.argument("id", IntegerArgumentType.integer(0))
                                .executes(LabyrinthSubcommand::teleport)))
                .then(Commands.literal("retire")
                        .then(Commands.argument("id", IntegerArgumentType.integer(0))
                                .executes(LabyrinthSubcommand::retire)))
                .then(Commands.literal("restamp")
                        .then(Commands.argument("id", IntegerArgumentType.integer(0))
                                .executes(LabyrinthSubcommand::restamp)))
                .then(Commands.literal("portal")
                        .then(Commands.argument("id", IntegerArgumentType.integer(0))
                                .executes(LabyrinthSubcommand::portal)))
                .then(Commands.literal("phase")
                        .then(Commands.argument("id", IntegerArgumentType.integer(0))
                                .then(Commands.argument("phase", StringArgumentType.word())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                                Arrays.stream(LabyrinthPhase.values())
                                                        .filter(phase -> phase != LabyrinthPhase.RETIRED)
                                                        .map(LabyrinthPhase::getSerializedName),
                                                builder))
                                        .executes(LabyrinthSubcommand::phase))))
                .then(Commands.literal("encounter")
                        .then(Commands.argument("id", IntegerArgumentType.integer(0))
                                .then(Commands.literal("clear").executes(ctx -> encounter(ctx, Optional.empty())))
                                .then(Commands.argument("encounter", ResourceLocationArgument.id())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggestResource(
                                                ctx.getSource()
                                                        .getServer()
                                                        .registryAccess()
                                                        .lookupOrThrow(LabyrinthEncounter.REGISTRY_KEY)
                                                        .listElementIds()
                                                        .map(ResourceKey::location),
                                                builder))
                                        .executes(ctx -> encounter(
                                                ctx, Optional.of(ResourceLocationArgument.getId(ctx, "encounter")))))));
    }

    private static int create(CommandContext<CommandSourceStack> ctx, Optional<ResourceLocation> definition)
            throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        GlobalPos origin = GlobalPos.of(source.getLevel().dimension(), BlockPos.containing(source.getPosition()));
        MazeRecord record =
                LabyrinthService.open(source.getServer(), origin, definition).orElseThrow(FAILED::create);
        MazePlan plan = record.plan();
        source.sendSuccess(
                () -> Component.translatable(
                        "commands.thaumaturge.labyrinth.create.success",
                        plan.id().value(),
                        plan.geometry().width(),
                        plan.geometry().depth(),
                        plan.encounter().map(key -> key.location().toString()).orElse("-")),
                true);
        return plan.id().value();
    }

    private static int list(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        Optional<ServerLevel> outer = LabyrinthService.outer(source.getServer());
        if (outer.isEmpty()) {
            return 0;
        }
        LabyrinthData data = LabyrinthData.get(outer.get());
        source.sendSuccess(
                () -> Component.translatable("commands.thaumaturge.labyrinth.list.header", data.size()), false);
        for (MazeRecord record : data.records()) {
            MazePlan plan = record.plan();
            source.sendSuccess(
                    () -> Component.translatable(
                            "commands.thaumaturge.labyrinth.list.entry",
                            plan.id().value(),
                            plan.geometry().width(),
                            plan.geometry().depth(),
                            record.state().phase().getSerializedName(),
                            plan.origin().dimension().location().toString(),
                            plan.origin().pos().toShortString()),
                    false);
        }
        return data.size();
    }

    private static int infoHere(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        MazeRecord record = LabyrinthService.find(source.getLevel(), BlockPos.containing(source.getPosition()))
                .orElseThrow(NOT_INSIDE::create);
        return info(source, record);
    }

    private static int info(CommandSourceStack source, MazeRecord record) {
        MazePlan plan = record.plan();
        MazeGeometry geometry = plan.geometry();
        source.sendSuccess(
                () -> Component.translatable(
                        "commands.thaumaturge.labyrinth.info",
                        plan.id().value(),
                        plan.definition().location().toString(),
                        geometry.width(),
                        geometry.depth(),
                        record.state().phase().getSerializedName(),
                        plan.encounter().map(key -> key.location().toString()).orElse("-"),
                        plan.landmarks()
                                .point(LabyrinthLandmarks.ARRIVAL)
                                .map(BlockPos::toShortString)
                                .orElse("-"),
                        plan.landmarks()
                                .point(LabyrinthLandmarks.KEY)
                                .map(BlockPos::toShortString)
                                .orElse("-"),
                        plan.landmarks()
                                .point(LabyrinthLandmarks.BOSS_DOOR)
                                .map(BlockPos::toShortString)
                                .orElse("-"),
                        plan.landmarks()
                                .point(LabyrinthLandmarks.BOSS_CENTER)
                                .map(BlockPos::toShortString)
                                .orElse("-"),
                        record.state().triggers().size()),
                false);
        BoundingBox hall = plan.landmarks().bossHall();
        EncounterState encounter = record.state().encounter();
        long defeated = encounter.bound().stream().filter(BoundEntity::defeated).count();
        source.sendSuccess(
                () -> Component.translatable(
                        "commands.thaumaturge.labyrinth.info.encounter",
                        encounter.active().map(key -> key.location().toString()).orElse("-"),
                        encounter
                                .override()
                                .map(key -> key.location().toString())
                                .orElse("-"),
                        encounter.bound().size(),
                        defeated,
                        encounter.scaledFor(),
                        new BlockPos(hall.minX(), hall.minY(), hall.minZ()).toShortString(),
                        new BlockPos(hall.maxX(), hall.maxY(), hall.maxZ()).toShortString()),
                false);
        return plan.id().value();
    }

    private static MazeRecord record(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        int id = IntegerArgumentType.getInteger(ctx, "id");
        return LabyrinthService.byId(ctx.getSource().getServer(), new MazeId(id))
                .orElseThrow(() -> UNKNOWN.create(id));
    }

    private static ServerLevel outer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return LabyrinthService.outer(ctx.getSource().getServer()).orElseThrow(FAILED::create);
    }

    private static int teleport(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        MazeRecord record = record(ctx);
        ServerLevel outer = outer(ctx);
        ArrivalPad.Arrival arrival = ArrivalPad.prepare(outer, record);
        player.changeDimension(new DimensionTransition(
                outer, arrival.pos(), Vec3.ZERO, arrival.yaw(), 0.0F, DimensionTransition.DO_NOTHING));
        return record.plan().id().value();
    }

    private static int phase(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        MazeRecord record = record(ctx);
        ServerLevel outer = outer(ctx);
        String name = StringArgumentType.getString(ctx, "phase");
        LabyrinthPhase phase = Arrays.stream(LabyrinthPhase.values())
                .filter(value -> value.getSerializedName().equals(name))
                .findFirst()
                .orElseThrow(() -> UNKNOWN_PHASE.create(name));
        switch (phase) {
            case SEALED -> EncounterPhases.reset(outer, record);
            case CHARGING -> EncounterPhases.charge(outer, record);
            case ACTIVE -> EncounterPhases.activate(outer, record);
            case CONQUERED -> EncounterPhases.conquer(outer, record);
            case RETIRED -> throw UNKNOWN_PHASE.create(name);
        }
        ctx.getSource()
                .sendSuccess(
                        () -> Component.translatable(
                                "commands.thaumaturge.labyrinth.phase.success",
                                record.plan().id().value(),
                                record.state().phase().getSerializedName()),
                        true);
        return Command.SINGLE_SUCCESS;
    }

    private static int encounter(CommandContext<CommandSourceStack> ctx, Optional<ResourceLocation> encounter)
            throws CommandSyntaxException {
        MazeRecord record = record(ctx);
        ServerLevel outer = outer(ctx);
        Optional<ResourceKey<LabyrinthEncounter>> key =
                encounter.map(id -> ResourceKey.create(LabyrinthEncounter.REGISTRY_KEY, id));
        if (key.isPresent()
                && outer.registryAccess()
                        .lookupOrThrow(LabyrinthEncounter.REGISTRY_KEY)
                        .get(key.get())
                        .isEmpty()) {
            throw UNKNOWN_ENCOUNTER.create(key.get().location());
        }
        record.state().encounter().setOverride(key);
        LabyrinthData.get(outer).setDirty();
        ctx.getSource()
                .sendSuccess(
                        () -> Component.translatable(
                                "commands.thaumaturge.labyrinth.encounter.success",
                                record.plan().id().value(),
                                key.map(value -> value.location().toString()).orElse("-")),
                        true);
        return Command.SINGLE_SUCCESS;
    }

    private static int retire(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        MazeRecord record = record(ctx);
        ServerLevel outer = outer(ctx);
        LabyrinthService.retire(outer, record.plan().id());
        ctx.getSource()
                .sendSuccess(
                        () -> Component.translatable(
                                "commands.thaumaturge.labyrinth.retire.success",
                                record.plan().id().value()),
                        true);
        return Command.SINGLE_SUCCESS;
    }

    private static int portal(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        MazeRecord record = record(ctx);
        ServerLevel level = ctx.getSource().getLevel();
        BlockPos pos = BlockPos.containing(ctx.getSource().getPosition());
        EldritchPlacement.place(
                        level,
                        pos,
                        TTBlocks.ELDRITCH_PORTAL.get().defaultBlockState(),
                        TTBlockEntities.ELDRITCH_PORTAL.get())
                .ifPresent(portal ->
                        portal.setLink(PortalLink.intoLabyrinth(record.plan().id())));
        ctx.getSource()
                .sendSuccess(
                        () -> Component.translatable(
                                "commands.thaumaturge.labyrinth.portal.success",
                                record.plan().id().value(),
                                pos.toShortString()),
                        true);
        return Command.SINGLE_SUCCESS;
    }

    private static int restamp(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        MazeRecord record = record(ctx);
        ServerLevel outer = outer(ctx);
        int queued = LabyrinthService.requeue(
                outer, record.plan(), record.plan().geometry().bounds());
        ctx.getSource()
                .sendSuccess(
                        () -> Component.translatable(
                                "commands.thaumaturge.labyrinth.restamp.success",
                                queued,
                                record.plan().id().value()),
                        true);
        return queued;
    }
}
