package com.leclowndu93150.thaumaturge.api.research.scan;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

/**
 * Scannable subject matching any block in a block tag. Modern replacement for the legacy
 * material-based scan.
 *
 * @since 1.0.0
 */
public class ScanBlockTag implements IScanThing {
    private final Identifier research;
    private final TagKey<Block> tag;

    /**
     * Creates the subject.
     *
     * @param research the research key granted on scan
     * @param tag the block tag this subject matches
     */
    public ScanBlockTag(Identifier research, TagKey<Block> tag) {
        this.research = research;
        this.tag = tag;
    }

    @Override
    public boolean checkThing(Player player, @Nullable Object target) {
        return target instanceof BlockPos pos && player.level().getBlockState(pos).is(tag);
    }

    @Override
    public Identifier getResearchKey(Player player, @Nullable Object target) {
        return research;
    }
}
