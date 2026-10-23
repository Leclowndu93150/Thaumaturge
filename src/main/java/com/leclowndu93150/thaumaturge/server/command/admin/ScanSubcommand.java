package com.leclowndu93150.thaumaturge.server.command.admin;

import com.leclowndu93150.thaumaturge.content.research.scan.ScanEverything;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

final class ScanSubcommand implements AdminSubcommand {
    private static final String TARGETS = "targets";

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build(CommandBuildContext context) {
        return Commands.literal("scan")
                .then(Commands.argument(TARGETS, EntityArgument.players())
                        .then(Commands.literal("everything").executes(ScanSubcommand::everything)));
    }

    private static int everything(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, TARGETS);
        int scanned = CommandSupport.forEach(targets, ScanEverything::scan);
        ctx.getSource()
                .sendSuccess(
                        () -> Component.translatable(
                                "commands.thaumaturge.scan.everything", scanned, CommandSupport.describe(targets)),
                        true);
        return targets.size();
    }
}
