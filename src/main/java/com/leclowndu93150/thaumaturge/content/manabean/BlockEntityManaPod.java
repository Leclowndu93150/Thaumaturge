package com.leclowndu93150.thaumaturge.content.manabean;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.legacy.LegacyIds;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import com.mojang.serialization.Codec;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class BlockEntityManaPod extends AbstractSyncedBlockEntity {
    public static final int MAX_AGE = 7;
    private static final int BLEND_STAGE = 3;
    private static final int HERBA_ONE_IN = 8;
    private static final int MEMBER_WEIGHT = 1;
    private static final int OFFSPRING_WEIGHT = 4;
    private static final int PARENT_COUNT = 2;
    private static final Codec<ResourceKey<IAspect>> ASPECT_KEY_CODEC = LegacyIds.ASPECT_KEY_CODEC;

    private @Nullable ResourceKey<IAspect> aspect;
    private boolean showcase;

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

    public boolean isShowcase() {
        return showcase;
    }

    public void setShowcase() {
        showcase = true;
        setChanged();
    }

    public void checkGrowth() {
        Level level = getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }
        BlockState state = getBlockState();
        int stage = Math.min(state.getValue(BlockManaPod.AGE) + 1, MAX_AGE);
        level.setBlock(getBlockPos(), state.setValue(BlockManaPod.AGE, stage), 3);
        settleAspect(level, stage, level.getRandom());
    }

    public void rollSpawnAspect(LevelReader level, RandomSource random) {
        aspect = random.nextInt(HERBA_ONE_IN) == 0 ? TTAspects.HERBA : pickPrimal(level.registryAccess(), random);
        setChanged();
    }

    public void settleAspect(LevelReader level, int stage, RandomSource random) {
        if (stage < BLEND_STAGE) {
            return;
        }
        ResourceKey<IAspect> before = aspect;
        if (stage == BLEND_STAGE) {
            blendWithNeighbours(level, random);
        }
        if (aspect == null) {
            aspect = random.nextInt(HERBA_ONE_IN) == 0 ? TTAspects.HERBA : pickPrimal(level.registryAccess(), random);
        }
        if (!Objects.equals(before, aspect)) {
            setAspect(aspect);
        }
    }

    private void blendWithNeighbours(LevelReader level, RandomSource random) {
        Set<ResourceKey<IAspect>> present = new LinkedHashSet<>();
        if (aspect != null) {
            present.add(aspect);
        }
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (level.getBlockEntity(getBlockPos().relative(side)) instanceof BlockEntityManaPod neighbor
                    && neighbor.aspect != null) {
                present.add(neighbor.aspect);
            }
        }
        if (present.size() == 1) {
            if (aspect == null) {
                aspect = present.iterator().next();
            }
            return;
        }
        if (present.isEmpty()) {
            return;
        }

        Map<ResourceKey<IAspect>, Integer> weights = new LinkedHashMap<>();
        present.forEach(key -> weights.put(key, MEMBER_WEIGHT));
        level.registryAccess()
                .lookupOrThrow(IAspect.REGISTRY_KEY)
                .listElements()
                .filter(holder -> bredFrom(holder.value(), present))
                .forEach(holder -> weights.merge(holder.key(), OFFSPRING_WEIGHT, Integer::sum));
        aspect = pickByWeight(weights, random);
    }

    private static boolean bredFrom(IAspect candidate, Set<ResourceKey<IAspect>> present) {
        List<Holder<IAspect>> parents = candidate.components();
        if (parents.size() != PARENT_COUNT) {
            return false;
        }
        Optional<ResourceKey<IAspect>> first = parents.get(0).unwrapKey();
        Optional<ResourceKey<IAspect>> second = parents.get(1).unwrapKey();
        return first.isPresent()
                && second.isPresent()
                && !first.equals(second)
                && present.contains(first.get())
                && present.contains(second.get());
    }

    private static ResourceKey<IAspect> pickByWeight(Map<ResourceKey<IAspect>, Integer> weights, RandomSource random) {
        int roll = random.nextInt(
                weights.values().stream().mapToInt(Integer::intValue).sum());
        for (Map.Entry<ResourceKey<IAspect>, Integer> entry : weights.entrySet()) {
            roll -= entry.getValue();
            if (roll < 0) {
                return entry.getKey();
            }
        }
        throw new IllegalStateException("Weighted aspect selection exceeded its candidates");
    }

    private static ResourceKey<IAspect> pickPrimal(HolderLookup.Provider registries, RandomSource random) {
        List<ResourceKey<IAspect>> primals = registries
                .lookupOrThrow(IAspect.REGISTRY_KEY)
                .listElements()
                .filter(holder -> holder.value().isPrimal())
                .map(Holder.Reference::key)
                .toList();
        if (primals.isEmpty()) {
            throw new IllegalStateException("Aspect registry contains no primal aspects");
        }
        return primals.get(random.nextInt(primals.size()));
    }

    public void assignWildAspect(HolderLookup.Provider registries, RandomSource random) {
        if (random.nextInt(HERBA_ONE_IN) == 0) {
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
        if (showcase) {
            output.putBoolean("Showcase", true);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        aspect = TTNbt.read(input, "Aspect", ASPECT_KEY_CODEC, registries).orElse(null);
        showcase = input.getBoolean("Showcase");
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
}
