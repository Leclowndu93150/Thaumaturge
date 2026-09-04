package com.leclowndu93150.thaumaturge.server.command;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.casters.FocusElement;
import com.leclowndu93150.thaumaturge.api.casters.FocusEngine;
import com.leclowndu93150.thaumaturge.api.casters.FocusPackage;
import com.leclowndu93150.thaumaturge.api.casters.FocusSettings;
import com.leclowndu93150.thaumaturge.api.casters.ICaster;
import com.leclowndu93150.thaumaturge.api.nodes.NodeModifier;
import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.api.taint.TaintApi;
import com.leclowndu93150.thaumaturge.api.warp.IPlayerWarp;
import com.leclowndu93150.thaumaturge.api.warp.WarpHelper;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeGenerator;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeLocationIndex;
import com.leclowndu93150.thaumaturge.content.casters.ItemFocus;
import com.leclowndu93150.thaumaturge.content.effect.StreamPathfinder;
import com.leclowndu93150.thaumaturge.content.eldritch.maze.MazeSavedData;
import com.leclowndu93150.thaumaturge.content.entity.EntityFluxRift;
import com.leclowndu93150.thaumaturge.content.entity.ThaumicSlime;
import com.leclowndu93150.thaumaturge.content.entity.champion.ChampionHelper;
import com.leclowndu93150.thaumaturge.content.entity.champion.ChampionModifier;
import com.leclowndu93150.thaumaturge.content.research.PlayerKnowledge;
import com.leclowndu93150.thaumaturge.content.research.ResearchGrants;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import com.leclowndu93150.thaumaturge.content.research.link.ResearchLinkData;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import com.leclowndu93150.thaumaturge.content.warp.WarpEvents;
import com.leclowndu93150.thaumaturge.data.worldgen.feature.TCConfiguredFeatures;
import com.leclowndu93150.thaumaturge.registry.TCAttachments;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import com.leclowndu93150.thaumaturge.registry.TCFocusElements;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.leclowndu93150.thaumaturge.registry.TCMobEffects;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TCIds.MODID)
public final class TCCommands {
    private TCCommands() {}

    private static final SuggestionProvider<CommandSourceStack> PARTICLE_NAMES =
            (ctx, builder) -> SharedSuggestionProvider.suggest(ParticleDemos.DEMOS.keySet(), builder);
    private static final int DEFAULT_RIFT_SIZE = 20;
    private static final int COMMAND_MAX_RIFT_SIZE = 500;
    private static final double RIFT_SPAWN_DISTANCE = 6.0;

    private static final SuggestionProvider<CommandSourceStack> WARP_TYPES =
            (ctx, builder) -> SharedSuggestionProvider.suggest(
                    Arrays.stream(WarpType.values()).map(t -> t.name().toLowerCase(Locale.ROOT)), builder);

    private static final SuggestionProvider<CommandSourceStack> FOCUS_ELEMENTS =
            (ctx, builder) -> SharedSuggestionProvider.suggest(
                    TCFocusElements.registry().keySet().stream().map(ResourceLocation::toString), builder);

    private static final SuggestionProvider<CommandSourceStack> CHAMPION_MODS =
            (ctx, builder) -> SharedSuggestionProvider.suggest(
                    Stream.concat(ChampionModifier.MODS.stream().map(ChampionModifier::name), Stream.of("random")),
                    builder);

    private static final double CHAMPION_SPAWN_DISTANCE = 4.0;

    private static final DynamicCommandExceptionType ERROR_INVALID_GATE =
            new DynamicCommandExceptionType((value) -> Component.literal("Unknown Research Entry : " + value));
    private static final DynamicCommandExceptionType ERROR_INVALID_ASPECT =
            new DynamicCommandExceptionType((value) -> Component.literal("Unknown Aspect : " + value));
    private static final DynamicCommandExceptionType ERROR_INVALID_NODE_TYPE =
            new DynamicCommandExceptionType((value) -> Component.literal("Unknown node type: " + value));

