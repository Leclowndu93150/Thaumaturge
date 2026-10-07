package com.leclowndu93150.thaumaturge.client.screen;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.resources.Identifier;

public final class TTScreenTextures {
    public static final int TEX_SIZE = 256;

    public static final Identifier ARCANE_WORKBENCH = gui("arcane_workbench.png");
    public static final Identifier GUI_BASE = gui("gui_base.png");
    public static final Identifier GUI_LOGISTICS = gui("gui_logistics.png");
    public static final Identifier RESEARCH_BROWSER = gui("gui_research_browser.png");
    public static final Identifier RESEARCH_BOOK = gui("gui_researchbook.png");
    public static final Identifier RESEARCH_BOOK_OVERLAY = gui("gui_researchbook_overlay.png");
    public static final Identifier RESEARCH_TABLE = gui("gui_research_table.png");
    public static final Identifier RESEARCH_BACK_OVER = gui("gui_research_back_over.png");
    public static final Identifier PAPER = gui("paper.png");
    public static final Identifier PAPER_GILDED = gui("papergilded.png");
    public static final Identifier HUD = gui("hud.png");
    public static final Identifier HUD_HOVER_SPARK = gui("hud_hover_spark.png");
    public static final Identifier THAUMOSTATIC_HARNESS = gui("gui_thaumostatic_harness.png");
    public static final Identifier RESEARCH_PREREQ_MAP = research("rd_map.png");
    public static final Identifier RESEARCH_PREREQ_FLASK = research("rd_flask.png");
    public static final Identifier RESEARCH_PREREQ_CHEST = research("rd_chest.png");

    public static final Identifier GRINDSTONE = Identifier.withDefaultNamespace("textures/gui/container/grindstone.png");
    public static final Identifier GRINDSTONE_ERROR = Identifier.withDefaultNamespace("container/grindstone/error");

    private TTScreenTextures() {}

    private static Identifier gui(String name) {
        return Identifier.fromNamespaceAndPath(TTIds.MODID, "textures/gui/" + name);
    }

    private static Identifier research(String name) {
        return Identifier.fromNamespaceAndPath(TTIds.MODID, "textures/research/" + name);
    }
}
