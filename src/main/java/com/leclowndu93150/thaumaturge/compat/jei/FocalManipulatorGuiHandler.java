package com.leclowndu93150.thaumaturge.compat.jei;

import com.leclowndu93150.thaumaturge.client.screen.casters.focal.FocalManipulatorScreen;
import java.util.List;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.renderer.Rect2i;

public final class FocalManipulatorGuiHandler implements IGuiContainerHandler<FocalManipulatorScreen> {
    @Override
    public List<Rect2i> getGuiExtraAreas(FocalManipulatorScreen screen) {
        return List.of(new Rect2i(0, 0, screen.width, screen.height));
    }
}
