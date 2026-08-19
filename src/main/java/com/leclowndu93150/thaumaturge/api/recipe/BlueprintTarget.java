package com.leclowndu93150.thaumaturge.api.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public sealed interface BlueprintTarget {
    Codec<BlueprintTarget> CODEC = Kind.CODEC.dispatch("type", BlueprintTarget::kind, k -> k.codec);

    StreamCodec<RegistryFriendlyByteBuf, BlueprintTarget> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public BlueprintTarget decode(RegistryFriendlyByteBuf buf) {
            int ord = buf.readByte();
            Kind kind = Kind.values()[ord];
            return kind.streamCodec.decode(buf);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, BlueprintTarget value) {
            Kind kind = value.kind();
            buf.writeByte(kind.ordinal());
            kind.encode(buf, value);
        }
    };

    Kind kind();

    enum Kind implements StringRepresentable {
        KEEP("keep", Keep.CODEC_INSTANCE, Keep.STREAM_CODEC_INSTANCE), AIR("air", Air.CODEC_INSTANCE, Air.STREAM_CODEC_INSTANCE), BLOCK("block", BlockTarget.CODEC_INSTANCE,
                BlockTarget.STREAM_CODEC_INSTANCE), STACK("stack", StackTarget.CODEC_INSTANCE,
                        StackTarget.STREAM_CODEC_INSTANCE), STATE("state", StateTarget.CODEC_INSTANCE, StateTarget.STREAM_CODEC_INSTANCE);

        public static final Codec<Kind> CODEC = StringRepresentable.fromEnum(Kind::values);

        private final String name;
        final MapCodec<? extends BlueprintTarget> codec;
        final StreamCodec<RegistryFriendlyByteBuf, ? extends BlueprintTarget> streamCodec;

        Kind(String name, MapCodec<? extends BlueprintTarget> codec, StreamCodec<RegistryFriendlyByteBuf, ? extends BlueprintTarget> streamCodec) {
            this.name = name;
            this.codec = codec;
            this.streamCodec = streamCodec;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        void encode(RegistryFriendlyByteBuf buf, BlueprintTarget value) {
            ((StreamCodec) this.streamCodec).encode(buf, value);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    record Keep() implements BlueprintTarget {
        public static final Keep INSTANCE = new Keep();

        static final MapCodec<Keep> CODEC_INSTANCE = MapCodec.unit(INSTANCE);
        static final StreamCodec<RegistryFriendlyByteBuf, Keep> STREAM_CODEC_INSTANCE = StreamCodec.unit(INSTANCE);

        @Override
        public Kind kind() {
            return Kind.KEEP;
        }
    }

    record Air() implements BlueprintTarget {
        public static final Air INSTANCE = new Air();

        static final MapCodec<Air> CODEC_INSTANCE = MapCodec.unit(INSTANCE);
        static final StreamCodec<RegistryFriendlyByteBuf, Air> STREAM_CODEC_INSTANCE = StreamCodec.unit(INSTANCE);

        @Override
        public Kind kind() {
            return Kind.AIR;
        }
    }

    record BlockTarget(Block block, boolean applyPlayerFacing, boolean opposite) implements BlueprintTarget {
        static final MapCodec<BlockTarget> CODEC_INSTANCE = RecordCodecBuilder.mapCodec(i -> i.group(BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(BlockTarget::block),
                Codec.BOOL.optionalFieldOf("apply_player_facing", false).forGetter(BlockTarget::applyPlayerFacing), Codec.BOOL.optionalFieldOf("opposite", false).forGetter(BlockTarget::opposite))
                .apply(i, BlockTarget::new));

        static final StreamCodec<RegistryFriendlyByteBuf, BlockTarget> STREAM_CODEC_INSTANCE = StreamCodec.composite(ByteBufCodecs.registry(Registries.BLOCK), BlockTarget::block, ByteBufCodecs.BOOL,
                BlockTarget::applyPlayerFacing, ByteBufCodecs.BOOL, BlockTarget::opposite, BlockTarget::new);

        @Override
        public Kind kind() {
            return Kind.BLOCK;
        }
    }

    record StateTarget(BlockState state) implements BlueprintTarget {
        static final MapCodec<StateTarget> CODEC_INSTANCE = RecordCodecBuilder.mapCodec(i -> i.group(BlockState.CODEC.fieldOf("state").forGetter(StateTarget::state)).apply(i, StateTarget::new));

        static final StreamCodec<RegistryFriendlyByteBuf, StateTarget> STREAM_CODEC_INSTANCE = new StreamCodec<>() {
            @Override
            public StateTarget decode(RegistryFriendlyByteBuf buf) {
                return new StateTarget(Block.BLOCK_STATE_REGISTRY.byId(buf.readVarInt()));
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, StateTarget value) {
                buf.writeVarInt(Block.BLOCK_STATE_REGISTRY.getId(value.state));
            }
        };

        @Override
        public Kind kind() {
            return Kind.STATE;
        }
    }

    record StackTarget(ItemStack stack) implements BlueprintTarget {
        static final MapCodec<StackTarget> CODEC_INSTANCE = RecordCodecBuilder.mapCodec(i -> i.group(ItemStack.CODEC.fieldOf("stack").forGetter(StackTarget::stack)).apply(i, StackTarget::new));

        static final StreamCodec<RegistryFriendlyByteBuf, StackTarget> STREAM_CODEC_INSTANCE = StreamCodec.composite(ItemStack.STREAM_CODEC, StackTarget::stack, StackTarget::new);

        @Override
        public Kind kind() {
            return Kind.STACK;
        }
    }
}
