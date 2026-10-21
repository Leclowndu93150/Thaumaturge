package com.leclowndu93150.thaumaturge.api.wands;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Explicit opt-in for storage with atomic multi-pool writes. Reads, capacity and revision queries
 * must be pure, including on copied stacks. A revision changes whenever the backing store changes.
 * Successful replacement joins the supplied transaction, including nested rollback; rejection
 * leaves all state unchanged. External backing data must be saved and synchronized on root commit.
 */
public interface ITransactionalWandVisStorage extends IWandVisStorage {
    int capacityCentivis(ItemStack wand, ResourceKey<IAspect> aspect);

    long revision(ItemStack wand);

    boolean replaceVis(ItemStack wand, long expectedRevision, WandVis expected, WandVis replacement, TransactionContext transaction);
}
