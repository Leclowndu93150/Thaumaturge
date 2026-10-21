package com.leclowndu93150.thaumaturge.content.golem;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessories;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessory;
import com.leclowndu93150.thaumaturge.api.golems.duty.GolemDuties;
import com.leclowndu93150.thaumaturge.api.golems.duty.GolemDuty;
import net.minecraft.nbt.CompoundTag;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

final class GolemOwnerActions {
    private static final Identifier MASTERY_RESEARCH = TTIds.rl("golem_direct");
    private static final String FOLLOW_MESSAGE_KEY = "message.thaumaturge.golem.follow";
    private static final String STAY_MESSAGE_KEY = "message.thaumaturge.golem.stay";
    private static final float PICKUP_DROP_OFFSET = 0.5F;

    private GolemOwnerActions() {}

    static void apply(EntityThaumaturgeGolem golem, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            pickUp(golem, player, hand);
        } else if (held.is(TTItems.GOLEM_BELL.get())) {
            if (KnowledgeAccess.of(player).isResearchComplete(MASTERY_RESEARCH)) {
                toggleFollow(golem, player, hand);
            }
        } else if (!tryFitAccessory(golem, player, hand, held)) {
            tryDye(golem, player, hand, held);
        }
    }

    private static void pickUp(EntityThaumaturgeGolem golem, Player player, InteractionHand hand) {
        ServerLevel level = (ServerLevel) golem.level();
        golem.playGolemSound(TTSounds.ZAP.get());
        golem.releaseTask();
        golem.spillHeldItems();
        golem.dropAccessories(level);
        ItemStack carried = new ItemStack(TTItems.GOLEM_PLACER.get());
        carried.set(TTDataComponents.GOLEM_PROPERTIES.get(), golem.properties());
        carried.set(TTDataComponents.GOLEM_XP.get(), golem.getRankXp());
        golem.spawnAtLocation(level, carried, PICKUP_DROP_OFFSET);
        golem.discard();
        player.swing(hand);
    }

    private static void toggleFollow(EntityThaumaturgeGolem golem, Player player, InteractionHand hand) {
        if (golem.duty().isPresent() && player instanceof ServerPlayer actor) {
            golem.exitDuty(actor);
        }
        boolean follow = !golem.isTrailingOwner();
        golem.setTrailingOwner(follow);
        golem.releaseTask();
        golem.playGolemSound(TTSounds.SCAN.get());
        player.swing(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.translatable(follow ? FOLLOW_MESSAGE_KEY : STAY_MESSAGE_KEY), true);
        }
        (follow ? GolemEvent.FOLLOW : GolemEvent.STAY).broadcast(golem);
        if (!follow) {
            golem.setHomeTo(golem.blockPosition(), GolemStatSheet.defaultHomeRadius(golem));
        }
        golem.recomputeStats();
    }

    private static boolean tryFitAccessory(EntityThaumaturgeGolem golem, Player player, InteractionHand hand, ItemStack held) {
        Optional<GolemAccessory> candidate = GolemAccessories.forItem(held);
        if (candidate.isEmpty()) {
            return false;
        }
        GolemAccessory accessory = candidate.get();
        Optional<GolemDuty> duty = GolemDuties.forAccessory(accessory.id());
        if (duty.isPresent() && (golem.duty().isPresent() || !(player instanceof ServerPlayer) || golem.distanceToSqr(player) > 64)) {
            return true;
        }
        for (GolemAccessory worn : golem.getAccessories()) {
            if (worn.id().equals(accessory.id()) || accessory.group().excludes(worn.group())) {
                return true;
            }
        }
        CompoundTag state = duty.map(GolemDuty::initialState).orElseGet(CompoundTag::new);
        golem.fitAccessory(accessory, held);
        if (duty.isPresent() && !golem.tryEnterDuty((ServerPlayer) player, duty.get().id(), state)) {
            golem.removeAccessory(accessory);
            return true;
        }
        golem.playGolemSound(TTSounds.CLACK.get());
        held.shrink(1);
        player.swing(hand);
        return true;
    }

    private static void tryDye(EntityThaumaturgeGolem golem, Player player, InteractionHand hand, ItemStack held) {
        DyeColor dye = held.get(DataComponents.DYE);
        if (dye == null) {
            return;
        }
        golem.playGolemSound(TTSounds.ZAP.get());
        golem.paint((byte) (1 + dye.getId()));
        held.shrink(1);
        player.swing(hand);
    }
}
