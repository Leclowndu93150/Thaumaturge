package com.leclowndu93150.thaumaturge.content.research.book;

import com.leclowndu93150.thaumaturge.client.screen.research.ThaumonomiconBrowserScreen;
import net.minecraft.client.Minecraft;

public final class ThaumonomiconClientOpener {
    private ThaumonomiconClientOpener() {}

    public static void open() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null)
            return;
        mc.setScreen(new ThaumonomiconBrowserScreen());
    }
}
