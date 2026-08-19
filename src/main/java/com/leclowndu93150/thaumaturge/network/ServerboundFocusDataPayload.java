package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.casters.BlockEntityFocalManipulator;
import com.leclowndu93150.thaumaturge.content.casters.FocusElementNode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundFocusDataPayload(BlockPos pos, String name, List<FocusElementNode> nodes) implements CustomPacketPayload {
    private static final double MAX_INTERACT_DISTANCE_SQ = 64.0;
    private static final int MAX_NAME_LENGTH = 50;

    public static final Type<ServerboundFocusDataPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TCIds.MODID, "focus_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundFocusDataPayload> STREAM_CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, ServerboundFocusDataPayload::pos,
            ByteBufCodecs.stringUtf8(MAX_NAME_LENGTH), ServerboundFocusDataPayload::name, ByteBufCodecs.fromCodecWithRegistries(FocusElementNode.CODEC.listOf()), ServerboundFocusDataPayload::nodes,
            ServerboundFocusDataPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerboundFocusDataPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            if (player.distanceToSqr(payload.pos.getCenter()) > MAX_INTERACT_DISTANCE_SQ) {
                return;
            }
            if (player.level().getBlockEntity(payload.pos) instanceof BlockEntityFocalManipulator table) {
                Map<Integer, FocusElementNode> nodes = new LinkedHashMap<>();
                for (FocusElementNode node : payload.nodes) {
                    nodes.put(node.id, node);
                }
                table.acceptClientData(nodes, payload.name);
            }
        });
    }
}
