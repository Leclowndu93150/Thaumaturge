package com.leclowndu93150.thaumaturge.content.golem.accessory;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessory;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessoryBehavior;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessoryContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class GolemAccessoryStateHolder {
    private static final String ITEMS_KEY = "items";
    private static final Codec<Map<ResourceLocation, ItemStack>> ITEMS_CODEC =
            Codec.unboundedMap(ResourceLocation.CODEC, ItemStack.CODEC);

    private final IGolemAPI golem;
    private final Map<ResourceLocation, ItemStack> items = new LinkedHashMap<>();
    private final Map<ResourceLocation, AccessoryStateSlot<?>> slots = new LinkedHashMap<>();
    private final Map<ResourceLocation, GolemAccessoryContext> contexts = new LinkedHashMap<>();

    public GolemAccessoryStateHolder(IGolemAPI golem) {
        this.golem = golem;
    }

    public boolean isEmpty() {
        return items.isEmpty() && slots.isEmpty();
    }

    public void attach(GolemAccessory accessory, ItemStack attachedStack) {
        ItemStack worn = attachedStack.copyWithCount(1);
        items.put(accessory.id(), worn);
        accessory
                .behavior()
                .ifPresent(behavior -> slots.put(
                        accessory.id(), AccessoryStateSlot.attached(context(accessory), behavior, worn.copy())));
    }

    public ItemStack detach(GolemAccessory accessory) {
        ItemStack returned = items.remove(accessory.id());
        if (returned == null) {
            returned = ItemStack.EMPTY;
        }
        AccessoryStateSlot<?> slot = slots.remove(accessory.id());
        if (slot != null) {
            slot.remove(context(accessory), returned);
        }
        contexts.remove(accessory.id());
        return returned;
    }

    public void clear() {
        items.clear();
        slots.clear();
        contexts.clear();
    }

    public boolean tick() {
        if (slots.isEmpty()) {
            return false;
        }
        boolean syncedChanged = false;
        for (Map.Entry<ResourceLocation, AccessoryStateSlot<?>> entry : slots.entrySet()) {
            AccessoryStateSlot<?> slot = entry.getValue();
            AccessoryStateSlot<?> next = slot.tick(context(slot.accessory()));
            if (next != slot) {
                entry.setValue(next);
                syncedChanged |= next.synced();
            }
        }
        return syncedChanged;
    }

    public <S> Optional<S> state(GolemAccessoryBehavior<S> behavior) {
        for (AccessoryStateSlot<?> slot : slots.values()) {
            Optional<S> state = slot.stateFor(behavior);
            if (state.isPresent()) {
                return state;
            }
        }
        return Optional.empty();
    }

    public <S> boolean update(GolemAccessoryBehavior<S> behavior, UnaryOperator<S> update) {
        for (Map.Entry<ResourceLocation, AccessoryStateSlot<?>> entry : slots.entrySet()) {
            if (entry.getValue().behavior() == behavior) {
                entry.setValue(entry.getValue().updated(behavior, update));
                return true;
            }
        }
        return false;
    }

    public GolemAccessoryStates synced() {
        return new GolemAccessoryStates(
                slots.values().stream().filter(AccessoryStateSlot::synced).toList());
    }

    public CompoundTag save(DynamicOps<Tag> ops) {
        CompoundTag output = new CompoundTag();
        if (!items.isEmpty()) {
            ITEMS_CODEC
                    .encodeStart(ops, items)
                    .resultOrPartial(
                            error -> Thaumaturge.LOGGER.error("Could not save golem accessory items: {}", error))
                    .ifPresent(tag -> output.put(ITEMS_KEY, tag));
        }
        for (AccessoryStateSlot<?> slot : slots.values()) {
            slot.save(output, ops);
        }
        return output;
    }

    public void load(CompoundTag input, DynamicOps<Tag> ops, List<GolemAccessory> worn) {
        clear();
        Tag savedItems = input.get(ITEMS_KEY);
        Map<ResourceLocation, ItemStack> saved = savedItems == null
                ? Map.of()
                : ITEMS_CODEC
                        .parse(ops, savedItems)
                        .resultOrPartial(
                                error -> Thaumaturge.LOGGER.error("Could not load golem accessory items: {}", error))
                        .orElse(Map.of());
        for (GolemAccessory accessory : worn) {
            ItemStack item = saved.get(accessory.id());
            if (item != null) {
                items.put(accessory.id(), item.copyWithCount(1));
            }
            accessory
                    .behavior()
                    .ifPresent(behavior ->
                            slots.put(accessory.id(), AccessoryStateSlot.load(accessory, behavior, input, ops)));
        }
    }

    private GolemAccessoryContext context(GolemAccessory accessory) {
        GolemAccessoryContext context = contexts.get(accessory.id());
        if (context == null) {
            context = new GolemAccessoryContext(golem, accessory);
            contexts.put(accessory.id(), context);
        }
        return context;
    }
}
