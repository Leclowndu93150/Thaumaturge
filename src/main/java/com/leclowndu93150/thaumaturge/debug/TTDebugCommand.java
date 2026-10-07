package com.leclowndu93150.thaumaturge.debug;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.casters.CastStreams;
import com.leclowndu93150.thaumaturge.api.casters.FocusEngine;
import com.leclowndu93150.thaumaturge.api.casters.FocusPackage;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import com.leclowndu93150.thaumaturge.debug.network.ClientboundToggleRaycastDebugPayload;
import com.leclowndu93150.thaumaturge.server.command.FocusElementArguments;
import com.leclowndu93150.thaumaturge.server.command.TTCommandRoot;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
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
        LiteralArgumentBuilder<CommandSourceStack> tc = TTCommandRoot.root()
                .then(Commands.literal("debug").then(Commands.literal("raycast").executes(TTDebugCommand::toggleRaycast))
                        .then(Commands.literal("cast").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .then(Commands.argument("count", IntegerArgumentType.integer(1, MAX_CASTS))
                                                .then(Commands.argument("elements", StringArgumentType.greedyString()).suggests(FocusElementArguments.SUGGESTIONS).executes(TTDebugCommand::cast)))))
                        .then(Commands.literal("taint_ecology").then(Commands.literal("get").executes(TTDebugCommand::getTaintEcology))
                                .then(Commands.literal("set").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                        .then(Commands.argument("saturation", FloatArgumentType.floatArg(0.0F, 1.0F)).executes(TTDebugCommand::setTaintEcology)))
                                .then(Commands.literal("clean").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                        .then(Commands.argument("amount", FloatArgumentType.floatArg(0.0F, 1.0F)).executes(TTDebugCommand::cleanTaintEcology))))
                        .then(Commands.literal("taint_biome").then(Commands.literal("get").executes(TTDebugCommand::getTaintBiome))
                                .then(Commands.literal("taint").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).executes(TTDebugCommand::taintBiome))
                                .then(Commands.literal("reset").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).executes(TTDebugCommand::resetTaintBiome))));
        event.getDispatcher().register(tc);
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
            List<Identifier> elements = FocusElementArguments.parse(StringArgumentType.getString(ctx, "elements"));
            LivingEntity caster = ctx.getSource().getEntity() instanceof LivingEntity living ? living : null;
            FocusPackage.Builder builder = FocusPackage.builder().power(CAST_POWER).caster(caster == null ? null : caster.getUUID());
            elements.forEach(builder::add);
            FocusPackage pack = builder.build();
            HitResult[] targets = {new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false)};
            for (int i = 0; i < count; i++) {
                FocusEngine.run(level, pack, caster, new CastStreams(null, targets));
            }
            ctx.getSource().sendSuccess(() -> Component.literal("Cast " + elements + " " + count + " times at " + pos.toShortString()), true);
            return count;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int getTaintEcology(CommandContext<CommandSourceStack> ctx) {
        float saturation = TaintEcology.getSaturation(ctx.getSource().getLevel(), BlockPos.containing(ctx.getSource().getPosition()));
        ctx.getSource().sendSuccess(() -> Component.literal("Taint ecology: " + String.format("%.3f", saturation)), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int setTaintEcology(CommandContext<CommandSourceStack> ctx) {
        float saturation = FloatArgumentType.getFloat(ctx, "saturation");
        TaintEcology.setSaturation(ctx.getSource().getLevel(), BlockPos.containing(ctx.getSource().getPosition()), saturation);
        return getTaintEcology(ctx);
    }

    private static int cleanTaintEcology(CommandContext<CommandSourceStack> ctx) {
        float amount = FloatArgumentType.getFloat(ctx, "amount");
        TaintEcology.clean(ctx.getSource().getLevel(), BlockPos.containing(ctx.getSource().getPosition()), amount);
        return getTaintEcology(ctx);
    }

    private static int getTaintBiome(CommandContext<CommandSourceStack> ctx) {
        BlockPos pos = BlockPos.containing(ctx.getSource().getPosition());
        boolean tainted = TaintBiomeManager.isTainted(ctx.getSource().getLevel(), pos);
        String biome = ctx.getSource().getLevel().getBiome(pos).unwrapKey().map(key -> key.identifier().toString()).orElse("unregistered");
        ctx.getSource().sendSuccess(() -> Component.literal("Biome: " + biome + " (Tainted Lands: " + tainted + ")"), false);
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
}
