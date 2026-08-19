package com.leclowndu93150.thaumaturge.content.research.scan;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.research.scan.IScanThing;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanKeys;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanningManager;
import com.leclowndu93150.thaumaturge.content.item.CelestialBody;
import com.leclowndu93150.thaumaturge.content.item.CelestialNotesItem;
import com.leclowndu93150.thaumaturge.content.research.PlayerKnowledge;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class ScanSky implements IScanThing {
    private static final int TICKS_PER_DAY = 24000;
    private static final int YAW_TOLERANCE = 10;
    private static final int PITCH_TOLERANCE = 7;
    private static final Identifier NODES_RESEARCH = TCIds.rl("nodes");

    @Override
    public boolean checkThing(Player player, @Nullable Object target) {
        if (target != null || !isLookingSkyward(player)) {
            return false;
        }
        SkyAngles angles = SkyAngles.of(player);
        return angles.onBody() || angles.night();
    }

    @Override
    public void onSuccess(Player player, @Nullable Object target) {
        if (target != null || !(player instanceof ServerPlayer serverPlayer) || !isLookingSkyward(player)) {
            return;
        }
        SkyAngles angles = SkyAngles.of(player);
        int worldDay = (int) (player.level().getGameTime() / TICKS_PER_DAY);
        if (angles.onBody()) {
            int moonPhase = player.level().environmentAttributes().getValue(EnvironmentAttributes.MOON_PHASE, player.position()).index();
            String body = angles.night() ? "moon" + moonPhase : "sun";
            CelestialBody note = angles.night() ? CelestialBody.moon(moonPhase) : CelestialBody.SUN;
            observe(serverPlayer, worldDay, body, note);
        } else if (angles.night()) {
            Direction facing = player.getDirection();
            int num = facing.get3DDataValue() - 2;
            observe(serverPlayer, worldDay, "star" + num, CelestialBody.star(num));
        }
    }

    @Override
    public @Nullable Identifier getResearchKey(Player player, @Nullable Object target) {
        return null;
    }

    private static void observe(ServerPlayer player, int worldDay, String body, CelestialBody note) {
        Identifier key = ScanKeys.celestial(worldDay, body);
        if (KnowledgeAccess.of(player).isResearchKnown(key)) {
            player.sendOverlayMessage(Component.translatable("tc.celestial.fail.1"));
            return;
        }
        if (isCarrying(player, TCItems.SCRIBING_TOOLS.get()) && consume(player, Items.PAPER)) {
            ItemStack stack = CelestialNotesItem.stackOf(note);
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
            ScanningManager.progressResearch(player, key);
        } else {
            player.sendOverlayMessage(Component.translatable("tc.celestial.fail.2"));
        }
        cleanResearch(player, worldDay);
    }

    private static void cleanResearch(ServerPlayer player, int worldDay) {
        IPlayerKnowledge knowledge = KnowledgeAccess.of(player);
        String dayPrefix = ScanKeys.celestialDayPrefix(worldDay);
        List<Identifier> stale = new ArrayList<>();
        for (Identifier key : knowledge.researchList()) {
            if (ScanKeys.isCelestial(key) && !key.getPath().startsWith(dayPrefix)) {
                stale.add(key);
            }
        }
        for (Identifier key : stale) {
            knowledge.removeResearch(key);
        }
        if (!stale.isEmpty() && knowledge instanceof PlayerKnowledge concrete) {
            concrete.sync(player);
        }
    }

    private static boolean isLookingSkyward(Player player) {
        return !(player.getXRot() > 0.0F) && player.level().canSeeSky(player.blockPosition().above()) && player.level().dimension() == Level.OVERWORLD
                && KnowledgeAccess.of(player).isResearchComplete(NODES_RESEARCH);
    }

    private static boolean isCarrying(Player player, Item item) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (player.getInventory().getItem(slot).is(item)) {
                return true;
            }
        }
        return false;
    }

    private static boolean consume(Player player, Item item) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(item)) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    private record SkyAngles(boolean night, boolean onBody) {
        static SkyAngles of(Player player) {
            int yaw = (int) (player.getYRot() + 90.0F) % 360;
            int pitch = (int) Math.abs(player.getXRot());
            float sunAngle = player.level().environmentAttributes().getValue(EnvironmentAttributes.SUN_ANGLE, player.position());
            int celestialAngle = (int) (sunAngle + 90.0F) % 360;
            boolean night = celestialAngle > 180;
            if (night) {
                celestialAngle -= 180;
            }
            boolean inRangeYaw;
            boolean inRangePitch;
            if (celestialAngle > 90) {
                inRangeYaw = Math.abs(Math.abs(yaw) - 180) < YAW_TOLERANCE;
                inRangePitch = Math.abs(180 - celestialAngle - pitch) < PITCH_TOLERANCE;
            } else {
                inRangeYaw = Math.abs(yaw) < YAW_TOLERANCE;
                inRangePitch = Math.abs(celestialAngle - pitch) < PITCH_TOLERANCE;
            }
            return new SkyAngles(night, inRangeYaw && inRangePitch);
        }
    }
}
