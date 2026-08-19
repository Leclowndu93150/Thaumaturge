package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public record ClientboundThaumatoriumRecipesPayload(int containerId, List<Entry> entries) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ClientboundThaumatoriumRecipesPayload> TYPE = new CustomPacketPayload.Type<>(TCIds.rl("thaumatorium_recipes"));

    public record Entry(Identifier id, ItemStack output, boolean queued, AspectList aspects) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(Identifier.STREAM_CODEC, Entry::id, ItemStack.STREAM_CODEC, Entry::output,
                ByteBufCodecs.BOOL, Entry::queued, AspectList.STREAM_CODEC, Entry::aspects, Entry::new);
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundThaumatoriumRecipesPayload> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT,
            ClientboundThaumatoriumRecipesPayload::containerId, Entry.STREAM_CODEC.apply(ByteBufCodecs.list()), ClientboundThaumatoriumRecipesPayload::entries,
            ClientboundThaumatoriumRecipesPayload::new);

    @Override
    public CustomPacketPayload.Type<ClientboundThaumatoriumRecipesPayload> type() {
        return TYPE;
    }
}
