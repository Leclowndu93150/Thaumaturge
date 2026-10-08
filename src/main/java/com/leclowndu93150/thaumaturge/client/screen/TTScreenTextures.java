package com.leclowndu93150.thaumaturge.client.screen;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.resources.ResourceLocation;

public final class TTScreenTextures {
    public static final int TEX_SIZE = 256;

    public static final ResourceLocation ARCANE_WORKBENCH = gui("arcane_workbench.png");
    public static final ResourceLocation GUI_BASE = gui("gui_base.png");
    public static final ResourceLocation GUI_LOGISTICS = gui("gui_logistics.png");
    public static final ResourceLocation RESEARCH_BROWSER = gui("gui_research_browser.png");
    public static final ResourceLocation RESEARCH_BOOK = gui("gui_researchbook.png");
    public static final ResourceLocation RESEARCH_BOOK_OVERLAY = gui("gui_researchbook_overlay.png");
    public static final ResourceLocation RESEARCH_TABLE = gui("gui_research_table.png");
    public static final ResourceLocation RESEARCH_BACK_OVER = gui("gui_research_back_over.png");
    public static final ResourceLocation PAPER = gui("paper.png");
    public static final ResourceLocation PAPER_GILDED = gui("papergilded.png");
    public static final ResourceLocation RESEARCH_PREREQ_MAP = research("rd_map.png");
    public static final ResourceLocation RESEARCH_PREREQ_FLASK = research("rd_flask.png");
    public static final ResourceLocation RESEARCH_PREREQ_CHEST = research("rd_chest.png");

    public static final ResourceLocation GRINDSTONE =
            ResourceLocation.withDefaultNamespace("textures/gui/container/grindstone.png");
    public static final ResourceLocation GRINDSTONE_ERROR =
            ResourceLocation.withDefaultNamespace("container/grindstone/error");

    public static final ResourceLocation HUD_HOVER_SPARK = gui("hud_hover_spark.png");
    public static final ResourceLocation THAUMOSTATIC_HARNESS = gui("gui_thaumostatic_harness.png");
    public static final ResourceLocation HUD = gui("hud.png");

    private TTScreenTextures() {}

    private static ResourceLocation gui(String name) {
        return ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "textures/gui/" + name);
    }

    private static ResourceLocation research(String name) {
        return ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "textures/research/" + name);
    }
}
