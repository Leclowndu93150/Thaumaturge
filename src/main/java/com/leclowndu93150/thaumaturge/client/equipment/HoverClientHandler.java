package com.leclowndu93150.thaumaturge.client.equipment;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.items.IHoverGear;
import com.leclowndu93150.thaumaturge.client.input.TTKeybinds;
import com.leclowndu93150.thaumaturge.content.equipment.hover.HoverManager;
import com.leclowndu93150.thaumaturge.content.particle.BoltParticleOptions;
import com.leclowndu93150.thaumaturge.network.ServerboundToggleHoverPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class HoverClientHandler {
    private static final double HOVER_DRAG = 0.7;
    private static final double BOLT_ORIGIN_HEIGHT = 1.17;
    private static final double BOLT_CROUCH_SINK = 0.075;
    private static final double BOLT_BACK_OFFSET = 0.5;
    private static final double BOLT_RANGE = 6.0;
    private static final float BOLT_YAW_BACK = 90.0F;
    private static final int BOLT_YAW_SPREAD = 180;
    private static final float BOLT_PITCH_MIN = -80.0F;
    private static final int BOLT_PITCH_SPREAD = 160;
    private static final int BOLT_SKIP_ODDS = 3;
    private static final float BOLT_RED = 0.75F;
    private static final float BOLT_GREEN = 1.0F;
    private static final float BOLT_BLUE = 1.0F;
    private static final float BOLT_WIDTH = 0.45F;
    private static final float RIGHT_ANGLE = 90.0F;

    private HoverClientHandler() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null) {
            return;
        }
        while (TTKeybinds.TOGGLE_HOVER.consumeClick()) {
            if (minecraft.screen == null && HoverManager.isWearingHoverGear(player)) {
                PacketDistributor.sendToServer(ServerboundToggleHoverPayload.INSTANCE);
            }
        }
        if (minecraft.isPaused()) {
            return;
        }
        boolean firstPerson = minecraft.options.getCameraType().isFirstPerson();
        RandomSource random = level.getRandom();
        for (Player hoverer : level.players()) {
            if (hoverer.isInvisible()
                    || hoverer == minecraft.getCameraEntity() && firstPerson
                    || !HoverManager.isHovering(hoverer)
                    || !HoverManager.isWearingHoverGear(hoverer)) {
                continue;
            }
            if (random.nextInt(BOLT_SKIP_ODDS) != 0) {
                discharge(level, hoverer, random);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LocalPlayer player)
                || player.isCreative()
                || player.isSpectator()
                || !HoverManager.isHovering(player)) {
            return;
        }
        ItemStack worn = HoverManager.wornHoverGear(player);
        if (!(worn.getItem() instanceof IHoverGear gear) || gear.getHoverFuel(worn) <= 0) {
            return;
        }
        Abilities abilities = player.getAbilities();
        if (!abilities.flying && !player.onGround()) {
            abilities.flying = true;
        }
        if (abilities.flying) {
            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.x * HOVER_DRAG, motion.y, motion.z * HOVER_DRAG);
        }
    }

    private static void discharge(ClientLevel level, Player hoverer, RandomSource random) {
        float bodyYaw = (hoverer.yBodyRot + RIGHT_ANGLE) * Mth.DEG_TO_RAD;
        double sink = hoverer.isCrouching() ? BOLT_CROUCH_SINK : 0.0;
        Vec3 origin = new Vec3(
                hoverer.getX() - Mth.cos(bodyYaw) * BOLT_BACK_OFFSET,
                hoverer.getY() + BOLT_ORIGIN_HEIGHT - sink,
                hoverer.getZ() - Mth.sin(bodyYaw) * BOLT_BACK_OFFSET);
        float yaw = hoverer.yBodyRot - BOLT_YAW_BACK - random.nextInt(BOLT_YAW_SPREAD);
        float pitch = BOLT_PITCH_MIN + random.nextInt(BOLT_PITCH_SPREAD);
        Vec3 end = origin.add(Vec3.directionFromRotation(pitch, yaw).scale(BOLT_RANGE));
        BlockHitResult hit =
                level.clip(new ClipContext(origin, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, hoverer));
        if (hit.getType() == HitResult.Type.BLOCK) {
            Vec3 target = hit.getLocation();
            level.addParticle(
                    new BoltParticleOptions(target.x, target.y, target.z, BOLT_RED, BOLT_GREEN, BOLT_BLUE, BOLT_WIDTH),
                    origin.x,
                    origin.y,
                    origin.z,
                    0.0,
                    0.0,
                    0.0);
        }
    }
}
