package com.leclowndu93150.thaumaturge.content.golem.accessory;

import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessories;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessory;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessoryBehavior;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessoryStateView;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.connection.ConnectionType;

public record GolemAccessoryStates(List<AccessoryStateSlot<?>> slots) implements GolemAccessoryStateView {
    public static final GolemAccessoryStates EMPTY = new GolemAccessoryStates(List.of());
    public static final StreamCodec<RegistryFriendlyByteBuf, GolemAccessoryStates> STREAM_CODEC =
            StreamCodec.of(GolemAccessoryStates::encode, GolemAccessoryStates::decode);

    public GolemAccessoryStates {
        slots = List.copyOf(slots);
    }

    @Override
    public <S> Optional<S> state(GolemAccessoryBehavior<S> behavior) {
        for (AccessoryStateSlot<?> slot : slots) {
            Optional<S> state = slot.stateFor(behavior);
            if (state.isPresent()) {
                return state;
            }
        }
        return Optional.empty();
    }

    public int encodedSize(RegistryAccess registries) {
        RegistryFriendlyByteBuf buf =
                new RegistryFriendlyByteBuf(Unpooled.buffer(), registries, ConnectionType.NEOFORGE);
        try {
            encode(buf, this);
            return buf.readableBytes();
        } finally {
            buf.release();
        }
    }

    private static void encode(RegistryFriendlyByteBuf buf, GolemAccessoryStates states) {
        buf.writeVarInt(states.slots.size());
        for (AccessoryStateSlot<?> slot : states.slots) {
            ResourceLocation.STREAM_CODEC.encode(buf, slot.accessory().id());
            slot.encode(buf);
        }
    }

    private static GolemAccessoryStates decode(RegistryFriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<AccessoryStateSlot<?>> slots = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            ResourceLocation id = ResourceLocation.STREAM_CODEC.decode(buf);
            GolemAccessory accessory = GolemAccessories.get(id);
            if (accessory == null || accessory.behavior().isEmpty()) {
                throw new DecoderException("Golem accessory " + id + " has no behaviour to decode its state");
            }
            slots.add(AccessoryStateSlot.decode(accessory, accessory.behavior().get(), buf));
        }
        return new GolemAccessoryStates(slots);
    }
}
