package com.leclowndu93150.thaumaturge.client.screen;

import com.leclowndu93150.thaumaturge.TCIds;
import net.minecraft.resources.Identifier;

public final class TCScreenTextures {
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

    private TCScreenTextures() {}

    private static Identifier gui(String name) {
        return Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/gui/" + name);
    }
}
