package com.leclowndu93150.thaumaturge.client.casters;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.content.wands.ItemWand;
import com.leclowndu93150.thaumaturge.content.wands.WandParts;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class WandTipTracker {
    private static final long STALE_MS = 250L;
    private static final Map<InteractionHand, Sample> SAMPLES = new EnumMap<>(InteractionHand.class);
    private static @Nullable Matrix4f inverseWorldProjection;
    private static long frame;

    private WandTipTracker() {}

    @SubscribeEvent
    public static void beginFrame(RenderFrameEvent.Pre event) {
        frame++;
    }

    public static void setWorldProjection(Matrix4fc projection) {
        inverseWorldProjection = projection.invert(new Matrix4f());
    }

    public static void capture(
            LocalPlayer player, InteractionHand hand, ItemStack stack, PoseStack poseStack, float tipModelY) {
        // The hand model-view cancels the inverse view at the base of the hand pose stack.
        Vector3f tip = new Matrix4f(RenderSystem.getProjectionMatrix())
                .mul(RenderSystem.getModelViewMatrix())
                .mul(poseStack.last().pose())
                .transformProject(new Vector3f(0.0F, tipModelY, 0.0F));
        WandSource wand = wandSource(player, hand, stack);
        if (wand != null && tip.isFinite()) {
            SAMPLES.put(hand, new Sample(wand, tip, frame, Util.getMillis()));
        }
    }

    public static @Nullable Vec3 firstPersonTip() {
        LocalPlayer player = Minecraft.getInstance().player;
        WandSource wand = player == null ? null : heldWand(player);
        return wand == null ? null : wand.tip();
    }

    public static @Nullable WandSource heldWand(LocalPlayer player) {
        if (player.isUsingItem()) {
            return heldWand(player, player.getUsedItemHand());
        }
        WandSource main = heldWand(player, InteractionHand.MAIN_HAND);
        WandSource off = heldWand(player, InteractionHand.OFF_HAND);
        return main == null ? off : off == null ? main : null;
    }

    public static @Nullable WandSource heldWand(LocalPlayer player, InteractionHand hand) {
        return wandSource(player, hand, player.getItemInHand(hand));
    }

    private static @Nullable WandSource wandSource(LocalPlayer player, InteractionHand hand, ItemStack stack) {
        if (!(stack.getItem() instanceof ItemWand wand)) {
            return null;
        }
        int slot = hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : -1;
        return new WandSource(
                player, hand, slot, WandVisHelper.getParts(stack), Spells.spellOf(wand.socketedFocus(stack)));
    }

    public record WandSource(
            LocalPlayer player,
            InteractionHand hand,
            int slot,
            WandParts parts,
            @Nullable Spell spell) {
        public @Nullable Vec3 tip() {
            Minecraft mc = Minecraft.getInstance();
            Camera camera = mc.gameRenderer.getMainCamera();
            Sample sample = SAMPLES.get(hand);
            if (mc.player != player
                    || camera.getEntity() != player
                    || !mc.options.getCameraType().isFirstPerson()
                    || mc.options.hideGui
                    || player.isSpectator()
                    || player.isSleeping()
                    || !equals(heldWand(player, hand))
                    || inverseWorldProjection == null
                    || sample == null
                    || !sample.wand().equals(this)
                    || frame - sample.frame() > 1
                    || Util.getMillis() - sample.capturedAt() > STALE_MS) {
                return null;
            }
            // World effects draw before the hand. Reproject its latest screen point with this frame's camera.
            Vector3f offset = unproject(inverseWorldProjection, camera.rotation(), sample.projectedTip());
            return offset.isFinite() ? camera.getPosition().add(offset.x, offset.y, offset.z) : null;
        }
    }

    static Vector3f unproject(Matrix4fc inverseProjection, Quaternionfc cameraRotation, Vector3f projectedTip) {
        return inverseProjection.transformProject(projectedTip, new Vector3f()).rotate(cameraRotation);
    }

    private record Sample(WandSource wand, Vector3f projectedTip, long frame, long capturedAt) {}
}
