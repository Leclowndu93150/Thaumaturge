package com.leclowndu93150.thaumaturge.content.manabean;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.content.aspect.AspectCombinations;
import com.leclowndu93150.thaumaturge.content.legacy.LegacyIds;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class BlockEntityManaPod extends BlockEntity {
    public static final int MAX_AGE = 7;
    private static final int ASPECT_STAGE = 3;
    private static final int HERBA_CHANCE = 8;
    private static final Codec<ResourceKey<IAspect>> ASPECT_KEY_CODEC = LegacyIds.ASPECT_KEY_CODEC;

    private @Nullable ResourceKey<IAspect> aspect;

    public BlockEntityManaPod(BlockPos pos, BlockState state) {
        super(TTBlockEntities.MANA_POD.get(), pos, state);
    }

    public @Nullable ResourceKey<IAspect> aspectKey() {
        return aspect;
    }

    public @Nullable Holder<IAspect> aspect() {
        if (aspect == null || getLevel() == null) {
            return null;
        }
        return Aspects.resolve(getLevel().registryAccess(), aspect);
    }

    public void setAspect(@Nullable ResourceKey<IAspect> aspect) {
        this.aspect = aspect;
        setChanged();
        if (getLevel() != null && !getLevel().isClientSide()) {
            getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public void checkGrowth() {
        Level level = getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }
        BlockState state = getBlockState();
        int age = state.getValue(BlockManaPod.AGE);
        if (age < MAX_AGE) {
            age++;
            level.setBlock(getBlockPos(), state.setValue(BlockManaPod.AGE, age), 3);
        }
        if (age <= ASPECT_STAGE - 1) {
            return;
        }
        RandomSource random = level.getRandom();
        if (age == ASPECT_STAGE) {
            Set<ResourceKey<IAspect>> nearby = new LinkedHashSet<>();
            if (aspect != null) {
                nearby.add(aspect);
            }
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                if (level.getBlockEntity(getBlockPos().relative(dir)) instanceof BlockEntityManaPod pod
                        && pod.aspect != null) {
                    nearby.add(pod.aspect);
                }
            }
            if (nearby.size() > 1) {
                List<ResourceKey<IAspect>> keys = new ArrayList<>(nearby);
                List<ResourceKey<IAspect>> outlist = new ArrayList<>();
                for (int i = 0; i < keys.size(); i++) {
                    outlist.add(keys.get(i));
                    for (int j = 0; j < keys.size(); j++) {
                        if (i == j) {
                            continue;
                        }
                        Holder<IAspect> first = Aspects.resolve(level.registryAccess(), keys.get(i));
                        Holder<IAspect> second = Aspects.resolve(level.registryAccess(), keys.get(j));
                        if (first == null || second == null) {
                            continue;
                        }
                        Holder<IAspect> combo = AspectCombinations.result(level.registryAccess(), first, second);
                        if (combo == null || combo.getKey() == null) {
                            continue;
                        }
                        outlist.add(combo.getKey());
                        outlist.add(combo.getKey());
                    }
                }
                if (!outlist.isEmpty()) {
                    setAspect(outlist.get(random.nextInt(outlist.size())));
                }
            } else if (!nearby.isEmpty() && aspect == null) {
                setAspect(nearby.iterator().next());
            }
        }
        if (aspect == null) {
            assignWildAspect(level.registryAccess(), random);
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public void assignWildAspect(HolderLookup.Provider registries, RandomSource random) {
        if (random.nextInt(HERBA_CHANCE) == 0) {
            aspect = TTAspects.HERBA;
        } else {
            List<ResourceKey<IAspect>> primals = registries
                    .lookupOrThrow(IAspect.REGISTRY_KEY)
                    .listElements()
                    .filter(entry -> entry.value().isPrimal())
                    .map(Holder.Reference::key)
                    .toList();
            aspect = primals.get(random.nextInt(primals.size()));
        }
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        if (aspect != null) {
            TTNbt.store(output, "Aspect", ASPECT_KEY_CODEC, registries, aspect);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        aspect = TTNbt.read(input, "Aspect", ASPECT_KEY_CODEC, registries).orElse(null);
    }

    @Override
    public void applyImplicitComponents(DataComponentInput components) {
        super.applyImplicitComponents(components);
        AspectInstance stored = components.get(TTDataComponents.CRYSTAL_ASPECT.get());
        if (stored != null) {
            aspect = stored.aspect().unwrapKey().orElse(null);
        }
    }

    @Override
    public void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        Holder<IAspect> resolved = aspect();
        if (resolved != null) {
            components.set(TTDataComponents.CRYSTAL_ASPECT.get(), new AspectInstance(resolved, 1));
        }
    }

    @Override
    public void removeComponentsFromTag(CompoundTag output) {
        output.remove("Aspect");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag nbt = super.getUpdateTag(registries);
        CompoundTag additional = new CompoundTag();
        saveAdditional(additional, registries);
        nbt.merge(additional);
        return nbt;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
