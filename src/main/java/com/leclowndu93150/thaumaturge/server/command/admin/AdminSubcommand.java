package com.leclowndu93150.thaumaturge.server.command.admin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;

interface AdminSubcommand {
    LiteralArgumentBuilder<CommandSourceStack> build(CommandBuildContext context);
}