    @SubscribeEvent
    public static void onRegister(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> tc = Commands.literal("tc")
                .then(Commands.literal("table").executes(TCCommands::giveResearchTable))
                .then(Commands.literal("outermaze")
                        .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(ctx -> generateOuterMaze(ctx, 0, 0))
                        .then(Commands.argument("width", IntegerArgumentType.integer(5, 31))
                                .then(Commands.argument("height", IntegerArgumentType.integer(5, 31))
                                        .executes(ctx -> generateOuterMaze(
                                                ctx,
                                                IntegerArgumentType.getInteger(ctx, "width"),
                                                IntegerArgumentType.getInteger(ctx, "height"))))))
                .then(Commands.literal("book").executes(TCCommands::giveThaumonomicon))
                .then(Commands.literal("build")
                        .requires(source -> source.getEntity() instanceof ServerPlayer player && player.isCreative())
                        .then(Commands.literal("infusion_altar").executes(TCCommands::buildInfusionAltar)))
                .then(Commands.literal("particle")
                        .then(Commands.literal("list").executes(TCCommands::listParticles))
                        .then(Commands.argument("name", StringArgumentType.word())
                                .suggests(PARTICLE_NAMES)
                                .executes(TCCommands::runParticle)))
                .then(Commands.literal("flux_goo")
                        .then(Commands.literal("set")
                                .then(Commands.argument("level", IntegerArgumentType.integer(1, 8))
                                        .executes(TCCommands::setFluxGoo))))
                .then(Commands.literal("effect")
                        .then(Commands.literal("vis_exhaust").executes(ctx -> giveEffect(ctx, "vis_exhaust")))
                        .then(Commands.literal("infectious_vis_exhaust")
                                .executes(ctx -> giveEffect(ctx, "infectious_vis_exhaust")))
                        .then(Commands.literal("flux_taint").executes(ctx -> giveEffect(ctx, "flux_taint"))))
                .then(Commands.literal("entity")
                        .then(Commands.literal("thaumic_slime").executes(ctx -> spawnEntity(ctx, "thaumic_slime")))
                        .then(Commands.literal("taint_crawler").executes(ctx -> spawnEntity(ctx, "taint_crawler")))
                        .then(Commands.literal("taint_seed").executes(ctx -> spawnEntity(ctx, "taint_seed")))
                        .then(Commands.literal("taint_seed_prime")
                                .executes(ctx -> spawnEntity(ctx, "taint_seed_prime")))
                        .then(Commands.literal("taint_swarm").executes(ctx -> spawnEntity(ctx, "taint_swarm")))
                        .then(Commands.literal("taintacle").executes(ctx -> spawnEntity(ctx, "taintacle")))
                        .then(Commands.literal("taintacle_small").executes(ctx -> spawnEntity(ctx, "taintacle_small"))))
                .then(Commands.literal("champion")
                        .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("modifier", StringArgumentType.word())
                                .suggests(CHAMPION_MODS)
                                .executes(ctx -> spawnChampion(ctx, null))
                                .then(Commands.argument(
                                                "entity",
                                                ResourceArgument.resource(
                                                        event.getBuildContext(), Registries.ENTITY_TYPE))
                                        .executes(ctx -> spawnChampion(
                                                ctx,
                                                ResourceArgument.getResource(ctx, "entity", Registries.ENTITY_TYPE))))))
                .then(Commands.literal("streampath")
                        .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("from", Vec3Argument.vec3())
                                .then(Commands.argument("to", Vec3Argument.vec3())
                                        .executes(TCCommands::traceStreamPath))))
                .then(Commands.literal("rift")
                        .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(ctx -> spawnRift(ctx, DEFAULT_RIFT_SIZE))
                        .then(Commands.argument("size", IntegerArgumentType.integer(1, COMMAND_MAX_RIFT_SIZE))
                                .executes(ctx -> spawnRift(ctx, IntegerArgumentType.getInteger(ctx, "size")))))
                .then(Commands.literal("crystal")
                        .then(Commands.argument("aspect", StringArgumentType.word())
                                .executes(TCCommands::giveCrystal)))
                .then(Commands.literal("node")
                        .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(ctx -> spawnNode(ctx, "random", "random", ""))
                        .then(Commands.argument("type", StringArgumentType.word())
                                .suggests(NODE_TYPES)
                                .executes(ctx -> spawnNode(ctx, StringArgumentType.getString(ctx, "type"), "none", ""))
                                .then(Commands.argument("modifier", StringArgumentType.word())
                                        .suggests(NODE_MODIFIERS)
                                        .executes(ctx -> spawnNode(
                                                ctx,
                                                StringArgumentType.getString(ctx, "type"),
                                                StringArgumentType.getString(ctx, "modifier"),
                                                ""))
                                        .then(Commands.argument("aspects", StringArgumentType.greedyString())
                                                .executes(ctx -> spawnNode(
                                                        ctx,
                                                        StringArgumentType.getString(ctx, "type"),
                                                        StringArgumentType.getString(ctx, "modifier"),
                                                        StringArgumentType.getString(ctx, "aspects")))))))
                .then(Commands.literal("link")
                        .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.literal("unlink").executes(TCCommands::shareUnlink)))
                .then(Commands.literal("focus")
                        .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("tier", IntegerArgumentType.integer(1, 3))
                                .then(Commands.argument("elements", StringArgumentType.greedyString())
                                        .suggests(FOCUS_ELEMENTS)
                                        .executes(TCCommands::giveFocus))))
                .then(Commands.literal("warp")
                        .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.literal("info").executes(TCCommands::warpInfo))
                        .then(Commands.literal("add")
                                .then(Commands.argument("type", StringArgumentType.word())
                                        .suggests(WARP_TYPES)
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 500))
                                                .executes(ctx -> warpModify(ctx, false)))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("type", StringArgumentType.word())
                                        .suggests(WARP_TYPES)
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 500))
                                                .executes(ctx -> warpModify(ctx, true)))))
                        .then(Commands.literal("clear").executes(TCCommands::warpClear))
                        .then(Commands.literal("event").executes(TCCommands::warpEvent)))
                .then(Commands.literal("aura")
                        .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.literal("info").executes(TCCommands::auraInfo))
                        .then(Commands.literal("vis")
                                .then(Commands.literal("add")
                                        .then(Commands.argument("amount", FloatArgumentType.floatArg(0.0F))
                                                .executes(ctx -> auraVis(ctx, false))))
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("amount", FloatArgumentType.floatArg(0.0F))
                                                .executes(ctx -> auraVis(ctx, true)))))
                        .then(Commands.literal("flux")
                                .then(Commands.literal("add")
                                        .then(Commands.argument("amount", FloatArgumentType.floatArg(0.0F))
                                                .executes(ctx -> auraFlux(ctx, false))))
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("amount", FloatArgumentType.floatArg(0.0F))
                                                .executes(ctx -> auraFlux(ctx, true))))))
                .then(Commands.literal("taint")
                        .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.literal("seed").executes(ctx -> spawnEntity(ctx, "taint_seed")))
                        .then(Commands.literal("spread").executes(TCCommands::taintSpread)))
                .then(Commands.literal("tree")
                        .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.literal("greatwood")
                                .executes(ctx -> placeFeature(ctx, TCConfiguredFeatures.GREATWOOD_TREE)))
                        .then(Commands.literal("silverwood")
                                .executes(ctx -> placeFeature(ctx, TCConfiguredFeatures.SILVERWOOD_TREE)))
                        .then(Commands.literal("magic")
                                .executes(ctx -> placeFeature(ctx, TCConfiguredFeatures.BIG_MAGIC_TREE))))
                .then(Commands.literal("research")
                        .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.literal("grant")
                                .then(Commands.argument("entry", ResourceKeyArgument.key(IResearchEntry.REGISTRY_KEY))
                                        .executes(TCCommands::grantGate)))
                        .then(Commands.literal("revoke")
                                .then(Commands.argument("entry", ResourceKeyArgument.key(IResearchEntry.REGISTRY_KEY))
                                        .executes(TCCommands::revokeGate)))
                        .then(Commands.literal("reset").executes(TCCommands::resetResearch))
                        .then(Commands.literal("all").executes(TCCommands::grantAllResearch)))
                .then(Commands.literal("aspect")
                        .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.literal("all")
                                .executes(ctx -> grantAllAspects(ctx, AspectPools.SOFT_CAP))
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1, 10000))
                                        .executes(ctx ->
                                                grantAllAspects(ctx, IntegerArgumentType.getInteger(ctx, "amount")))))
                        .then(Commands.argument("aspect", ResourceKeyArgument.key(IAspect.REGISTRY_KEY))
                                .executes(ctx -> grantAspect(ctx, AspectPools.SOFT_CAP))
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1, 10000))
                                        .executes(ctx ->
                                                grantAspect(ctx, IntegerArgumentType.getInteger(ctx, "amount"))))))
                .then(Commands.literal("locate")
                        .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.literal("node")
                                .then(Commands.argument("type", StringArgumentType.word())
                                        .suggests(NODE_LOCATE_TYPES)
                                        .executes(TCCommands::locateNode))));
        event.getDispatcher().register(tc);
    }

    private static int locateNode(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ServerLevel level = player.serverLevel();
        String typeName = StringArgumentType.getString(ctx, "type");
        NodeType type = Arrays.stream(NodeType.values())
                .filter(candidate -> candidate.getSerializedName().equalsIgnoreCase(typeName))
                .findFirst()
                .orElseThrow(() -> ERROR_INVALID_NODE_TYPE.create(typeName));

        NodeLocationIndex index = NodeLocationIndex.get(level);
        BlockPos origin = player.blockPosition();
        Optional<BlockPos> result;
        while ((result = index.findNearest(origin, type)).isPresent()) {
            BlockPos candidate = result.get();
            if (!level.hasChunkAt(candidate)) {
                break;
            }
            if (level.getBlockEntity(candidate) instanceof BlockEntityNode node && node.getNodeType() == type) {
                break;
            }
            index.remove(candidate);
        }
        if (result.isEmpty()) {
            ctx.getSource()
                    .sendFailure(Component.literal("No known "
                            + type.getSerializedName()
                            + " node found; nodes in legacy chunks are indexed when those chunks load"));
            return 0;
        }

        BlockPos pos = result.get();
        String teleport = "/tp @s " + pos.getX() + " " + pos.getY() + " " + pos.getZ();
        Component coordinates = Component.literal("[" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]")
                .withStyle(style -> style.withColor(ChatFormatting.GREEN)
                        .withUnderlined(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, teleport))
                        .withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT, Component.literal("Click to copy " + teleport))));
        int distance = (int) Math.round(Math.sqrt(pos.distSqr(origin)));
        ctx.getSource()
                .sendSuccess(
                        () -> Component.literal("Nearest " + type.getSerializedName() + " node is at ")
                                .append(coordinates)
                                .append(Component.literal(" (" + distance + " blocks away)")),
                        false);
        return Command.SINGLE_SUCCESS;
    }

    private static int resetResearch(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            PlayerKnowledge knowledge = (PlayerKnowledge) KnowledgeAccess.of(player);
            int cleared = knowledge.researchList().size();
            knowledge.clear();
            ResearchManager.applyAutoUnlock(player);
            knowledge.sync(player);
            ctx.getSource()
                    .sendSuccess(
                            () -> Component.literal(
                                    String.format("Reset %d research entries and all knowledge", cleared)),
                            false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int grantAllResearch(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            int granted = ResearchGrants.grantAll(player);
            ctx.getSource()
                    .sendSuccess(
                            () -> Component.literal(String.format(
                                    "Granted %d research entries and all aspect research points", granted)),
                            false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int grantAllAspects(CommandContext<CommandSourceStack> ctx, int amount) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            int count = AspectPools.grantAllForCommand(player, amount);
            ctx.getSource()
                    .sendSuccess(
                            () -> Component.literal(
                                    String.format("Granted %d research points to all %d aspects", amount, count)),
                            false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int grantAspect(CommandContext<CommandSourceStack> ctx, int amount) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            ResourceKey<IAspect> key = getRegistryKey(ctx, "aspect", IAspect.REGISTRY_KEY, ERROR_INVALID_ASPECT);
            Holder<IAspect> aspect =
                    player.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).getOrThrow(key);
            AspectPools.grantForCommand(player, aspect, amount);
            ctx.getSource()
                    .sendSuccess(
                            () -> Component.literal(
                                    String.format("Granted %d research points of %s", amount, key.location())),
                            false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int revokeGate(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            ResourceKey<IResearchEntry> key =
                    getRegistryKey(ctx, "entry", IResearchEntry.REGISTRY_KEY, ERROR_INVALID_GATE);
            PlayerKnowledge knowledge = (PlayerKnowledge) KnowledgeAccess.of(player);
            if (knowledge.removeResearch(key.location())) {
                knowledge.sync(player);
                ctx.getSource()
                        .sendSuccess(
                                () -> Component.literal(String.format("Revoked research %s ", key.location())), false);
                return Command.SINGLE_SUCCESS;
            } else {
                ctx.getSource().sendFailure(Component.literal("Failed to revoke research entry"));
                return 0;
            }
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int grantGate(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            ServerLevel level = (ServerLevel) player.level();
            BlockPos pos = player.blockPosition();
            ResourceKey<IResearchEntry> key =
                    getRegistryKey(ctx, "entry", IResearchEntry.REGISTRY_KEY, ERROR_INVALID_GATE);
            Holder<IResearchEntry> holder = level.registryAccess()
                    .lookupOrThrow(IResearchEntry.REGISTRY_KEY)
                    .getOrThrow(key);
            if (ResearchManager.complete(player, key.location())) {
                ResearchManager.setStage(
                        player, key.location(), holder.value().stages().size());
                ctx.getSource()
                        .sendSuccess(
                                () -> Component.literal(String.format("Unlocked research %s ", key.location())), false);
                return Command.SINGLE_SUCCESS;
            } else {
                ctx.getSource().sendFailure(Component.literal("Failed to grant research entry"));
                return 0;
            }
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int warpInfo(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            IPlayerWarp warp = WarpHelper.getWarp(player);
            ctx.getSource()
                    .sendSuccess(
                            () -> Component.literal(String.format(
                                    "Warp: permanent %d, normal %d, temporary %d, counter %d",
                                    warp.get(WarpType.PERMANENT),
                                    warp.get(WarpType.NORMAL),
                                    warp.get(WarpType.TEMPORARY),
                                    warp.getCounter())),
                            false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int warpModify(CommandContext<CommandSourceStack> ctx, boolean remove) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            WarpType type =
                    WarpType.valueOf(StringArgumentType.getString(ctx, "type").toUpperCase(Locale.ROOT));
            int amount = IntegerArgumentType.getInteger(ctx, "amount");
            WarpHelper.addWarp(player, remove ? -amount : amount, type);
            ctx.getSource()
                    .sendSuccess(
                            () -> Component.literal((remove ? "Removed " : "Added ") + amount + " " + type + " warp"),
                            false);
            return Command.SINGLE_SUCCESS;
        } catch (IllegalArgumentException e) {
            ctx.getSource().sendFailure(Component.literal("Unknown warp type"));
            return 0;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int warpClear(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            WarpHelper.getWarp(player).clear();
            player.syncData(TCAttachments.WARP);
            ctx.getSource().sendSuccess(() -> Component.literal("Warp cleared"), false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int warpEvent(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            WarpEvents.checkWarpEvent(player);
            ctx.getSource().sendSuccess(() -> Component.literal("Warp event check rolled"), false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int auraInfo(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            ServerLevel level = (ServerLevel) player.level();
            BlockPos pos = player.blockPosition();
            float vis = AuraHelper.getVis(level, pos);
            float flux = AuraHelper.getFlux(level, pos);
            int base = AuraHelper.getAuraBase(level, pos);
            ctx.getSource()
                    .sendSuccess(
                            () -> Component.literal(String.format(
                                    "Aura at %s: vis %.1f, flux %.1f, base %d", pos.toShortString(), vis, flux, base)),
                            false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int auraVis(CommandContext<CommandSourceStack> ctx, boolean remove) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            ServerLevel level = (ServerLevel) player.level();
            BlockPos pos = player.blockPosition();
            float amount = FloatArgumentType.getFloat(ctx, "amount");
            if (remove) {
                float drained = AuraHelper.drainVis(level, pos, amount, false);
                ctx.getSource().sendSuccess(() -> Component.literal(String.format("Drained %.1f vis", drained)), false);
            } else {
                AuraHelper.addVis(level, pos, amount);
                ctx.getSource().sendSuccess(() -> Component.literal(String.format("Added %.1f vis", amount)), false);
            }
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int auraFlux(CommandContext<CommandSourceStack> ctx, boolean remove) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            ServerLevel level = (ServerLevel) player.level();
            BlockPos pos = player.blockPosition();
            float amount = FloatArgumentType.getFloat(ctx, "amount");
            if (remove) {
                float drained = AuraHelper.drainFlux(level, pos, amount, false);
                ctx.getSource()
                        .sendSuccess(() -> Component.literal(String.format("Drained %.1f flux", drained)), false);
            } else {
                AuraHelper.addFlux(level, pos, amount);
                ctx.getSource().sendSuccess(() -> Component.literal(String.format("Added %.1f flux", amount)), false);
            }
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int placeFeature(CommandContext<CommandSourceStack> ctx, ResourceKey<ConfiguredFeature<?, ?>> key) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            ServerLevel level = (ServerLevel) player.level();
            BlockPos pos = player.blockPosition();
            Holder<ConfiguredFeature<?, ?>> holder = level.registryAccess()
                    .lookupOrThrow(Registries.CONFIGURED_FEATURE)
                    .getOrThrow(key);
            boolean placed = holder.value().place(level, level.getChunkSource().getGenerator(), level.getRandom(), pos);
            if (placed) {
                ctx.getSource().sendSuccess(() -> Component.literal("Placed " + key.location()), false);
                return Command.SINGLE_SUCCESS;
            }
            ctx.getSource().sendFailure(Component.literal("Feature refused to place here (bad soil or no clearance)"));
            return 0;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int taintSpread(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            ServerLevel level = (ServerLevel) player.level();
            BlockPos pos = player.blockPosition();
            TaintApi.spreadFibres(level, pos, true);
            ctx.getSource()
                    .sendSuccess(() -> Component.literal("Forced taint spread at " + pos.toShortString()), false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int setFluxGoo(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            int level = IntegerArgumentType.getInteger(ctx, "level");
            BlockPos pos = player.blockPosition();
            ServerLevel serverLevel = (ServerLevel) player.level();
            var state = TCBlocks.FLUX_GOO.get().defaultBlockState();
            serverLevel.setBlock(pos, state, Block.UPDATE_ALL);
            ctx.getSource().sendSuccess(() -> Component.literal("Placed flux goo at level " + level), false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int giveEffect(CommandContext<CommandSourceStack> ctx, String key) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            Holder<MobEffect> effect =
                    switch (key) {
                        case "vis_exhaust" -> TCMobEffects.VIS_EXHAUST;
                        case "infectious_vis_exhaust" -> TCMobEffects.INFECTIOUS_VIS_EXHAUST;
                        case "flux_taint" -> TCMobEffects.FLUX_TAINT;
                        default -> null;
                    };
            if (effect == null) {
                ctx.getSource().sendFailure(Component.literal("Unknown effect: " + key));
                return 0;
            }
            player.addEffect(new MobEffectInstance(effect, 600, 0, true, true, true));
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int traceStreamPath(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        Vec3 from = Vec3Argument.getVec3(ctx, "from");
        Vec3 to = Vec3Argument.getVec3(ctx, "to");
        StreamPathfinder.Result result = StreamPathfinder.explore(level, from, to);
        if (result.directSight()) {
            ctx.getSource().sendSuccess(() -> Component.literal("LOS direct=true waypoints=0"), false);
            return Command.SINGLE_SUCCESS;
        }
        List<Vec3> waypoints = result.waypoints();
        if (waypoints == null) {
            ctx.getSource().sendSuccess(() -> Component.literal("NOPATH expanded=" + result.expanded()), false);
            return 0;
        }
        StringBuilder report = new StringBuilder("ROUTE n=")
                .append(waypoints.size())
                .append(" expanded=")
                .append(result.expanded());
        Vec3 cursor = from;
        boolean clean = true;
        for (int i = 0; i <= waypoints.size(); i++) {
            Vec3 next = i < waypoints.size() ? waypoints.get(i) : to;
            if (!StreamPathfinder.hasLineOfSight(level, cursor, next)) {
                report.append(" SEGMENT_BLOCKED=").append(i);
                clean = false;
            }
            cursor = next;
        }
        report.append(clean ? " SEGMENTS_OK" : " SEGMENTS_BAD");
        for (Vec3 wp : waypoints) {
            report.append(String.format(Locale.ROOT, " (%.1f,%.1f,%.1f)", wp.x, wp.y, wp.z));
        }
        String text = report.toString();
        ctx.getSource().sendSuccess(() -> Component.literal(text), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int spawnRift(CommandContext<CommandSourceStack> ctx, int size) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            ServerLevel level = (ServerLevel) player.level();
            EntityFluxRift rift = TCEntities.FLUX_RIFT.get().create(level);
            if (rift == null) {
                ctx.getSource().sendFailure(Component.literal("Failed to create rift"));
                return 0;
            }
            Vec3 pos = player.getEyePosition().add(player.getLookAngle().scale(RIFT_SPAWN_DISTANCE));
            rift.setRiftSeed(level.getRandom().nextInt());
            rift.moveTo(pos.x, pos.y, pos.z, level.getRandom().nextInt(360), 0.0F);
            rift.setRiftSize(size);
            level.addFreshEntity(rift);
            ctx.getSource().sendSuccess(() -> Component.literal("Spawned flux rift (size " + size + ")"), false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int spawnChampion(
            CommandContext<CommandSourceStack> ctx, @Nullable Holder<EntityType<?>> entityType) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            ServerLevel level = (ServerLevel) player.level();
            String modName = StringArgumentType.getString(ctx, "modifier").toLowerCase(Locale.ROOT);
            int type = -1;
            if (modName.equals("random")) {
                type = player.getRandom().nextInt(ChampionModifier.MODS.size());
            } else {
                for (int i = 0; i < ChampionModifier.MODS.size(); i++) {
                    if (ChampionModifier.MODS.get(i).name().equals(modName)) {
                        type = i;
                        break;
                    }
                }
            }
            if (type < 0) {
                ctx.getSource().sendFailure(Component.literal("Unknown champion modifier: " + modName));
                return 0;
            }
            EntityType<?> toSpawn = entityType == null ? EntityType.ZOMBIE : entityType.value();
            Entity entity = toSpawn.create(level);
            if (!(entity instanceof Monster monster)) {
                if (entity != null) {
                    entity.discard();
                }
                ctx.getSource()
                        .sendFailure(Component.literal(
                                "Champion modifiers only apply to monsters: " + toSpawn.getDescriptionId()));
                return 0;
            }
            Vec3 pos = player.position()
                    .add(player.getLookAngle()
                            .multiply(1.0, 0.0, 1.0)
                            .normalize()
                            .scale(CHAMPION_SPAWN_DISTANCE));
            monster.moveTo(pos.x, pos.y, pos.z, player.getYRot() + 180.0F, 0.0F);
            ChampionHelper.makeChampion(monster, true, type);
            level.addFreshEntity(monster);
            String finalName = ChampionModifier.MODS.get(type).name();
            ctx.getSource()
                    .sendSuccess(
                            () -> Component.literal("Spawned " + finalName + " champion "
                                    + monster.getName().getString()),
                            false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int spawnEntity(CommandContext<CommandSourceStack> ctx, String name) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            ServerLevel level = (ServerLevel) player.level();
            var type =
                    switch (name) {
                        case "thaumic_slime" -> TCEntities.THAUMIC_SLIME.get();
                        case "taint_crawler" -> TCEntities.TAINT_CRAWLER.get();
                        case "taint_seed" -> TCEntities.TAINT_SEED.get();
                        case "taint_seed_prime" -> TCEntities.TAINT_SEED_PRIME.get();
                        case "taint_swarm" -> TCEntities.TAINT_SWARM.get();
                        case "taintacle" -> TCEntities.TAINTACLE.get();
                        case "taintacle_small" -> TCEntities.TAINTACLE_SMALL.get();
                        default -> null;
                    };
            if (type == null) {
                ctx.getSource().sendFailure(Component.literal("Unknown entity: " + name));
                return 0;
            }
            var entity = type.create(level);
            if (entity == null) {
                ctx.getSource().sendFailure(Component.literal("Failed to create " + name));
                return 0;
            }
            entity.setPos(player.getX(), player.getY(), player.getZ());
            if (entity instanceof ThaumicSlime slime) {
                slime.setSize(2, true);
            }
            level.addFreshEntity(entity);
            ctx.getSource().sendSuccess(() -> Component.literal("Spawned " + name), false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int giveFocus(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            int tier = IntegerArgumentType.getInteger(ctx, "tier");
            String[] tokens =
                    StringArgumentType.getString(ctx, "elements").trim().split("\\s+");
            FocusPackage.Builder core = FocusPackage.builder().caster(player);
            int complexity = 0;
            for (String token : tokens) {
                ResourceLocation id = token.contains(":")
                        ? ResourceLocation.parse(token)
                        : ResourceLocation.fromNamespaceAndPath(TCIds.MODID, token);
                FocusElement element = FocusEngine.element(id);
                if (element == null) {
                    ctx.getSource().sendFailure(Component.literal("Unknown focus element: " + id));
                    return 0;
                }
                complexity += element.complexity(FocusSettings.defaults(element));
                core.add(id);
            }
            core.complexity(complexity);
            ItemFocus focusItem =
                    switch (tier) {
                        case 1 -> TCItems.FOCUS_1.get();
                        case 2 -> TCItems.FOCUS_2.get();
                        default -> TCItems.FOCUS_3.get();
                    };
            ItemStack focusStack = new ItemStack(focusItem);
            ItemFocus.setPackage(focusStack, core.build());
            int finalComplexity = complexity;
            if (complexity > focusItem.getMaxComplexity()) {
                ctx.getSource()
                        .sendSuccess(
                                () -> Component.literal("Warning: complexity " + finalComplexity + " exceeds tier cap "
                                        + focusItem.getMaxComplexity()),
                                false);
            }
            ItemStack held = player.getMainHandItem();
            if (held.getItem() instanceof ICaster caster) {
                ItemStack previous = caster.getFocusStack(held);
                if (!previous.isEmpty()) {
                    player.getInventory().add(previous);
                }
                caster.setFocus(held, focusStack);
                ctx.getSource()
                        .sendSuccess(
                                () -> Component.literal(
                                        "Socketed focus (complexity " + finalComplexity + ") into held caster"),
                                false);
            } else {
                player.getInventory().add(focusStack);
                ctx.getSource()
                        .sendSuccess(() -> Component.literal("Gave focus (complexity " + finalComplexity + ")"), false);
            }
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int giveCrystal(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            String tag = StringArgumentType.getString(ctx, "aspect");
            ResourceKey<IAspect> key =
                    ResourceKey.create(IAspect.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath(TCIds.MODID, tag));
            ItemStack stack = EssentiaCrystalFactory.of(player.registryAccess(), key);
            player.getInventory().add(stack);
            ctx.getSource().sendSuccess(() -> Component.literal("Gave crystal of " + tag), false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int generateOuterMaze(CommandContext<CommandSourceStack> ctx, int w, int h) {
        try {
            ServerLevel level = ctx.getSource().getLevel();
            MazeSavedData maze = MazeSavedData.get(level);
            RandomSource rand = level.getRandom();
            int width = w > 0 ? w : 15 + rand.nextInt(8) * 2;
            int height = h > 0 ? h : 15 + rand.nextInt(8) * 2;
            int chunkX = (int) ctx.getSource().getPosition().x >> 4;
            int chunkZ = (int) ctx.getSource().getPosition().z >> 4;
            if (maze.mazesInRange(chunkX, chunkZ, width, height)) {
                ctx.getSource().sendFailure(Component.literal("A maze already exists in range."));
                return 0;
            }
            maze.generateMaze(chunkX, chunkZ, width, height, rand.nextLong());
            ctx.getSource()
                    .sendSuccess(
                            () -> Component.literal("Maze " + width + "x" + height + " generated around chunk " + chunkX
                                    + "," + chunkZ + " (portal room at that chunk in the Outer Lands)"),
                            true);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int buildInfusionAltar(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ServerLevel level = player.serverLevel();
        BlockPos center = player.blockPosition().relative(player.getDirection(), 4);

        level.setBlockAndUpdate(center, TCBlocks.PEDESTAL_ARCANE.get().defaultBlockState());
        level.setBlockAndUpdate(center.above(2), TCBlocks.INFUSION_MATRIX.get().defaultBlockState());

        placeArcanePillar(level, center.offset(-1, 0, -1), Direction.NORTH);
        placeArcanePillar(level, center.offset(-1, 0, 1), Direction.WEST);
        placeArcanePillar(level, center.offset(1, 0, -1), Direction.EAST);
        placeArcanePillar(level, center.offset(1, 0, 1), Direction.SOUTH);

        for (int dx = -3; dx <= 3; dx += 3) {
            for (int dz = -3; dz <= 3; dz += 3) {
                if (dx != 0 || dz != 0) {
                    level.setBlockAndUpdate(
                            center.offset(dx, 0, dz),
                            TCBlocks.PEDESTAL_ARCANE.get().defaultBlockState());
                }
            }
        }

        level.removeBlock(center.offset(-1, 1, -1), false);
        level.removeBlock(center.offset(-1, 1, 1), false);
        level.removeBlock(center.offset(1, 1, -1), false);
        level.removeBlock(center.offset(1, 1, 1), false);

        ctx.getSource()
                .sendSuccess(
                        () -> Component.literal("Built infusion altar centered at "
                                + center.getX()
                                + " "
                                + center.getY()
                                + " "
                                + center.getZ()),
                        false);
        return Command.SINGLE_SUCCESS;
    }

    private static void placeArcanePillar(ServerLevel level, BlockPos pos, Direction facing) {
        level.setBlockAndUpdate(
                pos,
                TCBlocks.PILLAR_ARCANE
                        .get()
                        .defaultBlockState()
                        .setValue(BlockStateProperties.HORIZONTAL_FACING, facing));
    }

    private static int giveResearchTable(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            player.getInventory().add(new ItemStack(TCItems.RESEARCH_TABLE.get()));
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int giveThaumonomicon(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            player.getInventory().add(new ItemStack(TCItems.THAUMONOMICON.get()));
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int listParticles(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource()
                .sendSuccess(
                        () -> Component.literal("=== Thaumaturge Particle Demos ===")
                                .withStyle(ChatFormatting.GOLD),
                        false);
        ctx.getSource()
                .sendSuccess(
                        () -> Component.literal("Use /tc particle <name> — spawns 3 blocks in front of you")
                                .withStyle(ChatFormatting.GRAY),
                        false);
        for (var entry : ParticleDemos.DEMOS.entrySet()) {
            String name = entry.getKey();
            String desc = entry.getValue().description();
            ctx.getSource()
                    .sendSuccess(
                            () -> Component.literal(name + " ")
                                    .withStyle(ChatFormatting.YELLOW)
                                    .append(Component.literal("— " + desc).withStyle(ChatFormatting.WHITE)),
                            false);
        }
        ctx.getSource()
                .sendSuccess(
                        () -> Component.literal("Total: " + ParticleDemos.DEMOS.size() + " demos")
                                .withStyle(ChatFormatting.GOLD),
                        false);
        return Command.SINGLE_SUCCESS;
    }

    private static int runParticle(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            String name = StringArgumentType.getString(ctx, "name");
            if (!ParticleDemos.DEMOS.containsKey(name)) {
                ctx.getSource().sendFailure(Component.literal("Unknown demo: " + name + " — try /tc particle list"));
                return 0;
            }
            ParticleDemos.run(player, name);
            ctx.getSource()
                    .sendSuccess(
                            () -> Component.literal("Spawned demo: " + name).withStyle(ChatFormatting.GREEN), false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static final SuggestionProvider<CommandSourceStack> NODE_TYPES = (ctx, builder) -> {
        for (NodeType type : NodeType.values()) {
            builder.suggest(type.getSerializedName());
        }
        builder.suggest("random");
        return builder.buildFuture();
    };

    private static final SuggestionProvider<CommandSourceStack> NODE_LOCATE_TYPES =
            (ctx, builder) -> SharedSuggestionProvider.suggest(
                    Arrays.stream(NodeType.values()).map(NodeType::getSerializedName), builder);

    private static final SuggestionProvider<CommandSourceStack> NODE_MODIFIERS = (ctx, builder) -> {
        for (NodeModifier modifier : NodeModifier.values()) {
            builder.suggest(modifier.getSerializedName());
        }
        builder.suggest("none");
        return builder.buildFuture();
    };

    private static int spawnNode(
            CommandContext<CommandSourceStack> ctx, String typeName, String modifierName, String aspectSpec) {
        ServerLevel level = ctx.getSource().getLevel();
        BlockPos pos = BlockPos.containing(ctx.getSource().getPosition()).above(2);
        if ("random".equals(typeName)) {
            boolean placed = NodeGenerator.createRandomNodeAt(
                    level,
                    pos,
                    level.getRandom(),
                    false,
                    false,
                    false,
                    NodeGenerator.DEFAULT_SPECIAL_RARITY,
                    NodeGenerator.DEFAULT_BASE_AURA);
            ctx.getSource()
                    .sendSuccess(
                            () -> Component.literal(placed ? "Random node created" : "Could not place node"), true);
            return placed ? 1 : 0;
        }
        NodeType type = null;
        for (NodeType candidate : NodeType.values()) {
            if (candidate.getSerializedName().equals(typeName)) {
                type = candidate;
            }
        }
        if (type == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown node type: " + typeName));
            return 0;
        }
        NodeModifier modifier = null;
        for (NodeModifier candidate : NodeModifier.values()) {
            if (candidate.getSerializedName().equals(modifierName)) {
                modifier = candidate;
            }
        }
        HolderLookup.RegistryLookup<IAspect> aspects = level.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY);
        AspectList list;
        if (aspectSpec.isBlank()) {
            list = AspectList.EMPTY
                    .add(aspects.getOrThrow(TCAspects.AER), 20)
                    .add(aspects.getOrThrow(TCAspects.IGNIS), 20)
                    .add(aspects.getOrThrow(TCAspects.AQUA), 20)
                    .add(aspects.getOrThrow(TCAspects.TERRA), 20)
                    .add(aspects.getOrThrow(TCAspects.ORDO), 10)
                    .add(aspects.getOrThrow(TCAspects.PERDITIO), 10);
        } else {
            String[] tokens = aspectSpec.trim().split("\\s+");
            if (tokens.length % 2 != 0) {
                ctx.getSource()
                        .sendFailure(
                                Component.literal("Aspects must be pairs: <aspect> <amount> [<aspect> <amount> ...]"));
                return 0;
            }
            list = AspectList.EMPTY;
            for (int i = 0; i < tokens.length; i += 2) {
                ResourceLocation id = tokens[i].contains(":") ? ResourceLocation.parse(tokens[i]) : TCIds.rl(tokens[i]);
                Holder<IAspect> holder = aspects.get(ResourceKey.create(IAspect.REGISTRY_KEY, id))
                        .orElse(null);
                if (holder == null) {
                    ctx.getSource().sendFailure(Component.literal("Unknown aspect: " + tokens[i]));
                    return 0;
                }
                int amount;
                try {
                    amount = Integer.parseInt(tokens[i + 1]);
                } catch (NumberFormatException e) {
                    ctx.getSource().sendFailure(Component.literal("Bad amount: " + tokens[i + 1]));
                    return 0;
                }
                if (amount < 1) {
                    ctx.getSource().sendFailure(Component.literal("Amount must be positive: " + tokens[i + 1]));
                    return 0;
                }
                list = list.add(holder, amount);
            }
        }
        boolean placed = NodeGenerator.createNodeAt(level, pos, type, modifier, list);
        ctx.getSource().sendSuccess(() -> Component.literal(placed ? "Node created" : "Could not place node"), true);
        return placed ? 1 : 0;
    }

    private static int shareUnlink(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            int removed = ResearchLinkData.get(player.level().getServer()).unlinkAll(player.getUUID());
            ctx.getSource()
                    .sendSuccess(
                            () -> Component.literal(String.format("Removed %d research link links", removed)), false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> ResourceKey<T> getRegistryKey(
            CommandContext<CommandSourceStack> ctx,
            String name,
            ResourceKey<Registry<T>> registryKey,
            DynamicCommandExceptionType error)
            throws CommandSyntaxException {
        ResourceKey<?> key = ctx.getArgument(name, ResourceKey.class);
        Optional<ResourceKey<T>> cast = key.cast(registryKey);
        return cast.orElseThrow(() -> error.create(key.location()));
    }
}
