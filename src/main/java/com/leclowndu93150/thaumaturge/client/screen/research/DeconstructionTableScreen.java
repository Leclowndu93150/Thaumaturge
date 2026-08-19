package com.leclowndu93150.thaumaturge.client.screen.research;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.client.render.aspect.AspectTagRenderer;
import com.leclowndu93150.thaumaturge.client.screen.AbstractTCContainerScreen;
import com.leclowndu93150.thaumaturge.content.research.decon.BlockEntityDeconstructionTable;
import com.leclowndu93150.thaumaturge.content.research.decon.MenuDeconstructionTable;
import com.leclowndu93150.thaumaturge.network.ServerboundDeconCollectPayload;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;

public final class DeconstructionTableScreen extends AbstractTCContainerScreen<MenuDeconstructionTable> {
    private static final Identifier TEXTURE = TCIds.rl("textures/gui/gui_decontable.png");

    private static final int GUI_W = 176;
    private static final int GUI_H = 166;
    private static final int BAR_X = 93;
    private static final int BAR_Y = 15;
    private static final int BAR_U = 176;
    private static final int BAR_W = 9;
    private static final int BAR_MAX_H = 46;
    private static final int RESULT_X = 63;
    private static final int RESULT_Y = 47;
    private static final int RESULT_SIZE = 16;

    public DeconstructionTableScreen(MenuDeconstructionTable menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE, GUI_W, GUI_H);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {}

    @Override
    protected void extractBackgroundOverlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        BlockEntityDeconstructionTable table = menu.blockEntity();
        if (table == null) {
            return;
        }
        int fill = BAR_MAX_H - table.breakTime() * BAR_MAX_H / BlockEntityDeconstructionTable.BREAK_TIME_TICKS;
        fill = Math.max(0, Math.min(BAR_MAX_H, fill));
        if (fill > 0) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + BAR_X, topPos + BAR_Y + BAR_MAX_H - fill, (float) BAR_U, (float) (BAR_MAX_H - fill), BAR_W, fill, 256, 256);
        }
        Holder<IAspect> result = resultHolder(table);
        if (result != null) {
            AspectTagRenderer.render(graphics, font, leftPos + RESULT_X, topPos + RESULT_Y, result, 1);
            if (mouseX >= leftPos + RESULT_X && mouseX < leftPos + RESULT_X + RESULT_SIZE && mouseY >= topPos + RESULT_Y && mouseY < topPos + RESULT_Y + RESULT_SIZE) {
                graphics.setTooltipForNextFrame(font, List.of(AspectComponents.name(result), Component.translatable("tc.decon.collect")), Optional.empty(), mouseX, mouseY);
            }
        }
    }

    private @Nullable Holder<IAspect> resultHolder(BlockEntityDeconstructionTable table) {
        Identifier id = table.resultAspect();
        if (id == null || minecraft == null || minecraft.level == null) {
            return null;
        }
        return minecraft.level.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).get(ResourceKey.create(IAspect.REGISTRY_KEY, id)).map(reference -> (Holder<IAspect>) reference).orElse(null);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            BlockEntityDeconstructionTable table = menu.blockEntity();
            if (table != null && table.resultAspect() != null && event.x() >= leftPos + RESULT_X && event.x() < leftPos + RESULT_X + RESULT_SIZE && event.y() >= topPos + RESULT_Y
                    && event.y() < topPos + RESULT_Y + RESULT_SIZE) {
                ClientPacketDistributor.sendToServer(new ServerboundDeconCollectPayload(menu.pos()));
                if (minecraft != null && minecraft.player != null) {
                    minecraft.player.playSound(TCSounds.HHON.get(), 0.3F, 1.0F);
                }
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }
}
