package com.leclowndu93150.thaumaturge.server.command;

import net.minecraft.world.item.Item;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.api.spell.SpellSummary;
import com.leclowndu93150.thaumaturge.api.spell.SpellProblem;
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.casters.ICaster;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTrait;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.api.nodes.NodeModifier;
import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.api.taint.TaintApi;
import com.leclowndu93150.thaumaturge.api.warp.IPlayerWarp;
import com.leclowndu93150.thaumaturge.api.warp.WarpHelper;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeGenerator;
import com.leclowndu93150.thaumaturge.content.aura.pressure.FluxPressureEvent;
import com.leclowndu93150.thaumaturge.content.aura.pressure.FluxPressureEventTypes;
import com.leclowndu93150.thaumaturge.content.aura.pressure.FluxPressureEvents;
import com.leclowndu93150.thaumaturge.content.effect.StreamPathfinder;
import com.leclowndu93150.thaumaturge.content.eldritch.maze.MazeSavedData;
import com.leclowndu93150.thaumaturge.content.entity.EntityFluxRift;
import com.leclowndu93150.thaumaturge.content.entity.ThaumicSlime;
import com.leclowndu93150.thaumaturge.content.entity.champion.ChampionHelper;
import com.leclowndu93150.thaumaturge.content.research.PlayerKnowledge;
import com.leclowndu93150.thaumaturge.content.research.ResearchGrants;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import com.leclowndu93150.thaumaturge.content.research.link.ResearchLinkData;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import com.leclowndu93150.thaumaturge.content.taint.flux.BlockFluxGas;
import com.leclowndu93150.thaumaturge.content.taint.flux.FluxGooFluid;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import com.leclowndu93150.thaumaturge.content.warp.WarpEvents;
import com.leclowndu93150.thaumaturge.data.worldgen.feature.TTConfiguredFeatures;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import com.leclowndu93150.thaumaturge.registry.TTMobTraits;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.commands.SummonCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TTIds.MODID)
public final class TTCommands {
    private TTCommands() {}

    private static final SuggestionProvider<CommandSourceStack> PARTICLE_NAMES = (ctx, builder) -> SharedSuggestionProvider.suggest(ParticleDemos.DEMOS.keySet(), builder);
    private static final int DEFAULT_RIFT_SIZE = 20;
    private static final int COMMAND_MAX_RIFT_SIZE = 500;
    private static final double RIFT_SPAWN_DISTANCE = 6.0;

    private static final SuggestionProvider<CommandSourceStack> WARP_TYPES = (ctx, builder) -> SharedSuggestionProvider
            .suggest(Arrays.stream(WarpType.values()).map(t -> t.name().toLowerCase(Locale.ROOT)), builder);

    private static final SuggestionProvider<CommandSourceStack> FLUX_EVENTS = (ctx, builder) -> SharedSuggestionProvider.suggest(FluxPressureEventTypes.ALL.stream().map(FluxPressureEvent::name),
            builder);

    private static final String RANDOM_CHAMPION = "random";

    private static final SuggestionProvider<CommandSourceStack> CHAMPION_MODS = (ctx, builder) -> SharedSuggestionProvider
            .suggest(Stream.concat(ChampionHelper.championTraits().stream().map(trait -> trait.unwrapKey().orElseThrow().identifier().getPath()), Stream.of(RANDOM_CHAMPION)), builder);

    private static final DynamicCommandExceptionType ERROR_INVALID_TRAIT = new DynamicCommandExceptionType((value) -> Component.literal("Unknown Mob Trait : " + value));

    private static final DynamicCommandExceptionType ERROR_NOT_LIVING = new DynamicCommandExceptionType((value) -> Component.literal("Only living entities can be tainted: " + value));

    private static final double CHAMPION_SPAWN_DISTANCE = 4.0;

    private static final DynamicCommandExceptionType ERROR_INVALID_GATE = new DynamicCommandExceptionType((value) -> Component.literal("Unknown Research Entry : " + value));
    private static final DynamicCommandExceptionType ERROR_INVALID_ASPECT = new DynamicCommandExceptionType((value) -> Component.literal("Unknown Aspect : " + value));

