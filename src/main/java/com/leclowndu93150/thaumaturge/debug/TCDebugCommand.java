package com.leclowndu93150.thaumaturge.debug;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.debug.network.ClientboundToggleRaycastDebugPayload;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
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
        LiteralArgumentBuilder<CommandSourceStack> tc = Commands.literal("tc_debug").then(Commands.literal("raycast").executes(TCDebugCommand::toggleRaycast));
        event.getDispatcher().register(tc);
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
