package com.leclowndu93150.thaumaturge.content.warp;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import com.leclowndu93150.thaumaturge.api.warp.IPlayerWarp;
import com.leclowndu93150.thaumaturge.api.warp.ItemWarp;
import com.leclowndu93150.thaumaturge.api.warp.WarpHelper;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.compat.curio.ThaumaturgeCuriosCompat;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import com.leclowndu93150.thaumaturge.registry.TCAttachments;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.leclowndu93150.thaumaturge.registry.TCDataMaps;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

public final class WarpManager {
    private WarpManager() {}

    public static WarpData data(Player player) {
        return player.getData(TCAttachments.WARP);
    }

    public static void addWarp(ServerPlayer player, int amount, WarpType type) {
        WarpData data = data(player);
        if (amount < 0) {
            amount = Math.max(amount, -data.get(type));
        }
        if (amount == 0) {
            return;
        }
        data.add(type, amount);
        if (amount > 0) {
            data.setCounter(data.total());
            notifyGain(player, type);
            grantWarpResearch(player, type);
        } else {
            notifyLoss(player, type);
        }
        player.syncData(TCAttachments.WARP);
    }

    private static final Identifier WARP_RESEARCH = TCIds.rl("warp");
    private static final Identifier FIRST_STEPS_RESEARCH = TCIds.rl("first_steps");

    private static void grantWarpResearch(ServerPlayer player, WarpType type) {
        if (type == WarpType.TEMPORARY) {
            return;
        }
        IPlayerKnowledge knowledge = KnowledgeAccess.of(player);
        if (knowledge.isResearchComplete(FIRST_STEPS_RESEARCH) && !knowledge.isResearchComplete(WARP_RESEARCH)) {
            ResearchManager.complete(player, WARP_RESEARCH);
            sendActionBar(player, "research.thaumaturge.warp.warn");
        }
    }

    public static int getActualWarp(Player player) {
        IPlayerWarp warp = data(player);
        return warp.get(WarpType.PERMANENT) + warp.get(WarpType.NORMAL);
    }

    public static int getFinalWarp(ItemStack stack, LivingEntity wearer) {
        if (stack.isEmpty()) {
            return 0;
        }
        int warp = 0;
        if (stack.getItem() instanceof IWarpingGear gear) {
            warp += gear.getWarp(stack, wearer);
        }
        ItemWarp mapped = stack.getItem().builtInRegistryHolder().getData(TCDataMaps.ITEM_WARP);
        if (mapped != null) {
            warp += mapped.amount();
        }
        warp += stack.getOrDefault(TCDataComponents.STACK_WARP.get(), 0);
        return warp;
    }

    public static int getWarpFromGear(ServerPlayer player) {
        int warp = getFinalWarp(player.getMainHandItem(), player);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.isArmor()) {
                warp += getFinalWarp(player.getItemBySlot(slot), player);
            }
        }
        if (ModList.get().isLoaded(TCIds.CURIOS)) {
            for (ItemStack stack : ThaumaturgeCuriosCompat.equippedCurios(player)) {
                warp += getFinalWarp(stack, player);
            }
        }
        return warp;
    }

    private static void notifyGain(ServerPlayer player, WarpType type) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), TCSounds.WHISPERS.get(), SoundSource.AMBIENT, 0.5F, 1.0F);
        String key = switch (type) {
            case PERMANENT -> "warp.thaumaturge.gain.permanent";
            case NORMAL -> "warp.thaumaturge.gain.normal";
            case TEMPORARY -> "warp.thaumaturge.gain.temporary";
        };
        sendActionBar(player, key);
    }

    private static void notifyLoss(ServerPlayer player, WarpType type) {
        String key = switch (type) {
            case PERMANENT -> "warp.thaumaturge.lose.permanent";
            case NORMAL -> "warp.thaumaturge.lose.normal";
            case TEMPORARY -> "warp.thaumaturge.lose.temporary";
        };
        sendActionBar(player, key);
    }

    public static void sendActionBar(ServerPlayer player, String key) {
        player.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable(key).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC)));
    }

    public static final class Bindings implements WarpHelper.Bindings {
        @Override
        public IPlayerWarp getWarp(Player player) {
            return data(player);
        }

        @Override
        public void addWarp(ServerPlayer player, int amount, WarpType type) {
            WarpManager.addWarp(player, amount, type);
        }

        @Override
        public int getActualWarp(Player player) {
            return WarpManager.getActualWarp(player);
        }

        @Override
        public int getFinalWarp(ItemStack stack, LivingEntity wearer) {
            return WarpManager.getFinalWarp(stack, wearer);
        }
    }
}
