package com.leclowndu93150.thaumaturge.client.screen;

import com.leclowndu93150.thaumaturge.TCIds;
import net.minecraft.resources.ResourceLocation;

public final class TCScreenTextures {
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

    private TCScreenTextures() {}

    private static ResourceLocation gui(String name) {
        return ResourceLocation.fromNamespaceAndPath(TCIds.MODID, "textures/gui/" + name);
    }
}
