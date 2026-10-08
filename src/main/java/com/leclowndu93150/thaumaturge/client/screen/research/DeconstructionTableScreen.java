package com.leclowndu93150.thaumaturge.client.screen.research;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.client.render.aspect.AspectTagRenderer;
import com.leclowndu93150.thaumaturge.client.screen.AbstractTTContainerScreen;
import com.leclowndu93150.thaumaturge.client.screen.tooltip.DeferredTooltip;
import com.leclowndu93150.thaumaturge.content.research.decon.BlockEntityDeconstructionTable;
import com.leclowndu93150.thaumaturge.content.research.decon.MenuDeconstructionTable;
import com.leclowndu93150.thaumaturge.network.ServerboundDeconCollectPayload;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;

public final class DeconstructionTableScreen extends AbstractTTContainerScreen<MenuDeconstructionTable> {
    private static final ResourceLocation TEXTURE = TTIds.rl("textures/gui/gui_decontable.png");

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
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {}

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        DeferredTooltip.render(graphics, font);
    }

    @Override
    protected void renderBackgroundOverlay(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        BlockEntityDeconstructionTable table = menu.blockEntity();
        if (table == null) {
            return;
        }
        int fill = BAR_MAX_H - menu.breakTime() * BAR_MAX_H / BlockEntityDeconstructionTable.BREAK_TIME_TICKS;
        fill = Math.max(0, Math.min(BAR_MAX_H, fill));
        if (fill > 0) {
            graphics.blit(
                    TEXTURE,
                    leftPos + BAR_X,
                    topPos + BAR_Y + BAR_MAX_H - fill,
                    (float) BAR_U,
                    (float) (BAR_MAX_H - fill),
                    BAR_W,
                    fill,
                    256,
                    256);
        }
        Holder<IAspect> result = resultHolder(table);
        if (result != null) {
            AspectTagRenderer.render(graphics, font, leftPos + RESULT_X, topPos + RESULT_Y, result, 1);
            if (mouseX >= leftPos + RESULT_X
                    && mouseX < leftPos + RESULT_X + RESULT_SIZE
                    && mouseY >= topPos + RESULT_Y
                    && mouseY < topPos + RESULT_Y + RESULT_SIZE) {
                DeferredTooltip.set(
                        List.of(
                                AspectComponents.name(result),
                                Component.translatable("gui.thaumaturge.deconstruction_table.collect")),
                        mouseX,
                        mouseY);
            }
        }
    }

    private @Nullable Holder<IAspect> resultHolder(BlockEntityDeconstructionTable table) {
        ResourceLocation id = table.resultAspect();
        if (id == null || minecraft == null || minecraft.level == null) {
            return null;
        }
        return minecraft
                .level
                .registryAccess()
                .lookupOrThrow(IAspect.REGISTRY_KEY)
                .get(ResourceKey.create(IAspect.REGISTRY_KEY, id))
                .map(reference -> (Holder<IAspect>) reference)
                .orElse(null);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            BlockEntityDeconstructionTable table = menu.blockEntity();
            if (table != null
                    && table.resultAspect() != null
                    && mouseX >= leftPos + RESULT_X
                    && mouseX < leftPos + RESULT_X + RESULT_SIZE
                    && mouseY >= topPos + RESULT_Y
                    && mouseY < topPos + RESULT_Y + RESULT_SIZE) {
                PacketDistributor.sendToServer(new ServerboundDeconCollectPayload(menu.pos()));
                if (minecraft != null && minecraft.player != null) {
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(TTSounds.HHON.get(), 1.0F, 0.3F));
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
