package com.leclowndu93150.thaumaturge.content.eldritch.lock;

import com.leclowndu93150.thaumaturge.api.labyrinth.MazeId;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthService;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeRecord;
import com.leclowndu93150.thaumaturge.content.eldritch.wayfinding.TabletResonance;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public final class RunedTabletItem extends Item {
    private static final String BOUND_TOOLTIP = "tooltip.thaumaturge.runed_tablet.bound";
    private static final int CRUMBLE_CHECK_INTERVAL = 40;
    private static final float CRUMBLE_VOLUME = 0.6F;
    private static final float CRUMBLE_PITCH = 0.7F;

    public RunedTabletItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity owner, int index, boolean selected) {
        if (!(world instanceof ServerLevel level)) return;
        EquipmentSlot slot = owner instanceof LivingEntity living && living.getOffhandItem() == stack
                ? EquipmentSlot.OFFHAND
                : selected ? EquipmentSlot.MAINHAND : null;
        Optional<MazeId> maze = LabyrinthKeys.boundTo(stack);
        if (maze.isEmpty()) {
            return;
        }
        if (owner instanceof ServerPlayer player && slot != null) {
            TabletResonance.tick(level, player, maze.get(), slot);
        }
        if (level.getGameTime() % CRUMBLE_CHECK_INTERVAL != 0
                || !ThaumaturgeServerConfig.LABYRINTH.crumbleSpentTablets.get()) {
            return;
        }
        Optional<MazeRecord> record = LabyrinthService.byId(level.getServer(), maze.get());
        if (record.isPresent() && !record.get().state().phase().isPastLock()) {
            return;
        }
        level.playSound(
                null,
                owner.blockPosition(),
                SoundEvents.DECORATED_POT_SHATTER,
                SoundSource.PLAYERS,
                CRUMBLE_VOLUME,
                CRUMBLE_PITCH);
        stack.setCount(0);
    }

    @Override
    public void appendHoverText(
            ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        LabyrinthKeys.boundTo(stack)
                .ifPresent(maze -> tooltip.add(
                        Component.translatable(BOUND_TOOLTIP, maze.value()).withStyle(ChatFormatting.DARK_PURPLE)));
    }
}
