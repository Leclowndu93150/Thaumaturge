package com.leclowndu93150.thaumaturge.client.warding;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.casters.FocusPackage;
import com.leclowndu93150.thaumaturge.api.casters.FocusUnit;
import com.leclowndu93150.thaumaturge.api.casters.ICaster;
import com.leclowndu93150.thaumaturge.content.casters.ItemFocus;
import com.leclowndu93150.thaumaturge.content.focus.effect.FocusEffectWard;
import com.leclowndu93150.thaumaturge.content.warding.ClientWardHolder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class WardOverlayRenderer {
    private static final double RENDER_RANGE = 48.0;
    private static final double SECTION_RANGE = RENDER_RANGE + 16.0;

    private static final float OUTSET = 0.001F;
    private static final float SPAN = 1.0F + OUTSET + OUTSET;
    private static final float HALF = 0.5F;

    private static final float PULSE = 0.2F;
    private static final float RED_BASE = 0.8F;
    private static final float OWNED_GREEN_BASE = 0.7F;
    private static final float OWNED_BLUE_BASE = 0.28F;
    private static final float FOREIGN_CHANNEL_BASE = 0.28F;
    private static final float OWNED_ALPHA = 0.5F;
    private static final float FOREIGN_ALPHA = 0.25F;
    private static final float RED_PERIOD = 2.0F;
    private static final float GREEN_PERIOD = 3.0F;
    private static final float BLUE_PERIOD = 4.0F;

    private static final int[][] FACE_BASE = {{0, 0, 0}, {0, 1, 0}, {1, 1, 0}, {0, 1, 1}, {0, 1, 0}, {1, 1, 1}};
    private static final int[][] FACE_U = {{1, 0, 0}, {1, 0, 0}, {-1, 0, 0}, {1, 0, 0}, {0, 0, 1}, {0, 0, -1}};
    private static final int[][] FACE_V = {{0, 0, 1}, {0, 0, 1}, {0, -1, 0}, {0, -1, 0}, {0, -1, 0}, {0, -1, 0}};

    private static final float[] CORNER_U = {0.0F, HALF, 0.0F, HALF};
    private static final float[] CORNER_V = {0.0F, 0.0F, HALF, HALF};

    private static final RenderType RUNES = WardRenderType.ADDITIVE;

    private WardOverlayRenderer() {}

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (minecraft.level == null || player == null || minecraft.options.hideGui) {
            return;
        }
        if (!holdsWardFocus(player) || ClientWardHolder.sections().isEmpty()) {
            return;
        }
        Vec3 cam = minecraft.gameRenderer.getMainCamera().getPosition();
        float time = player.tickCount + minecraft.getTimer().getGameTimeDeltaPartialTick(false);
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer buffer = buffers.getBuffer(RUNES);
        Matrix4f pose = event.getPoseStack().last().pose();
        TextureAtlasSprite[] sprites =
                new TextureAtlasSprite[WardConnectedTexture.CORNERS * WardConnectedTexture.STATES];
        BlockPos.MutableBlockPos neighbour = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (SectionPos section : ClientWardHolder.sections()) {
            if (cam.distanceToSqr(section.center().getCenter()) > SECTION_RANGE * SECTION_RANGE) {
                continue;
            }
            for (BlockPos pos : ClientWardHolder.wardsIn(section)) {
                if (cam.distanceToSqr(Vec3.atCenterOf(pos)) > RENDER_RANGE * RENDER_RANGE) {
                    continue;
                }
                drawWard(buffer, pose, sprites, neighbour, cursor, pos, cam, time);
            }
        }
        buffers.endBatch(RUNES);
    }

    private static void drawWard(
            VertexConsumer buffer,
            Matrix4f pose,
            TextureAtlasSprite[] sprites,
            BlockPos.MutableBlockPos neighbour,
            BlockPos.MutableBlockPos cursor,
            BlockPos pos,
            Vec3 cam,
            float time) {
        boolean owned = ClientWardHolder.isOwned(pos);
        float red = Math.min(Mth.sin(time / RED_PERIOD + pos.getX()) * PULSE + RED_BASE, 1.0F);
        float green = Math.min(
                Mth.sin(time / GREEN_PERIOD + pos.getY()) * PULSE + (owned ? OWNED_GREEN_BASE : FOREIGN_CHANNEL_BASE),
                1.0F);
        float blue = Math.min(
                Mth.sin(time / BLUE_PERIOD + pos.getZ()) * PULSE + (owned ? OWNED_BLUE_BASE : FOREIGN_CHANNEL_BASE),
                1.0F);
        float alpha = owned ? OWNED_ALPHA : FOREIGN_ALPHA;
        Predicate<BlockPos> connected =
                probe -> ClientWardHolder.isWarded(probe) && ClientWardHolder.isOwned(probe) == owned;
        for (Direction face : Direction.values()) {
            neighbour.setWithOffset(pos, face);
            if (ClientWardHolder.isWarded(neighbour)) {
                continue;
            }
            for (int corner = 0; corner < WardConnectedTexture.CORNERS; corner++) {
                int state = WardConnectedTexture.stateFor(pos, face, corner, cursor, connected);
                int slot = corner * WardConnectedTexture.STATES + state;
                TextureAtlasSprite sprite = sprites[slot];
                if (sprite == null) {
                    sprite = Minecraft.getInstance()
                            .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                            .apply(WardConnectedTexture.sprite(corner, state));
                    sprites[slot] = sprite;
                }
                drawCorner(buffer, pose, pos, face, corner, sprite, cam, red, green, blue, alpha);
            }
        }
    }

    private static void drawCorner(
            VertexConsumer buffer,
            Matrix4f pose,
            BlockPos pos,
            Direction face,
            int corner,
            TextureAtlasSprite sprite,
            Vec3 cam,
            float red,
            float green,
            float blue,
            float alpha) {
        int index = face.ordinal();
        int[] base = FACE_BASE[index];
        int[] uAxis = FACE_U[index];
        int[] vAxis = FACE_V[index];
        float originX = (float) (pos.getX() - cam.x) + edge(base[0]);
        float originY = (float) (pos.getY() - cam.y) + edge(base[1]);
        float originZ = (float) (pos.getZ() - cam.z) + edge(base[2]);
        float u0 = CORNER_U[corner];
        float v0 = CORNER_V[corner];
        float u1 = u0 + HALF;
        float v1 = v0 + HALF;
        vertex(
                buffer,
                pose,
                originX,
                originY,
                originZ,
                uAxis,
                vAxis,
                u0,
                v0,
                sprite.getU0(),
                sprite.getV0(),
                red,
                green,
                blue,
                alpha);
        vertex(
                buffer,
                pose,
                originX,
                originY,
                originZ,
                uAxis,
                vAxis,
                u1,
                v0,
                sprite.getU1(),
                sprite.getV0(),
                red,
                green,
                blue,
                alpha);
        vertex(
                buffer,
                pose,
                originX,
                originY,
                originZ,
                uAxis,
                vAxis,
                u1,
                v1,
                sprite.getU1(),
                sprite.getV1(),
                red,
                green,
                blue,
                alpha);
        vertex(
                buffer,
                pose,
                originX,
                originY,
                originZ,
                uAxis,
                vAxis,
                u0,
                v1,
                sprite.getU0(),
                sprite.getV1(),
                red,
                green,
                blue,
                alpha);
    }

    private static void vertex(
            VertexConsumer buffer,
            Matrix4f pose,
            float originX,
            float originY,
            float originZ,
            int[] uAxis,
            int[] vAxis,
            float u,
            float v,
            float spriteU,
            float spriteV,
            float red,
            float green,
            float blue,
            float alpha) {
        float x = originX + (uAxis[0] * u + vAxis[0] * v) * SPAN;
        float y = originY + (uAxis[1] * u + vAxis[1] * v) * SPAN;
        float z = originZ + (uAxis[2] * u + vAxis[2] * v) * SPAN;
        buffer.addVertex(pose, x, y, z).setUv(spriteU, spriteV).setColor(red, green, blue, alpha);
    }

    private static float edge(int flag) {
        return flag == 0 ? -OUTSET : 1.0F + OUTSET;
    }

    private static boolean holdsWardFocus(LocalPlayer player) {
        return hasWard(player.getMainHandItem()) || hasWard(player.getOffhandItem());
    }

    private static boolean hasWard(ItemStack stack) {
        if (!(stack.getItem() instanceof ICaster caster)) {
            return false;
        }
        FocusPackage core = ItemFocus.getPackage(caster.getFocusStack(stack));
        return core != null && containsWard(core);
    }

    private static boolean containsWard(FocusPackage core) {
        for (FocusUnit unit : core.units()) {
            if (FocusEffectWard.ID.equals(unit.element())) {
                return true;
            }
            for (FocusPackage branch : unit.branches()) {
                if (containsWard(branch)) {
                    return true;
                }
            }
        }
        return false;
    }
}
