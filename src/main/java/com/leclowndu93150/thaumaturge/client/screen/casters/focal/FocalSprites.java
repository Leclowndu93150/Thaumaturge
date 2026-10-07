package com.leclowndu93150.thaumaturge.client.screen.casters.focal;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPartKind;
import net.minecraft.resources.Identifier;

public final class FocalSprites {
    public static final Identifier FRAME = sprite("frame");
    public static final Identifier WELL = sprite("well");
    public static final Identifier FIELD = sprite("field");
    public static final Identifier SLOT = sprite("slot");
    public static final Identifier CELL = sprite("cell");
    public static final Identifier SELECTION = sprite("selection");
    public static final Identifier SCROLL_THUMB = sprite("scroll_thumb");
    public static final Identifier SIGIL = sprite("sigil");
    public static final Identifier DIVIDER = sprite("divider");
    public static final Identifier BAR_FILL = sprite("bar_fill");
    public static final Identifier BAR_FILL_OVER = sprite("bar_fill_over");
    public static final Identifier WARN = sprite("warn");
    public static final Identifier BUTTON_NORMAL = sprite("button_normal");
    public static final Identifier BUTTON_HOVER = sprite("button_hover");
    public static final Identifier BUTTON_PRESSED = sprite("button_pressed");
    public static final Identifier TAB_NORMAL = sprite("tab_normal");
    public static final Identifier TAB_HOVER = sprite("tab_hover");
    public static final Identifier TAB_SELECTED = sprite("tab_selected");
    public static final Identifier GLYPH_CHECK = sprite("glyph_check");
    public static final Identifier GLYPH_DOWN = sprite("glyph_down");
    public static final Identifier GLYPH_LEFT = sprite("glyph_left");
    public static final Identifier GLYPH_RIGHT = sprite("glyph_right");
    public static final Identifier GLYPH_MINUS = sprite("glyph_minus");
    public static final Identifier GLYPH_PLUS = sprite("glyph_plus");

    public static final Identifier CABINET = TTIds.rl("textures/gui/gui_wandtable3.png");
    public static final Identifier STRIP = TTIds.rl("textures/gui/gui_wandtable.png");
    public static final Identifier COMPLEXITY = TTIds.rl("textures/gui/complex.png");
    public static final Identifier CRYSTAL = TTIds.rl("textures/item/essentia_crystal.png");
    public static final Identifier XP_ORB = Identifier.withDefaultNamespace("textures/gui/sprites/container/enchanting_table/level_1.png");

    public static final int SLOT_SIZE = 18;
    public static final int CELL_SIZE = 22;
    public static final int SELECTION_SIZE = 24;
    public static final int SCROLL_THUMB_W = 6;
    public static final int SCROLL_THUMB_H = 18;
    public static final int SIGIL_SIZE = 128;
    public static final int DIVIDER_H = 2;
    public static final int BAR_FILL_H = 4;
    public static final int WARN_SIZE = 9;
    public static final int TAB_W = 26;
    public static final int TAB_H = 20;
    public static final int TAB_ICON = 12;
    public static final int GLYPH_CHECK_SIZE = 10;
    public static final int GLYPH_SIZE = 8;

    public static final int CABINET_W = 70;
    public static final int CABINET_H = 237;
    public static final int STRIP_TEXTURE = 256;
    public static final int LINK_U = 54;
    public static final int LINK_V = 232;
    public static final int LINK_SIZE = 12;
    public static final int SPLIT_LEFT_U = 8;
    public static final int SPLIT_RIGHT_U = 24;
    public static final int SPLIT_MID_U = 72;
    public static final int SPLIT_V = 240;
    public static final int SPLIT_END_W = 16;
    public static final int SPLIT_MID_W = 24;
    public static final int SPLIT_H = 16;
    public static final int COMPLEXITY_TEXTURE = 16;
    public static final int CRYSTAL_TEXTURE = 16;
    public static final int XP_ORB_TEXTURE = 16;
    public static final int XP_ORB_U = 3;
    public static final int XP_ORB_V = 3;
    public static final int XP_ORB_SIZE = 9;

    private FocalSprites() {}

    public static Identifier tabIcon(SpellPartKind kind, boolean selected) {
        return sprite("icon_" + kind.getSerializedName() + (selected ? "_selected" : ""));
    }

    private static Identifier sprite(String name) {
        return TTIds.rl("focal/" + name);
    }
}
