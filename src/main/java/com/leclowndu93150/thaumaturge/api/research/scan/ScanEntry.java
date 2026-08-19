package com.leclowndu93150.thaumaturge.api.research.scan;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

/**
 * Data-driven scannable subject loaded from datapacks under {@code thaumaturge/scan}. Each entry
 * grants one research key when the scanned target matches any of its declared blocks, items, or
 * entity types. Target sets accept direct identifiers and {@code #tag} references.
 *
 * <p>Blocks match both the placed block and its item form, so an entry listing a block also
 * covers the held or dropped item without a separate item declaration. Item entities match
 * through their contained stack.
 *
 * <p>The {@link ScanningManager} evaluates every loaded entry on each scan attempt; no
 * registration call is needed. Subjects that require code (custom predicates, gating, dynamic
 * keys) implement {@link IScanThing} instead.
 *
 * @param key the research key granted when the target matches
 * @param blocks blocks matched in world or as items, empty to match no blocks
 * @param items items matched as stacks or item entities, empty to match no items
 * @param entities entity types matched, empty to match no entities
 * @since 1.0.0
 */
public record ScanEntry(Identifier key, Optional<HolderSet<Block>> blocks, Optional<HolderSet<Item>> items, Optional<HolderSet<EntityType<?>>> entities) {

    /**
     * The datapack registry housing scan entries, at {@code data/<ns>/thaumaturge/scan/}.
     */
    public static final ResourceKey<Registry<ScanEntry>> REGISTRY_KEY = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath("thaumaturge", "scan"));

    /**
     * Codec used for both datapack loading and network sync.
     */
    public static final Codec<ScanEntry> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(Identifier.CODEC.fieldOf("key").forGetter(ScanEntry::key), RegistryCodecs.homogeneousList(Registries.BLOCK).optionalFieldOf("blocks").forGetter(ScanEntry::blocks),
                    RegistryCodecs.homogeneousList(Registries.ITEM).optionalFieldOf("items").forGetter(ScanEntry::items),
                    RegistryCodecs.homogeneousList(Registries.ENTITY_TYPE).optionalFieldOf("entities").forGetter(ScanEntry::entities))
            .apply(instance, ScanEntry::new));

    /**
     * Whether the given scan target matches this entry.
     *
     * @param player the scanning player, used for level access
     * @param target the scan target; position, entity, stack, or {@code null}
     * @return {@code true} when the target matches any declared set
     */
    public boolean matches(Player player, @Nullable Object target) {
        if (target instanceof BlockPos pos) {
            return blocks.isPresent() && blocks.get().contains(player.level().getBlockState(pos).typeHolder());
        }
        if (target instanceof Entity entity && !(entity instanceof ItemEntity)) {
            return entities.isPresent() && entities.get().contains(entity.getType().builtInRegistryHolder());
        }
        ItemStack stack = ItemStack.EMPTY;
        if (target instanceof ItemStack targetStack) {
            stack = targetStack;
        } else if (target instanceof ItemEntity itemEntity) {
            stack = itemEntity.getItem();
        }
        if (stack.isEmpty()) {
            return false;
        }
        if (items.isPresent() && items.get().contains(stack.typeHolder())) {
            return true;
        }
        return blocks.isPresent() && stack.getItem() instanceof BlockItem blockItem && blocks.get().contains(blockItem.getBlock().builtInRegistryHolder());
    }
}
