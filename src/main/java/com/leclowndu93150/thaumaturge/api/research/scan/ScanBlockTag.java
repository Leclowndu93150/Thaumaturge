package com.leclowndu93150.thaumaturge.api.research.scan;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

/**
 * Scannable subject matching any block in a block tag.
 *
 * @since 1.0.0
 */
public class ScanBlockTag implements IScanThing {
    private final ResourceLocation research;
    private final TagKey<Block> tag;

    /**
     * Creates the subject.
     *
     * @param research the research key granted on scan
     * @param tag the block tag this subject matches
     */
    public ScanBlockTag(ResourceLocation research, TagKey<Block> tag) {
        this.research = research;
        this.tag = tag;
    }

    @Override
    public boolean checkThing(Player player, @Nullable Object target) {
        return target instanceof BlockPos pos
                && player.level().getBlockState(pos).is(tag);
    }

    @Override
    public ResourceLocation getResearchKey(Player player, @Nullable Object target) {
        return research;
    }
}
