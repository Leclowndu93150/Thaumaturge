package com.leclowndu93150.thaumaturge.server.command.admin;

import com.leclowndu93150.thaumaturge.TCIds;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class ThaumaturgeCommand {

    private ThaumaturgeCommand() {}

    @SubscribeEvent
    public static void onRegister(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(TCIds.MODID).requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));
        List<AdminSubcommand> subcommands = List.of(new ResearchSubcommand(), new KnowledgeSubcommand(), new AspectSubcommand(), PlacementSubcommand.structures(), PlacementSubcommand.features(),
                new ShowcaseSubcommand());
        for (AdminSubcommand subcommand : subcommands) {
            root.then(subcommand.build(event.getBuildContext()));
        }
        event.getDispatcher().register(root);
    }
}
