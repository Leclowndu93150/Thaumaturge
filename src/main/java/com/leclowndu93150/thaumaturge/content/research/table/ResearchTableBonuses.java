package com.leclowndu93150.thaumaturge.content.research.table;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.content.world.LoadedChunks;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.registries.DeferredBlock;
import org.jspecify.annotations.Nullable;

final class ResearchTableBonuses {
    private static final int RADIUS = 8;
    private static final int BONUS_UNIT = 1;
    private static final int DARKNESS_LIGHT_LIMIT = 4;
    private static final int DARKNESS_ODDS = 20;
    private static final int ALTITUDE_ODDS = 20;
    private static final int BOOKSHELF_ODDS = 300;
    private static final int JAR_BRAIN_ODDS = 200;
    private static final int CRYSTAL_ODDS = 10;
    private static final int SUBSTRATE_ODDS = 20;
    private static final int WATER_ODDS = 15;
    private static final int HEAT_ODDS = 20;
    private static final int ORDO_ODDS = 20;
    private static final float[] ALTITUDE_FACTORS = {0.5F, 0.66F, 0.75F};
    private static final List<CrystalRow> CRYSTAL_ROWS = List.of(
            new CrystalRow(TTBlocks.CRYSTAL_AER, TTAspects.AER),
            new CrystalRow(TTBlocks.CRYSTAL_IGNIS, TTAspects.IGNIS),
            new CrystalRow(TTBlocks.CRYSTAL_AQUA, TTAspects.AQUA),
            new CrystalRow(TTBlocks.CRYSTAL_TERRA, TTAspects.TERRA),
            new CrystalRow(TTBlocks.CRYSTAL_ORDO, TTAspects.ORDO),
            new CrystalRow(TTBlocks.CRYSTAL_PERDITIO, TTAspects.PERDITIO));

    private final Level level;
    private final RandomSource random;
    private final List<Holder<IAspect>> successes = new ArrayList<>();
    private AspectList bonus;

    private ResearchTableBonuses(Level level, RandomSource random, AspectList bonus) {
        this.level = level;
        this.random = random;
        this.bonus = bonus;
    }

    private record CrystalRow(DeferredBlock<?> block, ResourceKey<IAspect> aspect) {}

    static AspectList recalculate(Level level, BlockPos tablePos, AspectList current, RandomSource random) {
        return new ResearchTableBonuses(level, random, current).run(tablePos);
    }

    private AspectList run(BlockPos tablePos) {
        BlockPos above = tablePos.above();
        int light = Math.max(level.getBrightness(LightLayer.BLOCK, above), level.getBrightness(LightLayer.SKY, above));
        if (light < DARKNESS_LIGHT_LIMIT && !level.canSeeSky(above) && roll(DARKNESS_ODDS)) {
            add(resolve(TTAspects.PERDITIO));
        }
        int altitude = LevelSpaces.worldBlock(level, tablePos).getY();
        for (float factor : ALTITUDE_FACTORS) {
            if (altitude > level.getHeight() * factor && roll(ALTITUDE_ODDS)) {
                add(resolve(TTAspects.AER));
            }
        }
        sampleSurroundings(tablePos);
        return bonus;
    }

    private void sampleSurroundings(BlockPos center) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dy = -RADIUS; dy <= RADIUS; dy++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    cursor.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    if (!LoadedChunks.contains(level, cursor)) {
                        continue;
                    }
                    successes.clear();
                    collect(level.getBlockState(cursor));
                    if (!successes.isEmpty() && add(successes.get(random.nextInt(successes.size())))) {
                        return;
                    }
                }
            }
        }
    }

    private void collect(BlockState state) {
        if (state.is(Blocks.BOOKSHELF) && roll(BOOKSHELF_ODDS)) {
            offer(randomAspect());
        }
        if (state.is(TTBlocks.JAR_BRAIN.get()) && roll(JAR_BRAIN_ODDS)) {
            offer(randomAspect());
        }
        for (CrystalRow row : CRYSTAL_ROWS) {
            if (state.is(row.block().get()) && roll(CRYSTAL_ODDS)) {
                offer(resolve(row.aspect()));
            }
        }
        // The source tag is unavailable in 1.21.1; its vanilla members available here are covered by BlockTags.DIRT.
        if (state.is(BlockTags.DIRT) && roll(SUBSTRATE_ODDS)) {
            offer(resolve(TTAspects.TERRA));
        }
        FluidState fluid = state.getFluidState();
        if (fluid.is(FluidTags.WATER) && roll(WATER_ODDS)) {
            offer(resolve(TTAspects.AQUA));
        }
        if ((fluid.is(FluidTags.LAVA) || state.is(Blocks.FIRE)) && roll(HEAT_ODDS)) {
            offer(resolve(TTAspects.IGNIS));
        }
        if (state.is(TTBlockTags.RESEARCH_BONUS_ORDO) && roll(ORDO_ODDS)) {
            offer(resolve(TTAspects.ORDO));
        }
    }

    private boolean roll(int odds) {
        return random.nextInt(odds) == 0;
    }

    private void offer(@Nullable Holder<IAspect> aspect) {
        if (aspect != null) {
            successes.add(aspect);
        }
    }

    private boolean add(@Nullable Holder<IAspect> aspect) {
        if (aspect == null || bonus.amountOf(aspect) > 0) {
            return false;
        }
        bonus = bonus.add(aspect, BONUS_UNIT);
        return true;
    }

    private @Nullable Holder<IAspect> resolve(ResourceKey<IAspect> key) {
        return Aspects.resolve(level, key);
    }

    private @Nullable Holder<IAspect> randomAspect() {
        List<Holder.Reference<IAspect>> all = level.registryAccess()
                .lookupOrThrow(IAspect.REGISTRY_KEY)
                .listElements()
                .toList();
        return all.isEmpty() ? null : all.get(random.nextInt(all.size()));
    }
}
