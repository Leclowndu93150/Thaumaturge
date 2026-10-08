package com.leclowndu93150.thaumaturge.server.command;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.Thaumaturge;
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
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.SpellProblem;
import com.leclowndu93150.thaumaturge.api.spell.SpellSummary;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.api.taint.TaintApi;
import com.leclowndu93150.thaumaturge.api.warp.IPlayerWarp;
import com.leclowndu93150.thaumaturge.api.warp.WarpHelper;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeGenerator;
import com.leclowndu93150.thaumaturge.content.aura.pressure.FluxPressureEvent;
import com.leclowndu93150.thaumaturge.content.aura.pressure.FluxPressureEventTypes;
import com.leclowndu93150.thaumaturge.content.aura.pressure.FluxPressureEvents;
import com.leclowndu93150.thaumaturge.content.effect.StreamPathfinder;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
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
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.commands.SummonCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
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
    private static final String KEY = "commands.thaumaturge.tools.";
    private static final int DEFAULT_RIFT_SIZE = 20;
    private static final int COMMAND_MAX_RIFT_SIZE = 500;
    private static final double RIFT_SPAWN_DISTANCE = 6.0;
    private static final double CHAMPION_SPAWN_DISTANCE = 4.0;
    private static final int MAX_WARP_CHANGE = 500;
    private static final int MAX_ASPECT_GRANT = 10000;
    private static final int MAX_FLUX_LEVEL = 8;
    private static final int MAX_FOCUS_TIER = 3;
    private static final int EFFECT_DURATION_TICKS = 600;
    private static final int SPAWNED_SLIME_SIZE = 2;
    private static final int NODE_HEIGHT_ABOVE_SOURCE = 2;
    private static final int DEFAULT_NODE_MAIN_PRIMAL = 20;
    private static final int DEFAULT_NODE_MINOR_PRIMAL = 10;
    private static final String RANDOM = "random";
    private static final String NO_MODIFIER = "none";

    private static final Map<String, Holder<MobEffect>> EFFECTS = new LinkedHashMap<>();
    private static final Map<String, Supplier<? extends EntityType<?>>> ENTITIES = new LinkedHashMap<>();

    static {
        EFFECTS.put("vis_exhaust", TTMobEffects.VIS_EXHAUST);
        EFFECTS.put("infectious_vis_exhaust", TTMobEffects.INFECTIOUS_VIS_EXHAUST);
        EFFECTS.put("flux_taint", TTMobEffects.FLUX_TAINT);
        ENTITIES.put("thaumic_slime", TTEntities.THAUMIC_SLIME);
        ENTITIES.put("taint_crawler", TTEntities.TAINT_CRAWLER);
        ENTITIES.put("taint_seed", TTEntities.TAINT_SEED);
        ENTITIES.put("taint_seed_prime", TTEntities.TAINT_SEED_PRIME);
        ENTITIES.put("taint_swarm", TTEntities.TAINT_SWARM);
        ENTITIES.put("taintacle", TTEntities.TAINTACLE);
        ENTITIES.put("taintacle_small", TTEntities.TAINTACLE_SMALL);
    }

    private static final SuggestionProvider<CommandSourceStack> PARTICLE_NAMES =
            (ctx, builder) -> SharedSuggestionProvider.suggest(ParticleDemos.DEMOS.keySet(), builder);
    private static final SuggestionProvider<CommandSourceStack> WARP_TYPES =
            (ctx, builder) -> SharedSuggestionProvider.suggest(
                    Arrays.stream(WarpType.values()).map(t -> t.name().toLowerCase(Locale.ROOT)), builder);
    private static final SuggestionProvider<CommandSourceStack> FLUX_EVENTS = (ctx, builder) ->
            SharedSuggestionProvider.suggest(FluxPressureEventTypes.ALL.stream().map(FluxPressureEvent::name), builder);
    private static final SuggestionProvider<CommandSourceStack> CHAMPION_MODS =
            (ctx, builder) -> SharedSuggestionProvider.suggest(
                    Stream.concat(
                            ChampionHelper.championTraits().stream()
                                    .map(trait -> trait.unwrapKey()
                                            .orElseThrow()
                                            .location()
                                            .getPath()),
                            Stream.of(RANDOM)),
                    builder);
    private static final SuggestionProvider<CommandSourceStack> NODE_TYPES =
            (ctx, builder) -> SharedSuggestionProvider.suggest(
                    Stream.concat(Arrays.stream(NodeType.values()).map(NodeType::getSerializedName), Stream.of(RANDOM)),
                    builder);
    private static final SuggestionProvider<CommandSourceStack> NODE_MODIFIERS =
            (ctx, builder) -> SharedSuggestionProvider.suggest(
                    Stream.concat(
                            Arrays.stream(NodeModifier.values()).map(NodeModifier::getSerializedName),
                            Stream.of(NO_MODIFIER)),
                    builder);

    private static final DynamicCommandExceptionType ERROR_INVALID_TRAIT = new DynamicCommandExceptionType(
            value -> Component.translatable(KEY + "unknown_trait", String.valueOf(value)));
    private static final DynamicCommandExceptionType ERROR_NOT_LIVING =
            new DynamicCommandExceptionType(value -> Component.translatable(KEY + "not_living", String.valueOf(value)));
    private static final DynamicCommandExceptionType ERROR_INVALID_GATE = new DynamicCommandExceptionType(
            value -> Component.translatable(KEY + "unknown_research", String.valueOf(value)));
    private static final DynamicCommandExceptionType ERROR_INVALID_ASPECT = new DynamicCommandExceptionType(
            value -> Component.translatable(KEY + "unknown_aspect", String.valueOf(value)));

    private TTCommands() {}

    @FunctionalInterface
    private interface Body {
        int run(CommandSourceStack source) throws CommandSyntaxException;
    }

    @FunctionalInterface
    private interface ContextBody {
        int run(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException;
    }

    private static Command<CommandSourceStack> guarded(String name, ContextBody body) {
        return ctx -> {
            try {
                return body.run(ctx);
            } catch (CommandSyntaxException e) {
                throw e;
            } catch (RuntimeException e) {
                Thaumaturge.LOGGER.error("Command /{} {} failed", TTCommandRoot.NAME, name, e);
                ctx.getSource().sendFailure(Component.translatable(KEY + "failed", name));
                return 0;
            }
        };
    }

    private static Command<CommandSourceStack> bySource(String name, Body body) {
        return guarded(name, ctx -> body.run(ctx.getSource()));
    }

    private static void success(CommandSourceStack source, String key, Object... args) {
        source.sendSuccess(() -> Component.translatable(KEY + key, args), false);
    }

    private static int failure(CommandSourceStack source, String key, Object... args) {
        source.sendFailure(Component.translatable(KEY + key, args));
        return 0;
    }

    private static String oneDecimal(float value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> admin(String name) {
        return Commands.literal(name).requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS));
    }

    @SubscribeEvent
    public static void onRegister(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> effect = admin("effect");
        EFFECTS.forEach((name, holder) ->
                effect.then(Commands.literal(name).executes(bySource("effect", source -> giveEffect(source, holder)))));
        LiteralArgumentBuilder<CommandSourceStack> entity = admin("entity");
        ENTITIES.forEach((name, type) -> entity.then(Commands.literal(name)
                .executes(bySource("entity", source -> spawnEntity(source, name, type.get())))));

        LiteralArgumentBuilder<CommandSourceStack> root = TTCommandRoot.root()
                .then(admin("table")
                        .executes(bySource(
                                "table", source -> give(source, new ItemStack(TTItems.RESEARCH_TABLE.get())))))
                .then(admin("book")
                        .executes(bySource("book", source -> give(source, new ItemStack(TTItems.THAUMONOMICON.get())))))
                .then(admin("particle")
                        .then(Commands.literal("list").executes(bySource("particle", TTCommands::listParticles)))
                        .then(Commands.argument("name", StringArgumentType.word())
                                .suggests(PARTICLE_NAMES)
                                .executes(guarded("particle", TTCommands::runParticle))))
                .then(admin("flux_goo")
                        .then(Commands.literal("set")
                                .then(Commands.argument("level", IntegerArgumentType.integer(1, MAX_FLUX_LEVEL))
                                        .executes(guarded("flux_goo", ctx -> placeFlux(ctx, false))))))
                .then(admin("flux_gas")
                        .then(Commands.literal("set")
                                .then(Commands.argument("level", IntegerArgumentType.integer(1, MAX_FLUX_LEVEL))
                                        .executes(guarded("flux_gas", ctx -> placeFlux(ctx, true))))))
                .then(admin("flux_event")
                        .then(Commands.argument("type", StringArgumentType.word())
                                .suggests(FLUX_EVENTS)
                                .executes(guarded("flux_event", TTCommands::triggerFluxEvent))))
                .then(effect)
                .then(entity)
                .then(admin("champion")
                        .then(Commands.argument("modifier", StringArgumentType.word())
                                .suggests(CHAMPION_MODS)
                                .executes(guarded("champion", ctx -> spawnChampion(ctx, null)))
                                .then(Commands.argument(
                                                "entity",
                                                ResourceArgument.resource(
                                                        event.getBuildContext(), Registries.ENTITY_TYPE))
                                        .executes(guarded(
                                                "champion",
                                                ctx -> spawnChampion(
                                                        ctx,
                                                        ResourceArgument.getResource(
                                                                ctx, "entity", Registries.ENTITY_TYPE)))))))
                .then(admin("tainted")
                        .then(Commands.argument(
                                        "entity",
                                        ResourceArgument.resource(event.getBuildContext(), Registries.ENTITY_TYPE))
                                .suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
                                .executes(guarded(
                                        "tainted",
                                        ctx -> summonTainted(
                                                ctx, ctx.getSource().getPosition())))
                                .then(Commands.argument("pos", Vec3Argument.vec3())
                                        .executes(guarded(
                                                "tainted",
                                                ctx -> summonTainted(ctx, Vec3Argument.getVec3(ctx, "pos")))))))
                .then(admin("trait")
                        .then(Commands.literal("add")
                                .then(Commands.argument("targets", EntityArgument.entities())
                                        .then(Commands.argument("trait", ResourceKeyArgument.key(MobTrait.REGISTRY_KEY))
                                                .executes(guarded("trait", ctx -> changeTrait(ctx, true))))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("targets", EntityArgument.entities())
                                        .then(Commands.argument("trait", ResourceKeyArgument.key(MobTrait.REGISTRY_KEY))
                                                .executes(guarded("trait", ctx -> changeTrait(ctx, false))))))
                        .then(Commands.literal("list")
                                .then(Commands.argument("target", EntityArgument.entity())
                                        .executes(guarded("trait", TTCommands::listTraits)))))
                .then(admin("streampath")
                        .then(Commands.argument("from", Vec3Argument.vec3())
                                .then(Commands.argument("to", Vec3Argument.vec3())
                                        .executes(guarded("streampath", TTCommands::traceStreamPath)))))
                .then(admin("rift")
                        .executes(bySource("rift", source -> spawnRift(source, DEFAULT_RIFT_SIZE)))
                        .then(Commands.argument("size", IntegerArgumentType.integer(1, COMMAND_MAX_RIFT_SIZE))
                                .executes(guarded(
                                        "rift",
                                        ctx -> spawnRift(
                                                ctx.getSource(), IntegerArgumentType.getInteger(ctx, "size"))))))
                .then(admin("crystal")
                        .then(Commands.argument("aspect", StringArgumentType.word())
                                .executes(guarded("crystal", TTCommands::giveCrystal))))
                .then(admin("node")
                        .executes(guarded("node", ctx -> spawnNode(ctx, RANDOM, NO_MODIFIER, "")))
                        .then(Commands.argument("type", StringArgumentType.word())
                                .suggests(NODE_TYPES)
                                .executes(guarded(
                                        "node",
                                        ctx -> spawnNode(
                                                ctx, StringArgumentType.getString(ctx, "type"), NO_MODIFIER, "")))
                                .then(Commands.argument("modifier", StringArgumentType.word())
                                        .suggests(NODE_MODIFIERS)
                                        .executes(guarded(
                                                "node",
                                                ctx -> spawnNode(
                                                        ctx,
                                                        StringArgumentType.getString(ctx, "type"),
                                                        StringArgumentType.getString(ctx, "modifier"),
                                                        "")))
                                        .then(Commands.argument("aspects", StringArgumentType.greedyString())
                                                .executes(guarded(
                                                        "node",
                                                        ctx -> spawnNode(
                                                                ctx,
                                                                StringArgumentType.getString(ctx, "type"),
                                                                StringArgumentType.getString(ctx, "modifier"),
                                                                StringArgumentType.getString(ctx, "aspects"))))))))
                .then(admin("link")
                        .then(Commands.literal("unlink").executes(bySource("link", TTCommands::shareUnlink))))
                .then(admin("focus")
                        .then(Commands.argument("tier", IntegerArgumentType.integer(1, MAX_FOCUS_TIER))
                                .then(Commands.argument("parts", StringArgumentType.greedyString())
                                        .suggests(SpellPartArguments.SUGGESTIONS)
                                        .executes(guarded("focus", TTCommands::giveFocus)))))
                .then(admin("warp")
                        .then(Commands.literal("info").executes(bySource("warp", TTCommands::warpInfo)))
                        .then(Commands.literal("add")
                                .then(Commands.argument("type", StringArgumentType.word())
                                        .suggests(WARP_TYPES)
                                        .then(Commands.argument(
                                                        "amount", IntegerArgumentType.integer(1, MAX_WARP_CHANGE))
                                                .executes(guarded("warp", ctx -> warpModify(ctx, false))))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("type", StringArgumentType.word())
                                        .suggests(WARP_TYPES)
                                        .then(Commands.argument(
                                                        "amount", IntegerArgumentType.integer(1, MAX_WARP_CHANGE))
                                                .executes(guarded("warp", ctx -> warpModify(ctx, true))))))
                        .then(Commands.literal("clear").executes(bySource("warp", TTCommands::warpClear)))
                        .then(Commands.literal("event").executes(bySource("warp", TTCommands::warpEvent))))
                .then(admin("aura")
                        .then(Commands.literal("info").executes(bySource("aura", TTCommands::auraInfo)))
                        .then(Commands.literal("vis")
                                .then(Commands.literal("add")
                                        .then(Commands.argument("amount", FloatArgumentType.floatArg(0.0F))
                                                .executes(guarded("aura", ctx -> auraVis(ctx, false)))))
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("amount", FloatArgumentType.floatArg(0.0F))
                                                .executes(guarded("aura", ctx -> auraVis(ctx, true))))))
                        .then(Commands.literal("flux")
                                .then(Commands.literal("add")
                                        .then(Commands.argument("amount", FloatArgumentType.floatArg(0.0F))
                                                .executes(guarded("aura", ctx -> auraFlux(ctx, false)))))
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("amount", FloatArgumentType.floatArg(0.0F))
                                                .executes(guarded("aura", ctx -> auraFlux(ctx, true)))))))
                .then(admin("taint")
                        .then(Commands.literal("seed")
                                .executes(bySource(
                                        "taint",
                                        source -> spawnEntity(source, "taint_seed", TTEntities.TAINT_SEED.get()))))
                        .then(Commands.literal("spread").executes(bySource("taint", TTCommands::taintSpread))))
                .then(admin("tree")
                        .then(Commands.literal("greatwood")
                                .executes(bySource(
                                        "tree", source -> placeFeature(source, TTConfiguredFeatures.GREATWOOD_TREE))))
                        .then(Commands.literal("silverwood")
                                .executes(bySource(
                                        "tree", source -> placeFeature(source, TTConfiguredFeatures.SILVERWOOD_TREE))))
                        .then(Commands.literal("magic")
                                .executes(bySource(
                                        "tree", source -> placeFeature(source, TTConfiguredFeatures.BIG_MAGIC_TREE)))))
                .then(admin("research")
                        .then(Commands.literal("grant")
                                .then(Commands.argument("entry", ResourceKeyArgument.key(IResearchEntry.REGISTRY_KEY))
                                        .executes(guarded("research", TTCommands::grantGate))))
                        .then(Commands.literal("revoke")
                                .then(Commands.argument("entry", ResourceKeyArgument.key(IResearchEntry.REGISTRY_KEY))
                                        .executes(guarded("research", TTCommands::revokeGate))))
                        .then(Commands.literal("reset").executes(bySource("research", TTCommands::resetResearch)))
                        .then(Commands.literal("all").executes(bySource("research", TTCommands::grantAllResearch))))
                .then(admin("aspect")
                        .then(Commands.literal("all")
                                .executes(bySource("aspect", source -> grantAllAspects(source, AspectPools.SOFT_CAP)))
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1, MAX_ASPECT_GRANT))
                                        .executes(guarded(
                                                "aspect",
                                                ctx -> grantAllAspects(
                                                        ctx.getSource(),
                                                        IntegerArgumentType.getInteger(ctx, "amount"))))))
                        .then(Commands.argument("aspect", ResourceKeyArgument.key(IAspect.REGISTRY_KEY))
                                .executes(guarded("aspect", ctx -> grantAspect(ctx, AspectPools.SOFT_CAP)))
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1, MAX_ASPECT_GRANT))
                                        .executes(guarded(
                                                "aspect",
                                                ctx -> grantAspect(
                                                        ctx, IntegerArgumentType.getInteger(ctx, "amount")))))));
        event.getDispatcher().register(root);
    }

    private static int give(CommandSourceStack source, ItemStack stack) throws CommandSyntaxException {
        source.getPlayerOrException().getInventory().add(stack);
        return Command.SINGLE_SUCCESS;
    }

    private static int resetResearch(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        PlayerKnowledge knowledge = (PlayerKnowledge) KnowledgeAccess.of(player);
        int cleared = knowledge.researchList().size();
        knowledge.clear();
        ResearchManager.applyAutoUnlock(player);
        knowledge.sync(player);
        success(source, "research.reset", cleared);
        return Command.SINGLE_SUCCESS;
    }

    private static int grantAllResearch(CommandSourceStack source) throws CommandSyntaxException {
        int granted = ResearchGrants.grantAll(source.getPlayerOrException());
        success(source, "research.all", granted);
        return Command.SINGLE_SUCCESS;
    }

    private static int grantAllAspects(CommandSourceStack source, int amount) throws CommandSyntaxException {
        int count = AspectPools.grantAllForCommand(source.getPlayerOrException(), amount);
        success(source, "aspect.all", amount, count);
        return Command.SINGLE_SUCCESS;
    }

    private static int grantAspect(CommandContext<CommandSourceStack> ctx, int amount) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ResourceKey<IAspect> key = registryKey(ctx, "aspect", IAspect.REGISTRY_KEY, ERROR_INVALID_ASPECT);
        Holder<IAspect> aspect =
                player.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).getOrThrow(key);
        AspectPools.grantForCommand(player, aspect, amount);
        success(ctx.getSource(), "aspect.one", amount, key.location().toString());
        return Command.SINGLE_SUCCESS;
    }

    private static int revokeGate(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ResourceKey<IResearchEntry> key = registryKey(ctx, "entry", IResearchEntry.REGISTRY_KEY, ERROR_INVALID_GATE);
        PlayerKnowledge knowledge = (PlayerKnowledge) KnowledgeAccess.of(player);
        String id = key.location().toString();
        if (!knowledge.isResearchKnown(key.location())) {
            success(ctx.getSource(), "research.not_known", id);
            return Command.SINGLE_SUCCESS;
        }
        if (!knowledge.removeResearch(key.location())) {
            return failure(ctx.getSource(), "research.revoke_failed", id);
        }
        knowledge.sync(player);
        success(ctx.getSource(), "research.revoked", id);
        return Command.SINGLE_SUCCESS;
    }

    private static int grantGate(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ResourceKey<IResearchEntry> key = registryKey(ctx, "entry", IResearchEntry.REGISTRY_KEY, ERROR_INVALID_GATE);
        Holder<IResearchEntry> holder = player.registryAccess()
                .lookupOrThrow(IResearchEntry.REGISTRY_KEY)
                .getOrThrow(key);
        String id = key.location().toString();
        if (KnowledgeAccess.of(player).isResearchComplete(key.location())) {
            success(ctx.getSource(), "research.already_complete", id);
            return Command.SINGLE_SUCCESS;
        }
        if (!ResearchManager.complete(player, key.location())) {
            return failure(ctx.getSource(), "research.grant_failed", id);
        }
        ResearchManager.setStage(player, key.location(), holder.value().stages().size());
        success(ctx.getSource(), "research.granted", id);
        return Command.SINGLE_SUCCESS;
    }

    private static int warpInfo(CommandSourceStack source) throws CommandSyntaxException {
        IPlayerWarp warp = WarpHelper.getWarp(source.getPlayerOrException());
        success(
                source,
                "warp.info",
                warp.get(WarpType.PERMANENT),
                warp.get(WarpType.NORMAL),
                warp.get(WarpType.TEMPORARY),
                warp.getCounter());
        return Command.SINGLE_SUCCESS;
    }

    private static int warpModify(CommandContext<CommandSourceStack> ctx, boolean remove)
            throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        String typeName = StringArgumentType.getString(ctx, "type");
        WarpType type = Arrays.stream(WarpType.values())
                .filter(candidate -> candidate.name().equalsIgnoreCase(typeName))
                .findFirst()
                .orElse(null);
        if (type == null) {
            return failure(ctx.getSource(), "warp.unknown_type", typeName);
        }
        int amount = IntegerArgumentType.getInteger(ctx, "amount");
        WarpHelper.addWarp(player, remove ? -amount : amount, type);
        success(
                ctx.getSource(),
                remove ? "warp.removed" : "warp.added",
                amount,
                type.name().toLowerCase(Locale.ROOT));
        return Command.SINGLE_SUCCESS;
    }

    private static int warpClear(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        WarpHelper.getWarp(player).clear();
        player.syncData(TTAttachments.WARP);
        success(source, "warp.cleared");
        return Command.SINGLE_SUCCESS;
    }

    private static int warpEvent(CommandSourceStack source) throws CommandSyntaxException {
        WarpEvents.checkWarpEvent(source.getPlayerOrException());
        success(source, "warp.event");
        return Command.SINGLE_SUCCESS;
    }

    private static int auraInfo(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = (ServerLevel) player.level();
        BlockPos pos = player.blockPosition();
        success(
                source,
                "aura.info",
                pos.toShortString(),
                oneDecimal(AuraHelper.getVis(level, pos)),
                oneDecimal(AuraHelper.getFlux(level, pos)),
                AuraHelper.getAuraBase(level, pos));
        return Command.SINGLE_SUCCESS;
    }

    private static int auraVis(CommandContext<CommandSourceStack> ctx, boolean remove) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ServerLevel level = (ServerLevel) player.level();
        BlockPos pos = player.blockPosition();
        float amount = FloatArgumentType.getFloat(ctx, "amount");
        if (remove) {
            success(ctx.getSource(), "aura.vis_drained", oneDecimal(AuraHelper.drainVis(level, pos, amount, false)));
        } else {
            AuraHelper.addVis(level, pos, amount);
            success(ctx.getSource(), "aura.vis_added", oneDecimal(amount));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int auraFlux(CommandContext<CommandSourceStack> ctx, boolean remove) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ServerLevel level = (ServerLevel) player.level();
        BlockPos pos = player.blockPosition();
        float amount = FloatArgumentType.getFloat(ctx, "amount");
        if (remove) {
            success(ctx.getSource(), "aura.flux_drained", oneDecimal(AuraHelper.drainFlux(level, pos, amount, false)));
        } else {
            AuraHelper.addFlux(level, pos, amount);
            success(ctx.getSource(), "aura.flux_added", oneDecimal(amount));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int placeFeature(CommandSourceStack source, ResourceKey<ConfiguredFeature<?, ?>> key)
            throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = (ServerLevel) player.level();
        Holder<ConfiguredFeature<?, ?>> holder = level.registryAccess()
                .lookupOrThrow(Registries.CONFIGURED_FEATURE)
                .getOrThrow(key);
        if (!holder.value()
                .place(level, level.getChunkSource().getGenerator(), level.getRandom(), player.blockPosition())) {
            return failure(source, "feature.refused");
        }
        success(source, "feature.placed", key.location().toString());
        return Command.SINGLE_SUCCESS;
    }

    private static int taintSpread(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        BlockPos pos = player.blockPosition();
        TaintApi.spreadFibres((ServerLevel) player.level(), pos, true);
        success(source, "taint.spread", pos.toShortString());
        return Command.SINGLE_SUCCESS;
    }

    private static int placeFlux(CommandContext<CommandSourceStack> ctx, boolean gas) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        int amount = IntegerArgumentType.getInteger(ctx, "level");
        player.level()
                .setBlock(
                        player.blockPosition(),
                        gas ? BlockFluxGas.gasBlockState(amount) : FluxGooFluid.gooBlockState(amount),
                        Block.UPDATE_ALL);
        success(ctx.getSource(), gas ? "flux.gas" : "flux.goo", amount);
        return Command.SINGLE_SUCCESS;
    }

    private static int triggerFluxEvent(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        String name = StringArgumentType.getString(ctx, "type");
        FluxPressureEvent event = FluxPressureEventTypes.byName(name);
        if (event == null) {
            return failure(ctx.getSource(), "flux_event.unknown", name);
        }
        if (!FluxPressureEvents.trigger(player.serverLevel(), player.blockPosition(), event)) {
            return failure(ctx.getSource(), "flux_event.blocked", name, event.cost());
        }
        success(ctx.getSource(), "flux_event.triggered", name, event.cost());
        return Command.SINGLE_SUCCESS;
    }

    private static int giveEffect(CommandSourceStack source, Holder<MobEffect> effect) throws CommandSyntaxException {
        source.getPlayerOrException()
                .addEffect(new MobEffectInstance(effect, EFFECT_DURATION_TICKS, 0, true, true, true));
        return Command.SINGLE_SUCCESS;
    }

    private static int traceStreamPath(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        Vec3 from = Vec3Argument.getVec3(ctx, "from");
        Vec3 to = Vec3Argument.getVec3(ctx, "to");
        StreamPathfinder.Result result = StreamPathfinder.explore(level, from, to);
        if (result.directSight()) {
            success(ctx.getSource(), "streampath.direct");
            return Command.SINGLE_SUCCESS;
        }
        List<Vec3> waypoints = result.waypoints();
        if (waypoints == null) {
            failure(ctx.getSource(), "streampath.none", result.expanded());
            return 0;
        }
        int blocked = -1;
        Vec3 cursor = from;
        for (int i = 0; i <= waypoints.size() && blocked < 0; i++) {
            Vec3 next = i < waypoints.size() ? waypoints.get(i) : to;
            if (!StreamPathfinder.hasLineOfSight(level, cursor, next)) {
                blocked = i;
            }
            cursor = next;
        }
        String points = waypoints.stream()
                .map(wp -> String.format(Locale.ROOT, "(%.1f, %.1f, %.1f)", wp.x, wp.y, wp.z))
                .collect(Collectors.joining(" "));
        if (blocked >= 0) {
            success(ctx.getSource(), "streampath.route_blocked", waypoints.size(), result.expanded(), blocked, points);
        } else {
            success(ctx.getSource(), "streampath.route", waypoints.size(), result.expanded(), points);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int spawnRift(CommandSourceStack source, int size) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = (ServerLevel) player.level();
        EntityFluxRift rift = TTEntities.FLUX_RIFT.get().create(level);
        if (rift == null) {
            return failure(source, "spawn_failed", TTEntities.FLUX_RIFT.get().getDescription());
        }
        Vec3 pos = player.getEyePosition().add(player.getLookAngle().scale(RIFT_SPAWN_DISTANCE));
        rift.setRiftSeed(level.getRandom().nextInt());
        rift.moveTo(pos.x, pos.y, pos.z, level.getRandom().nextFloat() * 360.0F, 0.0F);
        rift.setRiftSize(size);
        level.addFreshEntity(rift);
        success(source, "rift", size);
        return Command.SINGLE_SUCCESS;
    }

    private static int spawnChampion(CommandContext<CommandSourceStack> ctx, @Nullable Holder<EntityType<?>> entityType)
            throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ServerLevel level = (ServerLevel) player.level();
        String modName = StringArgumentType.getString(ctx, "modifier").toLowerCase(Locale.ROOT);
        List<Holder<MobTrait>> champions = ChampionHelper.championTraits();
        Holder<MobTrait> trait = RANDOM.equals(modName) && !champions.isEmpty()
                ? champions.get(player.getRandom().nextInt(champions.size()))
                : champions.stream()
                        .filter(candidate -> candidate
                                .unwrapKey()
                                .orElseThrow()
                                .location()
                                .getPath()
                                .equals(modName))
                        .findFirst()
                        .orElse(null);
        if (trait == null) {
            return failure(ctx.getSource(), "champion.unknown", modName);
        }
        EntityType<?> toSpawn = entityType == null ? EntityType.ZOMBIE : entityType.value();
        Entity entity = toSpawn.create(level);
        if (!(entity instanceof Mob mob)) {
            if (entity != null) {
                entity.discard();
            }
            return failure(ctx.getSource(), "champion.not_mob", toSpawn.getDescription());
        }
        Vec3 pos = player.position()
                .add(player.getLookAngle().multiply(1.0, 0.0, 1.0).normalize().scale(CHAMPION_SPAWN_DISTANCE));
        mob.moveTo(pos.x, pos.y, pos.z, player.getYRot() + 180.0F, 0.0F);
        ChampionHelper.makeChampion(mob, true, trait);
        level.addFreshEntity(mob);
        success(ctx.getSource(), "champion.spawned", modName, mob.getName());
        return Command.SINGLE_SUCCESS;
    }

    private static int summonTainted(CommandContext<CommandSourceStack> ctx, Vec3 pos) throws CommandSyntaxException {
        Entity entity = SummonCommand.createEntity(
                ctx.getSource(), ResourceArgument.getSummonableEntityType(ctx, "entity"), pos, new CompoundTag(), true);
        if (!(entity instanceof LivingEntity living)) {
            entity.discard();
            throw ERROR_NOT_LIVING.create(EntityType.getKey(entity.getType()));
        }
        MobTraits.add(living, TTMobTraits.TAINTED);
        ctx.getSource().sendSuccess(() -> Component.translatable(KEY + "tainted", living.getName()), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int changeTrait(CommandContext<CommandSourceStack> ctx, boolean add) throws CommandSyntaxException {
        ResourceKey<MobTrait> key = registryKey(ctx, "trait", MobTrait.REGISTRY_KEY, ERROR_INVALID_TRAIT);
        Holder<MobTrait> trait =
                TTMobTraits.registry().getHolder(key).orElseThrow(() -> ERROR_INVALID_TRAIT.create(key.location()));
        int changed = 0;
        for (Entity entity : EntityArgument.getEntities(ctx, "targets")) {
            if (entity instanceof LivingEntity living
                    && (add ? MobTraits.add(living, trait) : MobTraits.remove(living, trait))) {
                changed++;
            }
        }
        int count = changed;
        ctx.getSource()
                .sendSuccess(
                        () -> Component.translatable(
                                KEY + (add ? "trait.added" : "trait.removed"),
                                key.location().toString(),
                                count),
                        true);
        return changed;
    }

    private static int listTraits(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Entity entity = EntityArgument.getEntity(ctx, "target");
        List<Holder<MobTrait>> traits = entity instanceof LivingEntity living ? MobTraits.traits(living) : List.of();
        if (traits.isEmpty()) {
            success(ctx.getSource(), "trait.none", entity.getName());
        } else {
            success(
                    ctx.getSource(),
                    "trait.list",
                    entity.getName(),
                    traits.stream()
                            .map(trait ->
                                    trait.unwrapKey().orElseThrow().location().toString())
                            .collect(Collectors.joining(", ")));
        }
        return traits.size();
    }

    private static int spawnEntity(CommandSourceStack source, String name, EntityType<?> type)
            throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = (ServerLevel) player.level();
        Entity entity = type.create(level);
        if (entity == null) {
            return failure(source, "spawn_failed", type.getDescription());
        }
        entity.setPos(player.getX(), player.getY(), player.getZ());
        if (entity instanceof ThaumicSlime slime) {
            slime.setSize(SPAWNED_SLIME_SIZE, true);
        }
        level.addFreshEntity(entity);
        success(source, "spawned", type.getDescription());
        return Command.SINGLE_SUCCESS;
    }

    private static int giveFocus(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        int tier = IntegerArgumentType.getInteger(ctx, "tier");
        Spell spell = SpellPartArguments.chain(
                StringArgumentType.getString(ctx, "parts"), ctx.getSource().registryAccess());
        Item focusItem =
                switch (tier) {
                    case 1 -> TTItems.FOCUS_1.get();
                    case 2 -> TTItems.FOCUS_2.get();
                    default -> TTItems.FOCUS_3.get();
                };
        ItemStack focusStack = new ItemStack(focusItem);
        Spells.setSpell(focusStack, spell);
        SpellSummary summary = Spells.analyze(
                spell, Spells.tierOf(focusStack).orElse(null), ctx.getSource().registryAccess(), null);
        for (SpellProblem problem : summary.problems()) {
            ctx.getSource().sendSuccess(problem::message, false);
        }
        ItemStack held = player.getMainHandItem();
        if (held.getItem() instanceof ICaster caster) {
            ItemStack previous = caster.getFocusStack(held);
            if (!previous.isEmpty()) {
                player.getInventory().add(previous);
            }
            caster.setFocus(held, focusStack);
            success(ctx.getSource(), "focus.socketed", summary.complexity(), summary.budget());
        } else {
            player.getInventory().add(focusStack);
            success(ctx.getSource(), "focus.given", summary.complexity(), summary.budget());
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int giveCrystal(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        String tag = StringArgumentType.getString(ctx, "aspect");
        ResourceLocation id = ResourceLocation.tryBuild(TTIds.MODID, tag);
        ResourceKey<IAspect> key = id == null ? null : ResourceKey.create(IAspect.REGISTRY_KEY, id);
        if (key == null
                || player.registryAccess()
                        .lookupOrThrow(IAspect.REGISTRY_KEY)
                        .get(key)
                        .isEmpty()) {
            throw ERROR_INVALID_ASPECT.create(tag);
        }
        player.getInventory().add(EssentiaCrystalFactory.of(player.registryAccess(), key));
        success(ctx.getSource(), "crystal", tag);
        return Command.SINGLE_SUCCESS;
    }

    private static int listParticles(CommandSourceStack source) {
        source.sendSuccess(() -> Component.translatable(KEY + "particle.header").withStyle(ChatFormatting.GOLD), false);
        source.sendSuccess(
                () -> Component.translatable(KEY + "particle.usage", TTCommandRoot.NAME)
                        .withStyle(ChatFormatting.GRAY),
                false);
        for (Map.Entry<String, ParticleDemos.Demo> entry : ParticleDemos.DEMOS.entrySet()) {
            source.sendSuccess(
                    () -> Component.translatable(
                            KEY + "particle.entry",
                            Component.literal(entry.getKey()).withStyle(ChatFormatting.YELLOW),
                            entry.getValue().description()),
                    false);
        }
        source.sendSuccess(
                () -> Component.translatable(KEY + "particle.total", ParticleDemos.DEMOS.size())
                        .withStyle(ChatFormatting.GOLD),
                false);
        return Command.SINGLE_SUCCESS;
    }

    private static int runParticle(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        String name = StringArgumentType.getString(ctx, "name");
        if (!ParticleDemos.DEMOS.containsKey(name)) {
            return failure(ctx.getSource(), "particle.unknown", name, TTCommandRoot.NAME);
        }
        ParticleDemos.run(player, name);
        ctx.getSource()
                .sendSuccess(
                        () -> Component.translatable(KEY + "particle.spawned", name)
                                .withStyle(ChatFormatting.GREEN),
                        false);
        return Command.SINGLE_SUCCESS;
    }

    private static int spawnNode(
            CommandContext<CommandSourceStack> ctx, String typeName, String modifierName, String aspectSpec) {
        ServerLevel level = ctx.getSource().getLevel();
        BlockPos pos = BlockPos.containing(ctx.getSource().getPosition()).above(NODE_HEIGHT_ABOVE_SOURCE);
        if (RANDOM.equals(typeName)) {
            boolean placed = NodeGenerator.createRandomNodeAt(
                    level,
                    pos,
                    level.getRandom(),
                    false,
                    false,
                    false,
                    NodeGenerator.DEFAULT_SPECIAL_RARITY,
                    NodeGenerator.DEFAULT_BASE_AURA);
            return nodeResult(ctx.getSource(), placed);
        }
        NodeType type = Arrays.stream(NodeType.values())
                .filter(candidate -> candidate.getSerializedName().equals(typeName))
                .findFirst()
                .orElse(null);
        if (type == null) {
            return failure(ctx.getSource(), "node.unknown_type", typeName);
        }
        NodeModifier modifier = Arrays.stream(NodeModifier.values())
                .filter(candidate -> candidate.getSerializedName().equals(modifierName))
                .findFirst()
                .orElse(null);
        HolderLookup.RegistryLookup<IAspect> aspects = level.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY);
        AspectList list = aspectSpec.isBlank()
                ? defaultNodeAspects(aspects)
                : parseNodeAspects(ctx.getSource(), aspects, aspectSpec);
        if (list == null) {
            return 0;
        }
        return nodeResult(ctx.getSource(), NodeGenerator.createNodeAt(level, pos, type, modifier, list));
    }

    private static int nodeResult(CommandSourceStack source, boolean placed) {
        source.sendSuccess(() -> Component.translatable(KEY + (placed ? "node.created" : "node.failed")), true);
        return placed ? Command.SINGLE_SUCCESS : 0;
    }

    private static AspectList defaultNodeAspects(HolderLookup.RegistryLookup<IAspect> aspects) {
        return AspectList.EMPTY
                .add(aspects.getOrThrow(TTAspects.AER), DEFAULT_NODE_MAIN_PRIMAL)
                .add(aspects.getOrThrow(TTAspects.IGNIS), DEFAULT_NODE_MAIN_PRIMAL)
                .add(aspects.getOrThrow(TTAspects.AQUA), DEFAULT_NODE_MAIN_PRIMAL)
                .add(aspects.getOrThrow(TTAspects.TERRA), DEFAULT_NODE_MAIN_PRIMAL)
                .add(aspects.getOrThrow(TTAspects.ORDO), DEFAULT_NODE_MINOR_PRIMAL)
                .add(aspects.getOrThrow(TTAspects.PERDITIO), DEFAULT_NODE_MINOR_PRIMAL);
    }

    private static @Nullable AspectList parseNodeAspects(
            CommandSourceStack source, HolderLookup.RegistryLookup<IAspect> aspects, String spec) {
        String[] tokens = spec.trim().split("\\s+");
        if (tokens.length % 2 != 0) {
            failure(source, "node.pairs");
            return null;
        }
        AspectList list = AspectList.EMPTY;
        for (int i = 0; i < tokens.length; i += 2) {
            ResourceLocation id = tokens[i].contains(":")
                    ? ResourceLocation.tryParse(tokens[i])
                    : ResourceLocation.tryBuild(TTIds.MODID, tokens[i]);
            Holder<IAspect> holder = id == null
                    ? null
                    : aspects.get(ResourceKey.create(IAspect.REGISTRY_KEY, id)).orElse(null);
            if (holder == null) {
                failure(source, "unknown_aspect", tokens[i]);
                return null;
            }
            int amount;
            try {
                amount = Integer.parseInt(tokens[i + 1]);
            } catch (NumberFormatException e) {
                failure(source, "node.bad_amount", tokens[i + 1]);
                return null;
            }
            if (amount < 1) {
                failure(source, "node.bad_amount", tokens[i + 1]);
                return null;
            }
            list = list.add(holder, amount);
        }
        return list;
    }

    private static int shareUnlink(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        int removed = ResearchLinkData.get(player.level().getServer()).unlinkAll(player.getUUID());
        success(source, "link.removed", removed);
        return Command.SINGLE_SUCCESS;
    }

    private static <T> ResourceKey<T> registryKey(
            CommandContext<CommandSourceStack> context,
            String name,
            ResourceKey<Registry<T>> registry,
            DynamicCommandExceptionType unknown)
            throws CommandSyntaxException {
        ResourceKey<?> raw = context.getArgument(name, ResourceKey.class);
        return raw.cast(registry).orElseThrow(() -> unknown.create(raw.location()));
    }
}
