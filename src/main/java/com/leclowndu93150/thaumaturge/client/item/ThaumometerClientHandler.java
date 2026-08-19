package com.leclowndu93150.thaumaturge.client.item;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanningManager;
import com.leclowndu93150.thaumaturge.client.effect.ClientEffects;
import com.leclowndu93150.thaumaturge.content.item.ThaumometerItem;
import com.leclowndu93150.thaumaturge.content.research.scan.ScanRaycastHelper;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class ThaumometerClientHandler {
    private static final int HIGHLIGHT_INTERVAL_TICKS = 5;
    private static final double HIGHLIGHT_ENTITY_RANGE = 16.0;
    private static final float HIGHLIGHT_ENTITY_PADDING = 5.0F;
    private static final double WILD_RAY_RANGE = 16.0;
    private static final int WILD_RAY_ANGLE_SPREAD = 25;
    private static final int SCAN_RUNE_BURSTS = 10;
    private static final float RUNE_ENTITY_HEIGHT_SCALE = 15.0F;
    private static final int RUNE_BLOCK_DURATION = 15;
    private static final float RUNE_GRAVITY = 0.03F;
    private static final float SCAN_VOLUME = 0.5F;
    private static final float SCAN_PITCH = 1.0F;

    private static final int SCAN_TICK_SOUND_INTERVAL = 2;
    private static final float SCAN_TICK_VOLUME = 0.2F;
    private static final float SCAN_TICK_PITCH_BASE = 0.45F;
    private static final float SCAN_TICK_PITCH_SPREAD = 0.1F;

    private static @Nullable String scanTargetKey;

    private ThaumometerClientHandler() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || mc.isPaused()) {
            return;
        }
        tickScanning(mc, player);
        boolean held = player.getMainHandItem().is(TCItems.THAUMOMETER.get()) || player.getOffhandItem().is(TCItems.THAUMOMETER.get());
        if (!held) {
            return;
        }
        if (player.tickCount % HIGHLIGHT_INTERVAL_TICKS != 0) {
            return;
        }
        HitResult hitResult = ScanRaycastHelper.performRaycast(player, ClipContext.Fluid.SOURCE_ONLY);
        // Here we use the Type instead of an instanceof because, a miss is an instance of BlockHitResult
        if (hitResult.getType() == HitResult.Type.BLOCK && ScanningManager.isThingStillScannable(player, ((BlockHitResult) hitResult).getBlockPos())) {
            ClientEffects.scanHighlight(mc.level, ((BlockHitResult) hitResult).getBlockPos());
        }
        if (hitResult instanceof EntityHitResult result && ScanningManager.isThingStillScannable(player, result.getEntity())) {
            ClientEffects.scanHighlight(result.getEntity());
        }
    }

    private static void tickScanning(Minecraft mc, LocalPlayer player) {
        boolean scanning = player.isUsingItem() && player.getUseItem().is(TCItems.THAUMOMETER.get());
        if (!scanning) {
            scanTargetKey = null;
            return;
        }
        Level level = player.level();
        int elapsed = player.getTicksUsingItem();
        Object target = ThaumometerItem.resolveTarget(level, player);
        if (!ScanningManager.isThingStillScannable(player, target)) {
            player.stopUsingItem();
            scanTargetKey = null;
            return;
        }
        String key = keyOf(target);
        if (elapsed <= 1) {
            scanTargetKey = key;
        } else if (!key.equals(scanTargetKey)) {
            player.stopUsingItem();
            scanTargetKey = null;
            return;
        }
        if (elapsed % SCAN_TICK_SOUND_INTERVAL == 0) {
            player.level().playLocalSound(player.getX(), player.getY(), player.getZ(), TCSounds.CAMERA_TICKS.get(), SoundSource.PLAYERS, SCAN_TICK_VOLUME,
                    SCAN_TICK_PITCH_BASE + level.getRandom().nextFloat() * SCAN_TICK_PITCH_SPREAD, false);
            drawScanTickFx(level, target);
        }
        if (elapsed >= ThaumometerItem.SCAN_COMPLETE_ELAPSED_TICKS) {
            player.stopUsingItem();
            scanTargetKey = null;
        }
    }

    private static String keyOf(@Nullable Object target) {
        if (target instanceof Entity entity) {
            return "e:" + entity.getId();
        }
        if (target instanceof BlockPos pos) {
            return "b:" + pos.asLong();
        }
        return "none";
    }

    private static void drawScanTickFx(Level level, @Nullable Object target) {
        RandomSource rand = level.getRandom();
        if (target instanceof Entity entity) {
            ClientEffects.blockRunes(level, entity.getX() - 0.5, entity.getY() + entity.getEyeHeight() / 2.0F, entity.getZ() - 0.5, 0.3F + rand.nextFloat() * 0.7F, 0.0F,
                    0.3F + rand.nextFloat() * 0.7F, (int) (entity.getBbHeight() * RUNE_ENTITY_HEIGHT_SCALE), RUNE_GRAVITY);
        } else if (target instanceof BlockPos pos) {
            ClientEffects.blockRunes(level, pos.getX(), pos.getY() + 0.25, pos.getZ(), 0.3F + rand.nextFloat() * 0.7F, 0.0F, 0.3F + rand.nextFloat() * 0.7F, RUNE_BLOCK_DURATION, RUNE_GRAVITY);
        }
    }

    private static BlockHitResult wildBlockRay(Level level, Player player) {
        RandomSource rand = level.getRandom();
        float pitch = player.getXRot() + rand.nextInt(WILD_RAY_ANGLE_SPREAD) - rand.nextInt(WILD_RAY_ANGLE_SPREAD);
        float yaw = player.getYRot() + rand.nextInt(WILD_RAY_ANGLE_SPREAD) - rand.nextInt(WILD_RAY_ANGLE_SPREAD);
        Vec3 from = player.getEyePosition();
        Vec3 direction = Vec3.directionFromRotation(pitch, yaw);
        Vec3 to = from.add(direction.scale(WILD_RAY_RANGE));
        return level.clip(new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.SOURCE_ONLY, player));
    }
}
