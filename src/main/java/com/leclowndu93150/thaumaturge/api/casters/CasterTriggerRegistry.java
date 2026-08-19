package com.leclowndu93150.thaumaturge.api.casters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Registry of block states that react to being right-clicked with an {@link ICaster} item,
 * such as crafting-structure formation triggers. Triggers are grouped by the registering
 * mod id and looked up by exact block state.
 *
 * @apiNote Registration is write-at-init: mods register their triggers during mod
 *          construction or common setup and never mutate the registry afterwards. Lookups
 *          happen from gameplay threads and rely on that discipline; there is no internal
 *          synchronization.
 * @since 1.0.0
 */
public final class CasterTriggerRegistry {
    private static final Map<String, LinkedHashMap<BlockState, List<Trigger>>> TRIGGERS = new HashMap<>();
    private static final Map<TagKey<Block>, List<Trigger>> TAG_TRIGGERS = new LinkedHashMap<>();
    private static final String DEFAULT = "default";

    private CasterTriggerRegistry() {}

    /**
     * Registers a trigger for every block state of every block in a tag.
     *
     * @param manager the manager invoked when a matching state is caster-clicked
     * @param event   the event number handed back to the manager
     * @param tag     the block tag whose members activate the trigger
     */
    public static void registerCasterBlockTagTrigger(ICasterTriggerManager manager, int event, TagKey<Block> tag) {
        TAG_TRIGGERS.computeIfAbsent(tag, key -> new ArrayList<>()).add(new Trigger(manager, event));
    }

    /**
     * Registers a trigger under an explicit mod id group.
     *
     * @param manager the manager invoked when the state is caster-clicked
     * @param event   the event number handed back to the manager
     * @param state   the exact block state that activates the trigger
     * @param modid   the mod id group to register under
     */
    public static void registerCasterBlockTrigger(ICasterTriggerManager manager, int event, BlockState state, String modid) {
        LinkedHashMap<BlockState, List<Trigger>> group = TRIGGERS.computeIfAbsent(modid, key -> new LinkedHashMap<>());
        group.computeIfAbsent(state, key -> new ArrayList<>()).add(new Trigger(manager, event));
    }

    /**
     * Registers a trigger under the default group.
     *
     * @param manager the manager invoked when the state is caster-clicked
     * @param event   the event number handed back to the manager
     * @param state   the exact block state that activates the trigger
     */
    public static void registerCasterBlockTrigger(ICasterTriggerManager manager, int event, BlockState state) {
        registerCasterBlockTrigger(manager, event, state, DEFAULT);
    }

    /**
     * Whether any group holds a trigger for a block state.
     *
     * @param state the block state to test
     * @return true when at least one trigger is registered for {@code state}
     */
    public static boolean hasTrigger(BlockState state) {
        for (LinkedHashMap<BlockState, List<Trigger>> group : TRIGGERS.values()) {
            if (group.containsKey(state)) {
                return true;
            }
        }
        for (TagKey<Block> tag : TAG_TRIGGERS.keySet()) {
            if (state.is(tag)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether a specific group holds a trigger for a block state.
     *
     * @param state the block state to test
     * @param modid the mod id group to look in
     * @return true when the group has a trigger registered for {@code state}
     */
    public static boolean hasTrigger(BlockState state, String modid) {
        LinkedHashMap<BlockState, List<Trigger>> group = TRIGGERS.get(modid);
        return group != null && group.containsKey(state);
    }

    /**
     * Runs the triggers registered for a block state across every group, stopping at the
     * first one that reports success.
     *
     * @param level       the level the click happened in
     * @param casterStack the caster stack used
     * @param player      the clicking player
     * @param pos         the clicked position
     * @param side        the clicked face
     * @param state       the clicked block state
     * @return true when a trigger handled the click
     */
    public static boolean performTrigger(Level level, ItemStack casterStack, Player player, BlockPos pos, Direction side, BlockState state) {
        for (LinkedHashMap<BlockState, List<Trigger>> group : TRIGGERS.values()) {
            if (run(group, level, casterStack, player, pos, side, state)) {
                return true;
            }
        }
        for (Map.Entry<TagKey<Block>, List<Trigger>> entry : TAG_TRIGGERS.entrySet()) {
            if (!state.is(entry.getKey())) {
                continue;
            }
            for (Trigger trigger : entry.getValue()) {
                if (trigger.manager().performTrigger(level, casterStack, player, pos, side, trigger.event())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Runs the triggers registered for a block state within one group, stopping at the
     * first one that reports success.
     *
     * @param level       the level the click happened in
     * @param casterStack the caster stack used
     * @param player      the clicking player
     * @param pos         the clicked position
     * @param side        the clicked face
     * @param state       the clicked block state
     * @param modid       the mod id group to run
     * @return true when a trigger in the group handled the click
     */
    public static boolean performTrigger(Level level, ItemStack casterStack, Player player, BlockPos pos, Direction side, BlockState state, String modid) {
        LinkedHashMap<BlockState, List<Trigger>> group = TRIGGERS.get(modid);
        return group != null && run(group, level, casterStack, player, pos, side, state);
    }

    private static boolean run(LinkedHashMap<BlockState, List<Trigger>> group, Level level, ItemStack casterStack, Player player, BlockPos pos, Direction side, BlockState state) {
        List<Trigger> list = group.get(state);
        if (list == null) {
            return false;
        }
        for (Trigger trigger : list) {
            if (trigger.manager().performTrigger(level, casterStack, player, pos, side, trigger.event())) {
                return true;
            }
        }
        return false;
    }

    private record Trigger(ICasterTriggerManager manager, int event) {
    }
}
