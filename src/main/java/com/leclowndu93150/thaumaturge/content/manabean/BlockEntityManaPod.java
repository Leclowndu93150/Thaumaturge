package com.leclowndu93150.thaumaturge.content.manabean;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.legacy.LegacyIds;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
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
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public final class BlockEntityManaPod extends AbstractSyncedBlockEntity {
    public static final int MAX_AGE = 7;
    private static final String ASPECT_KEY = "Aspect";
    private static final int BLEND_STAGE = 3;
    private static final int HERBA_ONE_IN = 8;
    private static final int MEMBER_WEIGHT = 1;
    private static final int OFFSPRING_WEIGHT = 4;
    private static final int PARENT_COUNT = 2;

    private @Nullable ResourceKey<IAspect> aspectKey;

    public BlockEntityManaPod(BlockPos pos, BlockState state) {
        super(TTBlockEntities.MANA_POD.get(), pos, state);
    }

    public @Nullable ResourceKey<IAspect> aspectKey() {
        return aspectKey;
    }

    public @Nullable Holder<IAspect> aspect() {
        if (aspectKey == null || level == null) {
            return null;
        }
        return Aspects.resolve(level, aspectKey);
    }

    public void setAspect(@Nullable ResourceKey<IAspect> key) {
        aspectKey = key;
        setChanged();
        syncToClient();
    }

    public void checkGrowth() {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        BlockState state = getBlockState();
        int stage = Math.min(state.getValue(BlockManaPod.AGE) + 1, MAX_AGE);
        server.setBlock(worldPosition, state.setValue(BlockManaPod.AGE, stage), Block.UPDATE_CLIENTS);
        settleAspect(server, stage, server.getRandom());
    }

    public void settleAspect(LevelReader reader, int stage, RandomSource random) {
        if (stage < BLEND_STAGE) {
            return;
        }
        ResourceKey<IAspect> before = aspectKey;
        if (stage == BLEND_STAGE) {
            blendWithNeighbours(reader, random);
        }
        if (aspectKey == null) {
            aspectKey = random.nextInt(HERBA_ONE_IN) == 0 ? TTAspects.HERBA : pickPrimal(reader.registryAccess(), random);
        }
        if (!Objects.equals(before, aspectKey)) {
            setChangedAndSync();
        }
    }

    private void blendWithNeighbours(LevelReader reader, RandomSource random) {
        Set<ResourceKey<IAspect>> present = new LinkedHashSet<>();
        if (aspectKey != null) {
            present.add(aspectKey);
        }
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos neighbourPos = worldPosition.relative(side);
            if (reader.getBlockState(neighbourPos).is(TTBlocks.MANA_POD) && reader.getBlockEntity(neighbourPos) instanceof BlockEntityManaPod neighbour && neighbour.aspectKey != null) {
                present.add(neighbour.aspectKey);
            }
        }
        if (present.size() == 1) {
            if (aspectKey == null) {
                aspectKey = present.iterator().next();
            }
            return;
        }
        if (present.isEmpty()) {
            return;
        }
        Map<ResourceKey<IAspect>, Integer> weights = new LinkedHashMap<>();
        present.forEach(key -> weights.put(key, MEMBER_WEIGHT));
        reader.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).listElements().filter(holder -> bredFrom(holder.value(), present))
                .forEach(holder -> weights.merge(holder.key(), OFFSPRING_WEIGHT, Integer::sum));
        aspectKey = pickByWeight(weights, random);
    }

    private static boolean bredFrom(IAspect aspect, Set<ResourceKey<IAspect>> present) {
        List<Holder<IAspect>> parents = aspect.components();
        if (parents.size() != PARENT_COUNT) {
            return false;
        }
        Optional<ResourceKey<IAspect>> first = parents.get(0).unwrapKey();
        Optional<ResourceKey<IAspect>> second = parents.get(1).unwrapKey();
        return first.isPresent() && second.isPresent() && !first.equals(second) && present.contains(first.get()) && present.contains(second.get());
    }

    private static ResourceKey<IAspect> pickByWeight(Map<ResourceKey<IAspect>, Integer> weights, RandomSource random) {
        int total = weights.values().stream().mapToInt(Integer::intValue).sum();
        int roll = random.nextInt(total);
        for (Map.Entry<ResourceKey<IAspect>, Integer> entry : weights.entrySet()) {
            roll -= entry.getValue();
            if (roll < 0) {
                return entry.getKey();
            }
        }
        throw new IllegalStateException("Weighted aspect draw ran past its pool");
    }

    private static ResourceKey<IAspect> pickPrimal(HolderLookup.Provider registries, RandomSource random) {
        List<ResourceKey<IAspect>> primals = registries.lookupOrThrow(IAspect.REGISTRY_KEY).listElements().filter(holder -> holder.value().isPrimal()).map(Holder.Reference::key).toList();
        if (primals.isEmpty()) {
            throw new IllegalStateException("Aspect registry contains no primal aspect");
        }
        return primals.get(random.nextInt(primals.size()));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        Optional.ofNullable(aspectKey).ifPresent(key -> output.store(ASPECT_KEY, LegacyIds.ASPECT_KEY_CODEC, key));
        super.saveAdditional(output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        Optional<ResourceKey<IAspect>> stored = input.read(ASPECT_KEY, LegacyIds.ASPECT_KEY_CODEC);
        super.loadAdditional(input);
        aspectKey = stored.orElse(null);
    }

    @Override
    public void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        AspectInstance instance = components.get(TTDataComponents.CRYSTAL_ASPECT.get());
        if (instance != null) {
            aspectKey = instance.aspect().unwrapKey().orElse(null);
        }
    }

    @Override
    public void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        Holder<IAspect> holder = aspect();
        if (holder != null) {
            components.set(TTDataComponents.CRYSTAL_ASPECT.get(), new AspectInstance(holder, 1));
        }
    }

    @Override
    public void removeComponentsFromTag(ValueOutput output) {
        output.discard(ASPECT_KEY);
    }
}
