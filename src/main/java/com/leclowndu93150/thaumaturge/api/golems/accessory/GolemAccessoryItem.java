package com.leclowndu93150.thaumaturge.api.golems.accessory;

import com.leclowndu93150.thaumaturge.TCIds;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

/**
 * Data map value that makes an item a golem accessory. Using an item that carries this value on a
 * golem puts the named {@link GolemAccessory} on it. The golem keeps a one-item copy of the stack,
 * components included, and drops that copy when the accessory comes off.
 *
 * <p>The map is {@link #DATA_MAP}, keyed {@code thaumaturge:golem_accessory} on the item registry
 * and synced to clients. Any item can be bound, including items from other mods, by shipping
 * {@code data/thaumaturge/data_maps/item/golem_accessory.json} in a data pack:
 *
 * <pre>{@code
 * {
 *   "values": {
 *     "examplemod:crown": { "accessory": "examplemod:crown" }
 *   }
 * }
 * }</pre>
 *
 * <p>The accessory id is resolved through {@link GolemAccessories#forItem} when the item is used, so
 * an entry that names an unregistered accessory leaves the item without effect on golems.
 *
 * @param accessory the id of the accessory the item puts on
 * @since 1.0.0
 */
public record GolemAccessoryItem(Identifier accessory) {
    /** The codec of the data map value, an object with an {@code accessory} id field. */
    public static final Codec<GolemAccessoryItem> CODEC = RecordCodecBuilder
            .create(instance -> instance.group(Identifier.CODEC.fieldOf("accessory").forGetter(GolemAccessoryItem::accessory)).apply(instance, GolemAccessoryItem::new));

    /** The item data map binding items to accessories, keyed {@code thaumaturge:golem_accessory}. */
    public static final DataMapType<Item, GolemAccessoryItem> DATA_MAP = DataMapType.builder(TCIds.rl("golem_accessory"), Registries.ITEM, CODEC)
            .synced(CODEC, false).build();

    /**
     * Validates the accessory id.
     *
     * @throws NullPointerException when {@code accessory} is null
     */
    public GolemAccessoryItem {
        Objects.requireNonNull(accessory, "accessory");
    }
}
