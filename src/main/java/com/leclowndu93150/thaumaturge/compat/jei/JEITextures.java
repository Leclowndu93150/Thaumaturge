package com.leclowndu93150.thaumaturge.compat.jei;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.resources.Identifier;

public final class JEITextures {
    public static final Identifier DUST_TRIGGER_BACKGROUND = jei("dust_trigger.png");
    public static final Identifier ASPECT_COMPOSITION_BACKGROUND = jei("aspect_composition.png");
    public static final Identifier ARCANE_WORKBENCH_BACKGROUND = jei("arcane_workbench.png");
    public static final Identifier CRUCIBLE_BACKGROUND = jei("crucible.png");
    public static final Identifier INFUSION_BACKGROUND = jei("infusion.png");

    public static final int CATEGORY_WIDTH = 144;
    public static final int CATEGORY_HEIGHT = 64;

    private JEITextures() {}

    private static Identifier jei(String name) {
        return Identifier.fromNamespaceAndPath(TTIds.MODID, "textures/gui/jei/" + name);
    }
}