    @SubscribeEvent
    public static void onRegister(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> tc = TTCommandRoot.root().then(Commands.literal("table").executes(TTCommands::giveResearchTable))
                .then(Commands.literal("outermaze").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).executes(ctx -> generateOuterMaze(ctx, 0, 0))
                        .then(Commands.argument("width", IntegerArgumentType.integer(5, 31))
                                .then(Commands.argument("height", IntegerArgumentType.integer(5, 31))
                                        .executes(ctx -> generateOuterMaze(ctx, IntegerArgumentType.getInteger(ctx, "width"), IntegerArgumentType.getInteger(ctx, "height"))))))
                .then(Commands.literal("book").executes(TTCommands::giveThaumonomicon))
                .then(Commands.literal("particle").then(Commands.literal("list").executes(TTCommands::listParticles))
                        .then(Commands.argument("name", StringArgumentType.word()).suggests(PARTICLE_NAMES).executes(TTCommands::runParticle)))
                .then(Commands.literal("flux_goo").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.literal("set").then(Commands.argument("level", IntegerArgumentType.integer(1, 8)).executes(TTCommands::setFluxGoo))))
                .then(Commands.literal("flux_gas").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.literal("set").then(Commands.argument("level", IntegerArgumentType.integer(1, 8)).executes(TTCommands::setFluxGas))))
                .then(Commands.literal("flux_event").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("type", StringArgumentType.word()).suggests(FLUX_EVENTS).executes(TTCommands::triggerFluxEvent)))
                .then(Commands.literal("effect").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).then(Commands.literal("vis_exhaust").executes(ctx -> giveEffect(ctx, "vis_exhaust")))
                        .then(Commands.literal("infectious_vis_exhaust").executes(ctx -> giveEffect(ctx, "infectious_vis_exhaust")))
                        .then(Commands.literal("flux_taint").executes(ctx -> giveEffect(ctx, "flux_taint"))))
                .then(Commands.literal("entity").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).then(Commands.literal("thaumic_slime").executes(ctx -> spawnEntity(ctx, "thaumic_slime")))
                        .then(Commands.literal("taint_crawler").executes(ctx -> spawnEntity(ctx, "taint_crawler"))).then(Commands.literal("taint_seed").executes(ctx -> spawnEntity(ctx, "taint_seed")))
                        .then(Commands.literal("taint_seed_prime").executes(ctx -> spawnEntity(ctx, "taint_seed_prime")))
                        .then(Commands.literal("taint_swarm").executes(ctx -> spawnEntity(ctx, "taint_swarm"))).then(Commands.literal("taintacle").executes(ctx -> spawnEntity(ctx, "taintacle"))).then(
                                Commands.literal("taintacle_small").executes(ctx -> spawnEntity(ctx, "taintacle_small"))))
                .then(Commands
                        .literal("champion").requires(
                                Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("modifier", StringArgumentType.word()).suggests(CHAMPION_MODS).executes(ctx -> spawnChampion(ctx, null))
                                .then(Commands.argument("entity", ResourceArgument.resource(event.getBuildContext(), Registries.ENTITY_TYPE))
                                        .executes(ctx -> spawnChampion(ctx, ResourceArgument.getResource(ctx, "entity", Registries.ENTITY_TYPE))))))
                .then(Commands.literal("tainted").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("entity", ResourceArgument.resource(event.getBuildContext(), Registries.ENTITY_TYPE))
                                .suggests(SuggestionProviders.cast(SuggestionProviders.SUMMONABLE_ENTITIES)).executes(ctx -> summonTainted(ctx, ctx.getSource().getPosition()))
                                .then(Commands.argument("pos", Vec3Argument.vec3()).executes(ctx -> summonTainted(ctx, Vec3Argument.getVec3(ctx, "pos"))))))
                .then(Commands.literal("trait").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.literal("add")
                                .then(Commands.argument("targets", EntityArgument.entities())
                                        .then(Commands.argument("trait", ResourceKeyArgument.key(MobTrait.REGISTRY_KEY)).executes(ctx -> changeTrait(ctx, true)))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("targets", EntityArgument.entities())
                                        .then(Commands.argument("trait", ResourceKeyArgument.key(MobTrait.REGISTRY_KEY)).executes(ctx -> changeTrait(ctx, false)))))
                        .then(Commands.literal("list").then(Commands.argument("target", EntityArgument.entity()).executes(TTCommands::listTraits))))
                .then(Commands.literal("streampath").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("from", Vec3Argument.vec3()).then(Commands.argument("to", Vec3Argument.vec3()).executes(TTCommands::traceStreamPath))))
                .then(Commands.literal("rift").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).executes(ctx -> spawnRift(ctx, DEFAULT_RIFT_SIZE))
                        .then(Commands.argument("size", IntegerArgumentType.integer(1, COMMAND_MAX_RIFT_SIZE)).executes(ctx -> spawnRift(ctx, IntegerArgumentType.getInteger(ctx, "size")))))
                .then(Commands.literal("crystal").then(Commands.argument("aspect", StringArgumentType.word()).executes(TTCommands::giveCrystal)))
                .then(Commands.literal("node").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).executes(ctx -> spawnNode(ctx, "random", "random", ""))
                        .then(Commands.argument("type", StringArgumentType.word()).suggests(NODE_TYPES).executes(ctx -> spawnNode(ctx, StringArgumentType.getString(ctx, "type"), "none", ""))
                                .then(Commands.argument("modifier", StringArgumentType.word()).suggests(NODE_MODIFIERS)
                                        .executes(ctx -> spawnNode(ctx, StringArgumentType.getString(ctx, "type"), StringArgumentType.getString(ctx, "modifier"), ""))
                                        .then(Commands.argument("aspects", StringArgumentType.greedyString())
                                                .executes(ctx -> spawnNode(ctx, StringArgumentType.getString(ctx, "type"), StringArgumentType.getString(ctx, "modifier"),
                                                        StringArgumentType.getString(ctx, "aspects")))))))
                .then(Commands.literal("link").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).then(Commands.literal("unlink").executes(TTCommands::shareUnlink))).then(
                        Commands.literal("focus").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                .then(Commands.argument("tier", IntegerArgumentType.integer(1, 3))
                                        .then(Commands.argument("parts", StringArgumentType.greedyString()).suggests(SpellPartArguments.SUGGESTIONS).executes(TTCommands::giveFocus))))
                .then(Commands
                        .literal("warp").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).then(
                                Commands.literal("info").executes(TTCommands::warpInfo))
                        .then(Commands.literal("add")
                                .then(Commands.argument("type", StringArgumentType.word()).suggests(WARP_TYPES)
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 500)).executes(ctx -> warpModify(ctx, false)))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("type", StringArgumentType.word()).suggests(WARP_TYPES)
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 500)).executes(ctx -> warpModify(ctx, true)))))
                        .then(Commands.literal("clear").executes(TTCommands::warpClear)).then(Commands.literal("event").executes(TTCommands::warpEvent)))
                .then(Commands.literal("aura").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).then(Commands.literal("info").executes(TTCommands::auraInfo))
                        .then(Commands.literal("vis").then(Commands.literal("add").then(Commands.argument("amount", FloatArgumentType.floatArg(0.0F)).executes(ctx -> auraVis(ctx, false))))
                                .then(Commands.literal("remove").then(Commands.argument("amount", FloatArgumentType.floatArg(0.0F)).executes(ctx -> auraVis(ctx, true)))))
                        .then(Commands
                                .literal("flux").then(Commands.literal("add").then(Commands.argument("amount", FloatArgumentType.floatArg(0.0F)).executes(ctx -> auraFlux(ctx, false)))).then(
                                        Commands.literal("remove").then(Commands.argument("amount", FloatArgumentType.floatArg(0.0F)).executes(ctx -> auraFlux(ctx, true))))))
                .then(Commands.literal("taint").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).then(Commands.literal("seed").executes(ctx -> spawnEntity(ctx, "taint_seed")))
                        .then(Commands.literal("spread").executes(TTCommands::taintSpread)))
                .then(Commands.literal("tree").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.literal("greatwood").executes(ctx -> placeFeature(ctx, TTConfiguredFeatures.GREATWOOD_TREE)))
                        .then(Commands.literal("silverwood").executes(ctx -> placeFeature(ctx, TTConfiguredFeatures.SILVERWOOD_TREE)))
                        .then(Commands.literal("magic").executes(ctx -> placeFeature(ctx, TTConfiguredFeatures.BIG_MAGIC_TREE))))
                .then(Commands.literal("research").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.literal("grant").then(Commands.argument("entry", ResourceKeyArgument.key(IResearchEntry.REGISTRY_KEY)).executes(TTCommands::grantGate)))
                        .then(Commands.literal("revoke").then(Commands.argument("entry", ResourceKeyArgument.key(IResearchEntry.REGISTRY_KEY)).executes(TTCommands::revokeGate)))
                        .then(Commands.literal("reset").executes(TTCommands::resetResearch)).then(Commands.literal("all").executes(TTCommands::grantAllResearch)))
                .then(Commands.literal("aspect").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.literal("all").executes(ctx -> grantAllAspects(ctx, AspectPools.SOFT_CAP))
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1, 10000)).executes(ctx -> grantAllAspects(ctx, IntegerArgumentType.getInteger(ctx, "amount")))))
                        .then(Commands.argument("aspect", ResourceKeyArgument.key(IAspect.REGISTRY_KEY)).executes(ctx -> grantAspect(ctx, AspectPools.SOFT_CAP))
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1, 10000)).executes(ctx -> grantAspect(ctx, IntegerArgumentType.getInteger(ctx, "amount"))))));
        event.getDispatcher().register(tc);
    }

    private static int resetResearch(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            PlayerKnowledge knowledge = (PlayerKnowledge) KnowledgeAccess.of(player);
            int cleared = knowledge.researchList().size();
            knowledge.clear();
            ResearchManager.applyAutoUnlock(player);
            knowledge.sync(player);
            ctx.getSource().sendSuccess(() -> Component.literal(String.format("Reset %d research entries and all knowledge", cleared)), false);
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
            ctx.getSource().sendSuccess(() -> Component.literal(String.format("Granted %d research entries and all aspect research points", granted)), false);
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
            ctx.getSource().sendSuccess(() -> Component.literal(String.format("Granted %d research points to all %d aspects", amount, count)), false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int grantAspect(CommandContext<CommandSourceStack> ctx, int amount) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            ResourceKey<IAspect> key = ResourceKeyArgument.getRegistryKey(ctx, "aspect", IAspect.REGISTRY_KEY, ERROR_INVALID_ASPECT);
            Holder<IAspect> aspect = player.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).getOrThrow(key);
            AspectPools.grantForCommand(player, aspect, amount);
            ctx.getSource().sendSuccess(() -> Component.literal(String.format("Granted %d research points of %s", amount, key.identifier())), false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int revokeGate(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            ResourceKey<IResearchEntry> key = ResourceKeyArgument.getRegistryKey(ctx, "entry", IResearchEntry.REGISTRY_KEY, ERROR_INVALID_GATE);
            PlayerKnowledge knowledge = (PlayerKnowledge) KnowledgeAccess.of(player);
            if (!knowledge.isResearchKnown(key.identifier())) {
                ctx.getSource().sendSuccess(() -> Component.literal(String.format("Research %s is not known", key.identifier())), false);
                return Command.SINGLE_SUCCESS;
            }
            if (knowledge.removeResearch(key.identifier())) {
                knowledge.sync(player);
                ctx.getSource().sendSuccess(() -> Component.literal(String.format("Revoked research %s ", key.identifier())), false);
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
            ResourceKey<IResearchEntry> key = ResourceKeyArgument.getRegistryKey(ctx, "entry", IResearchEntry.REGISTRY_KEY, ERROR_INVALID_GATE);
            Holder<IResearchEntry> holder = level.registryAccess().lookupOrThrow(IResearchEntry.REGISTRY_KEY).getOrThrow(key);
            if (((PlayerKnowledge) KnowledgeAccess.of(player)).isResearchComplete(key.identifier())) {
                ctx.getSource().sendSuccess(() -> Component.literal(String.format("Research %s is already complete", key.identifier())), false);
                return Command.SINGLE_SUCCESS;
            }
            if (ResearchManager.complete(player, key.identifier())) {
                ResearchManager.setStage(player, key.identifier(), holder.value().stages().size());
                ctx.getSource().sendSuccess(() -> Component.literal(String.format("Unlocked research %s ", key.identifier())), false);
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
            ctx.getSource().sendSuccess(() -> Component.literal(
                    String.format("Warp: permanent %d, normal %d, temporary %d, counter %d", warp.get(WarpType.PERMANENT), warp.get(WarpType.NORMAL), warp.get(WarpType.TEMPORARY), warp.getCounter())),
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
            WarpType type = WarpType.valueOf(StringArgumentType.getString(ctx, "type").toUpperCase(Locale.ROOT));
            int amount = IntegerArgumentType.getInteger(ctx, "amount");
            WarpHelper.addWarp(player, remove ? -amount : amount, type);
            ctx.getSource().sendSuccess(() -> Component.literal((remove ? "Removed " : "Added ") + amount + " " + type + " warp"), false);
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
            player.syncData(TTAttachments.WARP);
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
            ctx.getSource().sendSuccess(() -> Component.literal(String.format("Aura at %s: vis %.1f, flux %.1f, base %d", pos.toShortString(), vis, flux, base)), false);
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
                ctx.getSource().sendSuccess(() -> Component.literal(String.format("Drained %.1f flux", drained)), false);
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
            Holder<ConfiguredFeature<?, ?>> holder = level.registryAccess().lookupOrThrow(Registries.CONFIGURED_FEATURE).getOrThrow(key);
            boolean placed = holder.value().place(level, level.getChunkSource().getGenerator(), level.getRandom(), pos);
            if (placed) {
                ctx.getSource().sendSuccess(() -> Component.literal("Placed " + key.identifier()), false);
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
            ctx.getSource().sendSuccess(() -> Component.literal("Forced taint spread at " + pos.toShortString()), false);
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
            serverLevel.setBlock(pos, FluxGooFluid.gooBlockState(level), Block.UPDATE_ALL);
            ctx.getSource().sendSuccess(() -> Component.literal("Placed flux goo at level " + level), false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int setFluxGas(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            int level = IntegerArgumentType.getInteger(ctx, "level");
            BlockPos pos = player.blockPosition();
            ServerLevel serverLevel = (ServerLevel) player.level();
            serverLevel.setBlock(pos, BlockFluxGas.gasBlockState(level), Block.UPDATE_ALL);
            ctx.getSource().sendSuccess(() -> Component.literal("Placed flux gas at level " + level), false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int triggerFluxEvent(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        String name = StringArgumentType.getString(ctx, "type");
        FluxPressureEvent event = FluxPressureEventTypes.byName(name);
        if (event == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown flux event: " + name));
            return 0;
        }
        if (!FluxPressureEvents.trigger(player.level(), player.blockPosition(), event)) {
            ctx.getSource().sendFailure(Component.literal("Flux event " + name + " could not trigger here (needs " + event.cost() + " local Flux, a valid target, and fluxPressureEvents on)"));
            return 0;
        }
        ctx.getSource().sendSuccess(() -> Component.literal("Triggered flux event " + name + " for " + event.cost() + " Flux"), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int giveEffect(CommandContext<CommandSourceStack> ctx, String key) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            Holder<MobEffect> effect = switch (key) {
                case "vis_exhaust" -> TTMobEffects.VIS_EXHAUST;
                case "infectious_vis_exhaust" -> TTMobEffects.INFECTIOUS_VIS_EXHAUST;
                case "flux_taint" -> TTMobEffects.FLUX_TAINT;
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
        StringBuilder report = new StringBuilder("ROUTE n=").append(waypoints.size()).append(" expanded=").append(result.expanded());
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
            EntityFluxRift rift = TTEntities.FLUX_RIFT.get().create(level, EntitySpawnReason.COMMAND);
            if (rift == null) {
                ctx.getSource().sendFailure(Component.literal("Failed to create rift"));
                return 0;
            }
            Vec3 pos = player.getEyePosition().add(player.getLookAngle().scale(RIFT_SPAWN_DISTANCE));
            rift.setRiftSeed(level.getRandom().nextInt());
            rift.snapTo(pos.x, pos.y, pos.z, level.getRandom().nextInt(360), 0.0F);
            rift.setRiftSize(size);
            level.addFreshEntity(rift);
            ctx.getSource().sendSuccess(() -> Component.literal("Spawned flux rift (size " + size + ")"), false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int spawnChampion(CommandContext<CommandSourceStack> ctx, @Nullable Holder<EntityType<?>> entityType) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            ServerLevel level = (ServerLevel) player.level();
            String modName = StringArgumentType.getString(ctx, "modifier").toLowerCase(Locale.ROOT);
            List<Holder<MobTrait>> champions = ChampionHelper.championTraits();
            Holder<MobTrait> trait = null;
            if (modName.equals(RANDOM_CHAMPION) && !champions.isEmpty()) {
                trait = champions.get(player.getRandom().nextInt(champions.size()));
            }
            for (Holder<MobTrait> candidate : champions) {
                if (trait == null && candidate.unwrapKey().orElseThrow().identifier().getPath().equals(modName)) {
                    trait = candidate;
                }
            }
            if (trait == null) {
                ctx.getSource().sendFailure(Component.literal("Unknown champion modifier: " + modName));
                return 0;
            }
            EntityType<?> toSpawn = entityType == null ? EntityType.ZOMBIE : entityType.value();
            Entity entity = toSpawn.create(level, EntitySpawnReason.COMMAND);
            if (!(entity instanceof Mob mob)) {
                if (entity != null) {
                    entity.discard();
                }
                ctx.getSource().sendFailure(Component.literal("Champion modifiers only apply to mobs: " + toSpawn.getDescriptionId()));
                return 0;
            }
            Vec3 pos = player.position().add(player.getLookAngle().multiply(1.0, 0.0, 1.0).normalize().scale(CHAMPION_SPAWN_DISTANCE));
            mob.snapTo(pos.x, pos.y, pos.z, player.getYRot() + 180.0F, 0.0F);
            ChampionHelper.makeChampion(mob, true, trait);
            level.addFreshEntity(mob);
            String finalName = modName;
            ctx.getSource().sendSuccess(() -> Component.literal("Spawned " + finalName + " champion " + mob.getName().getString()), false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int summonTainted(CommandContext<CommandSourceStack> ctx, Vec3 pos) throws CommandSyntaxException {
        Entity entity = SummonCommand.createEntity(ctx.getSource(), ResourceArgument.getSummonableEntityType(ctx, "entity"), pos, new CompoundTag(), true);
        if (!(entity instanceof LivingEntity living)) {
            entity.discard();
            throw ERROR_NOT_LIVING.create(EntityType.getKey(entity.getType()));
        }
        MobTraits.add(living, TTMobTraits.TAINTED);
        ctx.getSource().sendSuccess(() -> Component.literal("Summoned tainted " + living.getName().getString()), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int changeTrait(CommandContext<CommandSourceStack> ctx, boolean add) throws CommandSyntaxException {
        ResourceKey<MobTrait> key = ResourceKeyArgument.getRegistryKey(ctx, "trait", MobTrait.REGISTRY_KEY, ERROR_INVALID_TRAIT);
        Holder<MobTrait> trait = TTMobTraits.registry().get(key).orElseThrow(() -> ERROR_INVALID_TRAIT.create(key.identifier()));
        int changed = 0;
        for (Entity entity : EntityArgument.getEntities(ctx, "targets")) {
            if (entity instanceof LivingEntity living && (add ? MobTraits.add(living, trait) : MobTraits.remove(living, trait))) {
                changed++;
            }
        }
        int finalChanged = changed;
        ctx.getSource().sendSuccess(() -> Component.literal((add ? "Added " : "Removed ") + key.identifier() + " on " + finalChanged + " entities"), true);
        return changed;
    }

    private static int listTraits(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Entity entity = EntityArgument.getEntity(ctx, "target");
        List<Holder<MobTrait>> traits = entity instanceof LivingEntity living ? MobTraits.traits(living) : List.of();
        String names = traits.stream().map(trait -> trait.unwrapKey().orElseThrow().identifier().toString()).collect(Collectors.joining(", "));
        ctx.getSource().sendSuccess(() -> Component.literal(entity.getName().getString() + ": " + (names.isEmpty() ? "no traits" : names)), false);
        return traits.size();
    }

    private static int spawnEntity(CommandContext<CommandSourceStack> ctx, String name) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            ServerLevel level = (ServerLevel) player.level();
            var type = switch (name) {
                case "thaumic_slime" -> TTEntities.THAUMIC_SLIME.get();
                case "taint_crawler" -> TTEntities.TAINT_CRAWLER.get();
                case "taint_seed" -> TTEntities.TAINT_SEED.get();
                case "taint_seed_prime" -> TTEntities.TAINT_SEED_PRIME.get();
                case "taint_swarm" -> TTEntities.TAINT_SWARM.get();
                case "taintacle" -> TTEntities.TAINTACLE.get();
                case "taintacle_small" -> TTEntities.TAINTACLE_SMALL.get();
                default -> null;
            };
            if (type == null) {
                ctx.getSource().sendFailure(Component.literal("Unknown entity: " + name));
                return 0;
            }
            var entity = type.create(level, EntitySpawnReason.COMMAND);
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
            Spell spell = SpellPartArguments.chain(StringArgumentType.getString(ctx, "parts"), ctx.getSource().registryAccess());
            Item focusItem = switch (tier) {
                case 1 -> TTItems.FOCUS_1.get();
                case 2 -> TTItems.FOCUS_2.get();
                default -> TTItems.FOCUS_3.get();
            };
            ItemStack focusStack = new ItemStack(focusItem);
            Spells.setSpell(focusStack, spell);
            SpellSummary summary = Spells.analyze(spell, Spells.tierOf(focusStack).orElse(null), ctx.getSource().registryAccess(), null);
            for (SpellProblem problem : summary.problems()) {
                ctx.getSource().sendSuccess(() -> problem.message(), false);
            }
            ItemStack held = player.getMainHandItem();
            if (held.getItem() instanceof ICaster caster) {
                ItemStack previous = caster.getFocusStack(held);
                if (!previous.isEmpty()) {
                    player.getInventory().add(previous);
                }
                caster.setFocus(held, focusStack);
                ctx.getSource().sendSuccess(() -> Component.literal("Socketed focus (complexity " + summary.complexity() + "/" + summary.budget() + ") into held caster"), false);
            } else {
                player.getInventory().add(focusStack);
                ctx.getSource().sendSuccess(() -> Component.literal("Gave focus (complexity " + summary.complexity() + "/" + summary.budget() + ")"), false);
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
            ResourceKey<IAspect> key = ResourceKey.create(IAspect.REGISTRY_KEY, Identifier.fromNamespaceAndPath(TTIds.MODID, tag));
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
            ctx.getSource().sendSuccess(
                    () -> Component.literal("Maze " + width + "x" + height + " generated around chunk " + chunkX + "," + chunkZ + " (portal room at that chunk in the Outer Lands)"), true);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int giveResearchTable(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            player.getInventory().add(new ItemStack(TTItems.RESEARCH_TABLE.get()));
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int giveThaumonomicon(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            player.getInventory().add(new ItemStack(TTItems.THAUMONOMICON.get()));
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int listParticles(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(() -> Component.literal("=== Thaumaturge Particle Demos ===").withStyle(ChatFormatting.GOLD), false);
        ctx.getSource().sendSuccess(() -> Component.literal("Use /thaumaturge particle <name> to spawn one 3 blocks in front of you").withStyle(ChatFormatting.GRAY), false);
        for (var entry : ParticleDemos.DEMOS.entrySet()) {
            String name = entry.getKey();
            String desc = entry.getValue().description();
            ctx.getSource().sendSuccess(() -> Component.literal(name + " ").withStyle(ChatFormatting.YELLOW).append(Component.literal("— " + desc).withStyle(ChatFormatting.WHITE)), false);
        }
        ctx.getSource().sendSuccess(() -> Component.literal("Total: " + ParticleDemos.DEMOS.size() + " demos").withStyle(ChatFormatting.GOLD), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int runParticle(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            String name = StringArgumentType.getString(ctx, "name");
            if (!ParticleDemos.DEMOS.containsKey(name)) {
                ctx.getSource().sendFailure(Component.literal("Unknown demo: " + name + ", try /thaumaturge particle list"));
                return 0;
            }
            ParticleDemos.run(player, name);
            ctx.getSource().sendSuccess(() -> Component.literal("Spawned demo: " + name).withStyle(ChatFormatting.GREEN), false);
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

    private static final SuggestionProvider<CommandSourceStack> NODE_MODIFIERS = (ctx, builder) -> {
        for (NodeModifier modifier : NodeModifier.values()) {
            builder.suggest(modifier.getSerializedName());
        }
        builder.suggest("none");
        return builder.buildFuture();
    };

    private static int spawnNode(CommandContext<CommandSourceStack> ctx, String typeName, String modifierName, String aspectSpec) {
        ServerLevel level = ctx.getSource().getLevel();
        BlockPos pos = BlockPos.containing(ctx.getSource().getPosition()).above(2);
        if ("random".equals(typeName)) {
            boolean placed = NodeGenerator.createRandomNodeAt(level, pos, level.getRandom(), false, false, false, NodeGenerator.DEFAULT_SPECIAL_RARITY, NodeGenerator.DEFAULT_BASE_AURA);
            ctx.getSource().sendSuccess(() -> Component.literal(placed ? "Random node created" : "Could not place node"), true);
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
            list = AspectList.EMPTY.add(aspects.getOrThrow(TTAspects.AER), 20).add(aspects.getOrThrow(TTAspects.IGNIS), 20).add(aspects.getOrThrow(TTAspects.AQUA), 20)
                    .add(aspects.getOrThrow(TTAspects.TERRA), 20).add(aspects.getOrThrow(TTAspects.ORDO), 10).add(aspects.getOrThrow(TTAspects.PERDITIO), 10);
        } else {
            String[] tokens = aspectSpec.trim().split("\\s+");
            if (tokens.length % 2 != 0) {
                ctx.getSource().sendFailure(Component.literal("Aspects must be pairs: <aspect> <amount> [<aspect> <amount> ...]"));
                return 0;
            }
            list = AspectList.EMPTY;
            for (int i = 0; i < tokens.length; i += 2) {
                Identifier id = tokens[i].contains(":") ? Identifier.parse(tokens[i]) : TTIds.rl(tokens[i]);
                Holder<IAspect> holder = aspects.get(ResourceKey.create(IAspect.REGISTRY_KEY, id)).orElse(null);
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
            ctx.getSource().sendSuccess(() -> Component.literal(String.format("Removed %d research link links", removed)), false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }
}
