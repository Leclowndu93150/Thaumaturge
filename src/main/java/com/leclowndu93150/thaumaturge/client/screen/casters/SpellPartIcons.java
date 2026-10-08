package com.leclowndu93150.thaumaturge.client.screen.casters;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPartKind;
import com.leclowndu93150.thaumaturge.client.render.GuiBlend;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;

public final class SpellPartIcons {
    public static final ResourceLocation ROUND_BACK = TTIds.rl("textures/foci/_medium.png");
    public static final ResourceLocation DIAMOND_BACK = TTIds.rl("textures/foci/_effect.png");
    private static final int GLYPH_TEXTURE = 32;
    private static final int OPAQUE = 0xFF;

    private SpellPartIcons() {}

    public static void draw(
            GuiGraphics graphics,
            HolderLookup.Provider registries,
            SpellNode node,
            int centreX,
            int centreY,
            int back,
            int glyph,
            int flowGlyph,
            float alpha) {
        Optional<SpellPart> part = Spells.part(registries, node.part());
        if (part.isPresent()) {
            draw(graphics, part.get(), Spells.color(registries, node), centreX, centreY, back, glyph, flowGlyph, alpha);
        }
    }

    public static void draw(
            GuiGraphics graphics,
            HolderLookup.Provider registries,
            ResourceKey<SpellPart> key,
            int centreX,
            int centreY,
            int back,
            int glyph,
            int flowGlyph,
            float alpha) {
        Optional<SpellPart> part = Spells.part(registries, key);
        if (part.isPresent()) {
            draw(
                    graphics,
                    part.get(),
                    Spells.color(registries, SpellNode.of(key)),
                    centreX,
                    centreY,
                    back,
                    glyph,
                    flowGlyph,
                    alpha);
        }
    }

    public static void draw(
            GuiGraphics graphics,
            SpellPart part,
            int color,
            int centreX,
            int centreY,
            int back,
            int glyph,
            int flowGlyph,
            float alpha) {
        int alphaByte = Math.round(alpha * OPAQUE);
        if (part.kind() == SpellPartKind.FLOW) {
            centred(graphics, part.icon(), centreX, centreY, flowGlyph, ARGB32.color(alphaByte, 0xFFFFFF));
            return;
        }
        ResourceLocation backplate = part.kind() == SpellPartKind.EFFECT ? DIAMOND_BACK : ROUND_BACK;
        centred(graphics, backplate, centreX, centreY, back, ARGB32.color(alphaByte, color));
        centred(graphics, part.icon(), centreX, centreY, glyph, ARGB32.color(alphaByte, 0xFFFFFF));
    }

    private static void centred(
            GuiGraphics graphics, ResourceLocation texture, int centreX, int centreY, int size, int color) {
        int half = size / 2;
        GuiBlend.blitTinted(
                graphics,
                texture,
                centreX - half,
                centreY - half,
                size,
                size,
                0.0F,
                0.0F,
                GLYPH_TEXTURE,
                GLYPH_TEXTURE,
                GLYPH_TEXTURE,
                GLYPH_TEXTURE,
                color);
    }
}
