package com.leclowndu93150.thaumaturge.debug;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellContinuation;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellState;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellStats;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import com.leclowndu93150.thaumaturge.debug.network.ClientboundToggleRaycastDebugPayload;
import com.leclowndu93150.thaumaturge.server.command.SpellPartArguments;
import com.leclowndu93150.thaumaturge.server.command.TTCommandRoot;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = TTIds.MODID)
public final class TTDebugCommand {
    private static final int MAX_CASTS = 10000;
    private static final float CAST_POWER = 1.0F;

    private TTDebugCommand() {}

    @SubscribeEvent
    public static void onRegister(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> debug = Commands.literal("debug")
                .then(Commands.literal("raycast").executes(TTDebugCommand::toggleRaycast))
                .then(Commands.literal("cast")
                        .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .then(Commands.argument("count", IntegerArgumentType.integer(1, MAX_CASTS))
                                        .then(Commands.argument("parts", StringArgumentType.greedyString())
                                                .suggests(SpellPartArguments.SUGGESTIONS)
                                                .executes(TTDebugCommand::cast)))))
                .then(Commands.literal("taint_ecology")
                        .then(Commands.literal("get").executes(TTDebugCommand::getTaintEcology))
                        .then(Commands.literal("set")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("saturation", FloatArgumentType.floatArg(0.0F, 1.0F))
                                        .executes(TTDebugCommand::setTaintEcology)))
                        .then(Commands.literal("clean")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("amount", FloatArgumentType.floatArg(0.0F, 1.0F))
                                        .executes(TTDebugCommand::cleanTaintEcology))))
                .then(Commands.literal("taint_biome")
                        .then(Commands.literal("get").executes(TTDebugCommand::getTaintBiome))
                        .then(Commands.literal("taint")
                                .requires(source -> source.hasPermission(2))
                                .executes(TTDebugCommand::taintBiome))
                        .then(Commands.literal("reset")
                                .requires(source -> source.hasPermission(2))
                                .executes(TTDebugCommand::resetTaintBiome)));
        event.getDispatcher().register(TTCommandRoot.root().then(debug));
    }

    private static int getTaintEcology(CommandContext<CommandSourceStack> ctx) {
        float saturation = TaintEcology.getSaturation(
                ctx.getSource().getLevel(), BlockPos.containing(ctx.getSource().getPosition()));
        ctx.getSource()
                .sendSuccess(() -> Component.literal("Taint ecology: " + String.format("%.3f", saturation)), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int setTaintEcology(CommandContext<CommandSourceStack> ctx) {
        float saturation = FloatArgumentType.getFloat(ctx, "saturation");
        TaintEcology.setSaturation(
                ctx.getSource().getLevel(), BlockPos.containing(ctx.getSource().getPosition()), saturation);
        return getTaintEcology(ctx);
    }

    private static int cleanTaintEcology(CommandContext<CommandSourceStack> ctx) {
        float amount = FloatArgumentType.getFloat(ctx, "amount");
        TaintEcology.clean(
                ctx.getSource().getLevel(), BlockPos.containing(ctx.getSource().getPosition()), amount);
        return getTaintEcology(ctx);
    }

    private static int getTaintBiome(CommandContext<CommandSourceStack> ctx) {
        BlockPos pos = BlockPos.containing(ctx.getSource().getPosition());
        boolean tainted = TaintBiomeManager.isTainted(ctx.getSource().getLevel(), pos);
        String biome = ctx.getSource()
                .getLevel()
                .getBiome(pos)
                .unwrapKey()
                .map(key -> key.location().toString())
                .orElse("unregistered");
        ctx.getSource()
                .sendSuccess(() -> Component.literal("Biome: " + biome + " (Tainted Lands: " + tainted + ")"), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int taintBiome(CommandContext<CommandSourceStack> ctx) {
        BlockPos pos = BlockPos.containing(ctx.getSource().getPosition());
        TaintBiomeManager.taintColumn(ctx.getSource().getLevel(), pos);
        return getTaintBiome(ctx);
    }

    private static int resetTaintBiome(CommandContext<CommandSourceStack> ctx) {
        BlockPos pos = BlockPos.containing(ctx.getSource().getPosition());
        TaintBiomeManager.restoreColumn(ctx.getSource().getLevel(), pos);
        return getTaintBiome(ctx);
    }

    private static int toggleRaycast(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            TTDebugEvents.toggleRaycastDebug(player);
            PacketDistributor.sendToPlayer(player, ClientboundToggleRaycastDebugPayload.INSTANCE);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int cast(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerLevel level = ctx.getSource().getLevel();
            BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
            int count = IntegerArgumentType.getInteger(ctx, "count");
            Spell spell = SpellPartArguments.chain(
                    StringArgumentType.getString(ctx, "parts"), ctx.getSource().registryAccess());
            Optional<UUID> caster = ctx.getSource().getEntity() instanceof LivingEntity living
                    ? Optional.of(living.getUUID())
                    : Optional.empty();
            SpellTarget target = new SpellTarget(
                    new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false),
                    Vec3.atCenterOf(pos).add(0.0, 0.5, 0.0),
                    new Vec3(0.0, -1.0, 0.0));
            for (int i = 0; i < count; i++) {
                SpellContinuation continuation = new SpellContinuation(
                        spell.root().children(),
                        SpellState.DEFAULT.with(SpellStats.POWER, CAST_POWER),
                        caster,
                        UUID.randomUUID());
                Spells.resume(level, continuation, List.of(target));
            }
            ctx.getSource()
                    .sendSuccess(
                            () -> Component.literal("Cast " + StringArgumentType.getString(ctx, "parts") + " " + count
                                    + " times at " + pos.toShortString()),
                            true);
            return count;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }
}
