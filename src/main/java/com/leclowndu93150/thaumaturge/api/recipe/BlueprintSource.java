package com.leclowndu93150.thaumaturge.api.recipe;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public sealed interface BlueprintSource {
    Codec<BlueprintSource> CODEC = Kind.CODEC.dispatch("type", BlueprintSource::kind, k -> k.codec);

    StreamCodec<RegistryFriendlyByteBuf, BlueprintSource> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public BlueprintSource decode(RegistryFriendlyByteBuf buf) {
            int ord = buf.readByte();
            Kind kind = Kind.values()[ord];
            return kind.streamCodec.decode(buf);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, BlueprintSource value) {
            Kind kind = value.kind();
            buf.writeByte(kind.ordinal());
            kind.encode(buf, value);
        }
    };

    Kind kind();

    boolean matches(BlockState state);

    BlockState getState();

    List<ItemStack> getRepresentations();

    enum Kind implements StringRepresentable {
        BLOCK("block", BlockSource.CODEC_INSTANCE, BlockSource.STREAM_CODEC_INSTANCE), STATE("state", StateSource.CODEC_INSTANCE, StateSource.STREAM_CODEC_INSTANCE), TAG("tag",
                TagSource.CODEC_INSTANCE, TagSource.STREAM_CODEC_INSTANCE);

        public static final Codec<Kind> CODEC = StringRepresentable.fromEnum(Kind::values);

        private final String name;
        final MapCodec<? extends BlueprintSource> codec;
        final StreamCodec<RegistryFriendlyByteBuf, ? extends BlueprintSource> streamCodec;

        Kind(String name, MapCodec<? extends BlueprintSource> codec, StreamCodec<RegistryFriendlyByteBuf, ? extends BlueprintSource> streamCodec) {
            this.name = name;
            this.codec = codec;
            this.streamCodec = streamCodec;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        void encode(RegistryFriendlyByteBuf buf, BlueprintSource value) {
            ((StreamCodec) this.streamCodec).encode(buf, value);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    record BlockSource(Block block) implements BlueprintSource {
        static final MapCodec<BlockSource> CODEC_INSTANCE = RecordCodecBuilder
                .mapCodec(i -> i.group(BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(BlockSource::block)).apply(i, BlockSource::new));

        static final StreamCodec<RegistryFriendlyByteBuf, BlockSource> STREAM_CODEC_INSTANCE = StreamCodec.composite(ByteBufCodecs.registry(Registries.BLOCK), BlockSource::block, BlockSource::new);

        @Override
        public Kind kind() {
            return Kind.BLOCK;
        }

        @Override
        public boolean matches(BlockState state) {
            return state.getBlock() == this.block;
        }

        @Override
        public BlockState getState() {
            return block.defaultBlockState();
        }

        @Override
        public List<ItemStack> getRepresentations() {
            if (!block.defaultBlockState().getFluidState().isEmpty())
                return List.of(block.defaultBlockState().getFluidState().getType().getBucket().getDefaultInstance());
            if (block.asItem().getDefaultInstance().isEmpty())
                return List.of();
            return List.of(block.asItem().getDefaultInstance());
        }
    }

    record StateSource(BlockState state) implements BlueprintSource {
        static final MapCodec<StateSource> CODEC_INSTANCE = RecordCodecBuilder.mapCodec(i -> i.group(BlockState.CODEC.fieldOf("state").forGetter(StateSource::state)).apply(i, StateSource::new));

        static final StreamCodec<RegistryFriendlyByteBuf, StateSource> STREAM_CODEC_INSTANCE = new StreamCodec<>() {
            @Override
            public StateSource decode(RegistryFriendlyByteBuf buf) {
                int id = buf.readVarInt();
                BlockState st = Block.BLOCK_STATE_REGISTRY.byId(id);
                if (st == null) {
                    throw new IllegalStateException("unknown blockstate id " + id);
                }
                return new StateSource(st);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, StateSource value) {
                buf.writeVarInt(Block.BLOCK_STATE_REGISTRY.getId(value.state));
            }
        };

        @Override
        public Kind kind() {
            return Kind.STATE;
        }

        @Override
        public boolean matches(BlockState state) {
            return state == this.state;
        }

        @Override
        public BlockState getState() {
            return state;
        }

        @Override
        public List<ItemStack> getRepresentations() {
            return List.of(state.getBlock().asItem().getDefaultInstance());
        }
    }

    record TagSource(TagKey<Block> tag) implements BlueprintSource {
        static final MapCodec<TagSource> CODEC_INSTANCE = RecordCodecBuilder.mapCodec(i -> i.group(TagKey.codec(Registries.BLOCK).fieldOf("tag").forGetter(TagSource::tag)).apply(i, TagSource::new));

        static final StreamCodec<RegistryFriendlyByteBuf, TagSource> STREAM_CODEC_INSTANCE = StreamCodec
                .composite(Identifier.STREAM_CODEC.map(rl -> TagKey.create(Registries.BLOCK, rl), TagKey::location), TagSource::tag, TagSource::new);

        @Override
        public Kind kind() {
            return Kind.TAG;
        }

        @Override
        public boolean matches(BlockState state) {
            return state.is(this.tag);
        }

        @Override
        public BlockState getState() {
            Optional<HolderSet.Named<Block>> tagSet = BuiltInRegistries.BLOCK.get(this.tag);
            if (tagSet.isEmpty())
                return Blocks.AIR.defaultBlockState();
            HolderSet.Named<Block> namedTag = tagSet.get();
            if (!namedTag.isBound())
                return Blocks.AIR.defaultBlockState();
            return namedTag.get(0).value().defaultBlockState();
        }

        @Override
        public List<ItemStack> getRepresentations() {
            Optional<HolderSet.Named<Block>> tagSet = BuiltInRegistries.BLOCK.get(this.tag);
            if (tagSet.isEmpty())
                return List.of();
            HolderSet.Named<Block> namedTag = tagSet.get();
            if (!namedTag.isBound())
                return List.of();
            List<ItemStack> stacks = new ArrayList<>();
            for (Holder<Block> blockHolder : namedTag) {
                ItemStack stack = new ItemStack(blockHolder.value());
                if (!stack.isEmpty()) {
                    stacks.add(stack);
                }
            }
            return ImmutableList.copyOf(stacks);
        }
    }
}
