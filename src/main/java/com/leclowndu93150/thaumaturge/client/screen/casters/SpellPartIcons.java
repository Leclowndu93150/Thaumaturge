package com.leclowndu93150.thaumaturge.client.screen.casters;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPartKind;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;

public final class SpellPartIcons {
    public static final Identifier ROUND_BACK = TTIds.rl("textures/foci/_medium.png");
    public static final Identifier DIAMOND_BACK = TTIds.rl("textures/foci/_effect.png");
    private static final int GLYPH_TEXTURE = 32;
    private static final int OPAQUE = 0xFF;

    private SpellPartIcons() {}

    public static void draw(GuiGraphicsExtractor graphics, HolderLookup.Provider registries, SpellNode node, int centreX, int centreY, int back, int glyph, int flowGlyph, float alpha) {
        Optional<SpellPart> part = Spells.part(registries, node.part());
        if (part.isPresent()) {
            draw(graphics, part.get(), Spells.color(registries, node), centreX, centreY, back, glyph, flowGlyph, alpha);
        }
    }

    public static void draw(GuiGraphicsExtractor graphics, HolderLookup.Provider registries, ResourceKey<SpellPart> key, int centreX, int centreY, int back, int glyph, int flowGlyph, float alpha) {
        Optional<SpellPart> part = Spells.part(registries, key);
        if (part.isPresent()) {
            draw(graphics, part.get(), Spells.color(registries, SpellNode.of(key)), centreX, centreY, back, glyph, flowGlyph, alpha);
        }
    }

    public static void draw(GuiGraphicsExtractor graphics, SpellPart part, int color, int centreX, int centreY, int back, int glyph, int flowGlyph, float alpha) {
        int alphaByte = Math.round(alpha * OPAQUE);
        if (part.kind() == SpellPartKind.FLOW) {
            centred(graphics, part.icon(), centreX, centreY, flowGlyph, ARGB.color(alphaByte, 0xFFFFFF));
            return;
        }
        Identifier backplate = part.kind() == SpellPartKind.EFFECT ? DIAMOND_BACK : ROUND_BACK;
        centred(graphics, backplate, centreX, centreY, back, ARGB.color(alphaByte, color));
        centred(graphics, part.icon(), centreX, centreY, glyph, ARGB.color(alphaByte, 0xFFFFFF));
    }

    private static void centred(GuiGraphicsExtractor graphics, Identifier texture, int centreX, int centreY, int size, int color) {
        int half = size / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, centreX - half, centreY - half, 0.0F, 0.0F, size, size, GLYPH_TEXTURE, GLYPH_TEXTURE, GLYPH_TEXTURE, GLYPH_TEXTURE, color);
    }
}
