package com.leclowndu93150.thaumaturge.debug;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import com.leclowndu93150.thaumaturge.debug.network.ClientboundToggleRaycastDebugPayload;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = TCIds.MODID)
public final class TCDebugCommand {

    private TCDebugCommand() {}

    @SubscribeEvent
    public static void onRegister(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> tc = Commands.literal("tc_debug")
                .then(Commands.literal("raycast").executes(TCDebugCommand::toggleRaycast))
                .then(Commands.literal("taint_ecology")
                        .then(Commands.literal("get").executes(TCDebugCommand::getTaintEcology))
                        .then(Commands.literal("set")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("saturation", FloatArgumentType.floatArg(0.0F, 1.0F))
                                        .executes(TCDebugCommand::setTaintEcology)))
                        .then(Commands.literal("clean")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("amount", FloatArgumentType.floatArg(0.0F, 1.0F))
                                        .executes(TCDebugCommand::cleanTaintEcology))))
                .then(Commands.literal("taint_biome")
                        .then(Commands.literal("get").executes(TCDebugCommand::getTaintBiome))
                        .then(Commands.literal("taint")
                                .requires(source -> source.hasPermission(2))
                                .executes(TCDebugCommand::taintBiome))
                        .then(Commands.literal("reset")
                                .requires(source -> source.hasPermission(2))
                                .executes(TCDebugCommand::resetTaintBiome)));
        event.getDispatcher().register(tc);
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
            PacketDistributor.sendToPlayer(player, ClientboundToggleRaycastDebugPayload.INSTANCE);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }
}
