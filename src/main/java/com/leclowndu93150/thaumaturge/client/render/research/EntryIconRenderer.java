package com.leclowndu93150.thaumaturge.client.render.research;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.casters.FocusEffect;
import com.leclowndu93150.thaumaturge.api.casters.FocusElement;
import com.leclowndu93150.thaumaturge.api.casters.FocusEngine;
import com.leclowndu93150.thaumaturge.api.casters.FocusMedium;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.api.research.IResearchStage;
import com.leclowndu93150.thaumaturge.api.research.ResearchEntryMeta;
import com.leclowndu93150.thaumaturge.api.research.ResearchIcon;
import com.leclowndu93150.thaumaturge.api.research.ResearchRequirement;
import com.leclowndu93150.thaumaturge.client.effect.pipeline.TCRenderPipelines;
import com.leclowndu93150.thaumaturge.client.screen.TCScreenTextures;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class EntryIconRenderer {
    public static final int ICON_REFERENCE_SIZE = 16;
    public static final int HIT_PADDING = 2;

    public static final Identifier NODE_TEXTURE = Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/misc/auranodes.png");
    private static final int NODE_TEXTURE_SIZE = 2048;
    private static final int NODE_GRID = 32;
    private static final int NODE_FRAME_SIZE = NODE_TEXTURE_SIZE / NODE_GRID;
    private static final int NODE_BASE_FRAME = 160;
    private static final int NODE_FRAME_COUNT = 32;
    private static final float NODE_VISIBLE_SCALE = 90.0F;
    private static final int NODE_TINT = 0xE5540070;

    private static final int FRAME_SIZE = 32;
    private static final int FRAME_OFFSET = 8;

    private static final int FRAME_NORMAL_U = 80;
    private static final int FRAME_HEX_U = 112;
    private static final int FRAME_ROUND_U = 144;
    private static final int FRAME_SPIKY_U = 176;
    private static final int FRAME_VISIBLE_V = 48;
    private static final int FRAME_HIDDEN_V = 80;

    private static final int FLAG_BADGE_SIZE = 32;
    private static final float FLAG_BADGE_SCALE = 0.5F;
    private static final int FLAG_BADGE_OFFSET_X = -9;
    private static final int FLAG_BADGE_TOP_OFFSET_Y = -9;
    private static final int FLAG_BADGE_BOTTOM_OFFSET_Y = 9;
    private static final int FLAG_RESEARCH_U = 176;
    private static final int FLAG_PAGE_U = 208;
    private static final int FLAG_BADGE_V = 16;

    private static final int COLOR_COMPLETE = 0xFFFFFFFF;
    private static final int COLOR_UNKNOWN_LOCKED_FRAME = 0xFF4D4D4D;
    private static final int COLOR_UNKNOWN_LOCKED_ICON = 0xFF333333;
    private static final int LOCKED_ICON_OVERLAY = 0x7F000000;
    private static final int ICON_TEX_SIZE = 16;
    private static final long FLIPBOOK_FRAME_MS = 150L;

    private static final Identifier FOCUS_EFFECT_BACK = Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/foci/_effect.png");
    private static final Identifier FOCUS_MEDIUM_BACK = Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/foci/_medium.png");
    private static final float FOCUS_PART_SCALE = 24.0F;
    private static final int FOCUS_BACK_ALPHA = 220;
    private static final int FOCUS_GLYPH_ALPHA = 220;
    private static final int FOCUS_GLYPH_LOCKED_ALPHA = 50;

    private static final long PULSE_PERIOD_MS = 600L;
    private static final float PULSE_AMPLITUDE = 0.25F;
    private static final float PULSE_OFFSET = 0.75F;

    public enum Status {
        UNKNOWN, IN_PROGRESS, COMPLETE
    }

    private EntryIconRenderer() {}

    public static int colorForStatus(Status status) {
        return switch (status) {
            case COMPLETE -> COLOR_COMPLETE;
            case IN_PROGRESS -> pulseColor();
            case UNKNOWN -> COLOR_UNKNOWN_LOCKED_FRAME;
        };
    }

    public static int pulseColor() {
        double phase = (System.currentTimeMillis() % PULSE_PERIOD_MS) / (double) PULSE_PERIOD_MS;
        float v = (float) (Math.sin(phase * Math.PI * 2.0) * PULSE_AMPLITUDE + PULSE_OFFSET);
        int channel = Math.max(0, Math.min(255, Math.round(v * 255.0F)));
        return 0xFF000000 | (channel << 16) | (channel << 8) | channel;
    }

    public static void render(GuiGraphicsExtractor graphics, int iconX, int iconY, IResearchEntry entry, Status status, ItemStack icon, boolean hasWarp, boolean hasNewResearchFlag, boolean hasNewPageFlag, int tickCount) {
        render(graphics, iconX, iconY, entry, status, (Object) icon, hasWarp, hasNewResearchFlag, hasNewPageFlag);
    }

    public static void render(GuiGraphicsExtractor graphics, int iconX, int iconY, IResearchEntry entry, Status status, Object icon, boolean hasWarp, boolean hasNewResearchFlag, boolean hasNewPageFlag) {
        Set<ResearchEntryMeta> meta = entry.meta();
        if (hasWarp) {
            drawForbidden(graphics, iconX + 8, iconY + 8);
        }
        int frameColor = colorForStatus(status);
        int frameV = meta.contains(ResearchEntryMeta.HIDDEN) ? FRAME_HIDDEN_V : FRAME_VISIBLE_V;
        int baseU;
        if (meta.contains(ResearchEntryMeta.ROUND)) {
            baseU = FRAME_ROUND_U;
        } else if (meta.contains(ResearchEntryMeta.HEX)) {
            baseU = FRAME_HEX_U;
        } else {
            baseU = FRAME_NORMAL_U;
        }
        blitFrame(graphics, iconX - FRAME_OFFSET, iconY - FRAME_OFFSET, baseU, frameV, frameColor);
        if (meta.contains(ResearchEntryMeta.SPIKY)) {
            blitFrame(graphics, iconX - FRAME_OFFSET, iconY - FRAME_OFFSET, FRAME_SPIKY_U, frameV, frameColor);
        }
        boolean lockedIcon = status == Status.UNKNOWN;
        drawResearchIcon(graphics, iconX, iconY, icon, lockedIcon);
        if (hasNewResearchFlag) {
            drawBadge(graphics, iconX + FLAG_BADGE_OFFSET_X, iconY + FLAG_BADGE_TOP_OFFSET_Y, FLAG_RESEARCH_U);
        }
        if (hasNewPageFlag) {
            drawBadge(graphics, iconX + FLAG_BADGE_OFFSET_X, iconY + FLAG_BADGE_BOTTOM_OFFSET_Y, FLAG_PAGE_U);
        }
    }

    public static Object resolveIcon(IResearchEntry entry, long ticks) {
        List<ResearchIcon> icons = entry.icons();
        if (!icons.isEmpty()) {
            ResearchIcon icon = icons.get((int) (ticks / 20L % icons.size()));
            if (icon.kind() == ResearchIcon.Kind.FOCUS) {
                return new FocusIcon(icon.id());
            }
            if (icon.texture()) {
                return icon.id();
            }
            return new ItemStack(BuiltInRegistries.ITEM.getValue(icon.id()));
        }
        for (IResearchStage stage : entry.stages()) {
            for (List<ResearchRequirement> reqs : List.of(stage.obtain(), stage.craft())) {
                if (reqs.isEmpty()) {
                    continue;
                }
                List<Holder<Item>> items = reqs.get(0).items().stream().toList();
                if (!items.isEmpty()) {
                    return new ItemStack(items.get((int) (ticks / 20L % items.size())));
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public static void drawResearchIcon(GuiGraphicsExtractor graphics, int iconX, int iconY, Object icon, boolean locked) {
        if (icon instanceof ItemStack stack) {
            if (stack.isEmpty())
                return;
            graphics.item(stack, iconX, iconY);
            if (locked) {
                graphics.fill(iconX, iconY, iconX + ICON_TEX_SIZE, iconY + ICON_TEX_SIZE, LOCKED_ICON_OVERLAY);
            }
            return;
        }
        if (icon instanceof FocusIcon focus) {
            drawFocusIcon(graphics, iconX + ICON_TEX_SIZE / 2, iconY + ICON_TEX_SIZE / 2, focus.elementId(), locked);
            return;
        }
        if (icon instanceof Identifier texture) {
            drawTextureIcon(graphics, iconX, iconY, texture, locked);
        }
    }

    public record FocusIcon(Identifier elementId) {
    }

    public static void drawFocusIcon(GuiGraphicsExtractor graphics, int centerX, int centerY, Identifier elementId, boolean locked) {
        FocusElement element = FocusEngine.element(elementId);
        if (element == null) {
            return;
        }
        int color = (FOCUS_BACK_ALPHA << 24) | (FocusEngine.color(elementId) & 0x00FFFFFF);
        if (element instanceof FocusEffect) {
            blitCentered(graphics, FOCUS_EFFECT_BACK, centerX, centerY, Math.round(FOCUS_PART_SCALE * 0.9F), color);
        } else if (element instanceof FocusMedium) {
            blitCentered(graphics, FOCUS_MEDIUM_BACK, centerX, centerY, Math.round(FOCUS_PART_SCALE * 0.9F), color);
        }
        int glyphAlpha = locked ? FOCUS_GLYPH_LOCKED_ALPHA : FOCUS_GLYPH_ALPHA;
        Identifier glyph = FocusEngine.icon(elementId);
        if (glyph != null) {
            blitCentered(graphics, glyph, centerX, centerY, Math.round(FOCUS_PART_SCALE / 2.0F), (glyphAlpha << 24) | 0x00FFFFFF);
        }
    }

    private static void blitCentered(GuiGraphicsExtractor graphics, Identifier texture, int cx, int cy, int size, int color) {
        int half = size / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, cx - half, cy - half, 0.0F, 0.0F, size, size, 32, 32, 32, 32, color);
    }

    private static void drawTextureIcon(GuiGraphicsExtractor graphics, int iconX, int iconY, Identifier texture, boolean locked) {
        Minecraft mc = Minecraft.getInstance();
        AbstractTexture tex = mc.getTextureManager().getTexture(texture);
        int tint = locked ? COLOR_UNKNOWN_LOCKED_ICON : COLOR_COMPLETE;
        int w;
        int h;
        try {
            w = tex.getTexture().getWidth(0);
            h = tex.getTexture().getHeight(0);
        } catch (IllegalStateException unInit) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, texture, iconX, iconY, 0.0F, 0.0F, ICON_TEX_SIZE, ICON_TEX_SIZE, ICON_TEX_SIZE, ICON_TEX_SIZE, ICON_TEX_SIZE, ICON_TEX_SIZE, tint);
            return;
        }
        if (h > w && h % w == 0) {
            int frames = h / w;
            int frameIdx = (int) (System.currentTimeMillis() / FLIPBOOK_FRAME_MS % frames);
            float v = (float) frameIdx * (float) w;
            graphics.blit(RenderPipelines.GUI_TEXTURED, texture, iconX, iconY, 0.0F, v, ICON_TEX_SIZE, ICON_TEX_SIZE, w, w, w, h, tint);
        } else if (w > h && w % h == 0) {
            int frames = w / h;
            int frameIdx = (int) (System.currentTimeMillis() / FLIPBOOK_FRAME_MS % frames);
            float u = (float) frameIdx * (float) h;
            graphics.blit(RenderPipelines.GUI_TEXTURED, texture, iconX, iconY, u, 0.0F, ICON_TEX_SIZE, ICON_TEX_SIZE, h, h, w, h, tint);
        } else {
            graphics.blit(RenderPipelines.GUI_TEXTURED, texture, iconX, iconY, 0.0F, 0.0F, ICON_TEX_SIZE, ICON_TEX_SIZE, w, h, w, h, tint);
        }
    }

    private static void drawBadge(GuiGraphicsExtractor graphics, int x, int y, int u) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(FLAG_BADGE_SCALE, FLAG_BADGE_SCALE);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BROWSER, 0, 0, (float) u, (float) FLAG_BADGE_V, FLAG_BADGE_SIZE, FLAG_BADGE_SIZE, FLAG_BADGE_SIZE, FLAG_BADGE_SIZE,
                TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE);
        graphics.pose().popMatrix();
    }

    public static void drawForbidden(GuiGraphicsExtractor graphics, int centerX, int centerY) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null)
            return;
        int count = mc.player.tickCount;
        int frame = NODE_BASE_FRAME + count % NODE_FRAME_COUNT;
        int frameCol = frame % NODE_GRID;
        int frameRow = frame / NODE_GRID;
        int u = frameCol * NODE_FRAME_SIZE;
        int v = frameRow * NODE_FRAME_SIZE;
        int half = Math.round(NODE_VISIBLE_SCALE * 0.5F);
        graphics.pose().pushMatrix();
        graphics.pose().translate(centerX, centerY);
        graphics.blit(TCRenderPipelines.GUI_TEXTURED_ADDITIVE, NODE_TEXTURE, -half, -half, (float) u, (float) v, Math.round(NODE_VISIBLE_SCALE), Math.round(NODE_VISIBLE_SCALE), NODE_FRAME_SIZE,
                NODE_FRAME_SIZE, NODE_TEXTURE_SIZE, NODE_TEXTURE_SIZE, NODE_TINT);
        graphics.pose().popMatrix();
    }

    private static void blitFrame(GuiGraphicsExtractor graphics, int x, int y, int u, int v, int color) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BROWSER, x, y, (float) u, (float) v, FRAME_SIZE, FRAME_SIZE, FRAME_SIZE, FRAME_SIZE, TCScreenTextures.TEX_SIZE,
                TCScreenTextures.TEX_SIZE, color);
    }
}
