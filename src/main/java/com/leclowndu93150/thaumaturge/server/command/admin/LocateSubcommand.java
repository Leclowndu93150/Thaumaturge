package com.leclowndu93150.thaumaturge.server.command.admin;

import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeLocationIndex;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerLevel;

final class LocateSubcommand implements AdminSubcommand {
    private static final DynamicCommandExceptionType INVALID_TYPE = new DynamicCommandExceptionType(
            type -> Component.translatable("commands.thaumaturge.locate.node.invalid_type", type));

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build(CommandBuildContext context) {
        return Commands.literal("locate")
                .then(Commands.literal("node")
                        .then(Commands.argument("type", StringArgumentType.word())
                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                        Arrays.stream(NodeType.values()).map(NodeType::getSerializedName), builder))
                                .executes(LocateSubcommand::locateNode)));
    }

    private static int locateNode(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        String name = StringArgumentType.getString(context, "type");
        NodeType type = Arrays.stream(NodeType.values())
                .filter(candidate -> candidate.getSerializedName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> INVALID_TYPE.create(name));
        ServerLevel level = source.getLevel();
        BlockPos origin = BlockPos.containing(source.getPosition());
        NodeLocationIndex index = NodeLocationIndex.get(level);
        Optional<BlockPos> result;
        while ((result = index.findNearest(origin, type)).isPresent()) {
            BlockPos candidate = result.get();
            if (!level.hasChunk(candidate.getX() >> 4, candidate.getZ() >> 4)) {
                break;
            }
            if ((level.getBlockState(candidate).is(TCBlocks.NODE.get())
                            || level.getBlockState(candidate).is(TCBlocks.SILVERWOOD_NODE_LOG.get()))
                    && level.getBlockEntity(candidate) instanceof BlockEntityNode node) {
                if (node.getNodeType() == type) {
                    break;
                }
                index.register(candidate, node.getNodeType());
            } else {
                index.remove(candidate);
            }
        }
        if (result.isEmpty()) {
            source.sendFailure(
                    Component.translatable("commands.thaumaturge.locate.node.not_found", type.getSerializedName()));
            return 0;
        }
        BlockPos pos = result.get();
        String coordinatesText = pos.getX() + " " + pos.getY() + " " + pos.getZ();
        Component coordinates = Component.literal("[" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]")
                .withStyle(style -> style.withColor(ChatFormatting.GREEN)
                        .withUnderlined(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, coordinatesText))
                        .withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                Component.translatable("commands.thaumaturge.locate.node.copy"))));
        int distance = (int) Math.round(Math.sqrt(pos.distSqr(origin)));
        source.sendSuccess(
                () -> Component.translatable(
                        "commands.thaumaturge.locate.node.found", type.getSerializedName(), coordinates, distance),
                false);
        return Command.SINGLE_SUCCESS;
    }
}
