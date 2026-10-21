package com.leclowndu93150.thaumaturge.content.taint;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import com.leclowndu93150.thaumaturge.registry.TTItemTags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Tool;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class TaintToolEvents {
    private static final float TAINT_TOOL_SPEED_MULTIPLIER = 4.0F;

    private TaintToolEvents() {}

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (!event.getState().is(TTBlockTags.TAINT_BLOCKS)) {
            return;
        }
        Player player = event.getEntity();
        ItemStack held = player.getMainHandItem();
        if (!held.is(TTItemTags.TAINT_TOOLS)) {
            return;
        }
        Tool tool = held.get(DataComponents.TOOL);
        if (tool == null) {
            return;
        }
        float toolSpeed = bestRuleSpeed(tool) + (float) player.getAttributeValue(Attributes.MINING_EFFICIENCY);
        event.setNewSpeed(event.getNewSpeed() * toolSpeed * TAINT_TOOL_SPEED_MULTIPLIER);
    }

    private static float bestRuleSpeed(Tool tool) {
        float best = tool.defaultMiningSpeed();
        for (Tool.Rule rule : tool.rules()) {
            if (rule.speed().isPresent()) {
                best = Math.max(best, rule.speed().get());
            }
        }
        return best;
    }
}
