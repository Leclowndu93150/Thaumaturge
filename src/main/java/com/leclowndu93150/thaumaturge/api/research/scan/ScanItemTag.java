package com.leclowndu93150.thaumaturge.api.research.scan;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Scannable subject matching any item in an item tag, whether held, dropped, or placed as the
 * scanned block's item form.
 *
 * @since 1.0.0
 */
public class ScanItemTag implements IScanThing {
    private final ResourceLocation research;
    private final TagKey<Item> tag;

    /**
     * Creates the subject.
     *
     * @param research the research key granted on scan
     * @param tag the item tag this subject matches
     */
    public ScanItemTag(ResourceLocation research, TagKey<Item> tag) {
        this.research = research;
        this.tag = tag;
    }

    @Override
    public boolean checkThing(Player player, @Nullable Object target) {
        ItemStack stack = ScanningManager.getItemFromParms(player, target);
        return !stack.isEmpty() && stack.is(tag);
    }

    @Override
    public ResourceLocation getResearchKey(Player player, @Nullable Object target) {
        return research;
    }
}
