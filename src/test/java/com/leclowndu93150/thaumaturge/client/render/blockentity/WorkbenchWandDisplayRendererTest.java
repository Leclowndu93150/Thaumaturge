package com.leclowndu93150.thaumaturge.client.render.blockentity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.client.render.blockentity.LegacyItemLift.ModelBounds;
import com.leclowndu93150.thaumaturge.content.wands.ItemWand;
import com.leclowndu93150.thaumaturge.content.workbench.BlockEntityArcaneWorkbench;
import com.leclowndu93150.thaumaturge.content.workbench.InventoryArcaneWorkbench;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

class WorkbenchWandDisplayRendererTest {
    @Test
    void wandAndStaffRestOnTheSurfaceCenteredAcrossTheCraftingGrid() {
        ItemStack wand = mock(ItemStack.class);
        when(wand.getItem()).thenReturn(mock(ItemWand.class));
        BlockEntityArcaneWorkbench workbench = workbench(wand);
        Minecraft minecraft = mock(Minecraft.class);
        ItemRenderer items = mock(ItemRenderer.class);
        when(minecraft.getItemRenderer()).thenReturn(items);
        MultiBufferSource buffers = mock(MultiBufferSource.class);

        try (var client = mockStatic(Minecraft.class);
                var measuredBounds = mockStatic(LegacyItemLift.class)) {
            client.when(Minecraft::getInstance).thenReturn(minecraft);
            for (ModelBounds bounds : List.of(
                    new ModelBounds(-0.075F, 0.075F, -1.2F, 0.08F, -0.075F, 0.075F),
                    new ModelBounds(-0.1F, 0.1F, -2.4F, 0.35F, -0.1F, 0.1F),
                    new ModelBounds(0.2F, 0.4F, -1.2F, 0.1F, -0.4F, -0.2F))) {
                measuredBounds
                        .when(() -> LegacyItemLift.modelBounds(wand, ItemDisplayContext.NONE))
                        .thenReturn(bounds);
                doAnswer(call -> {
                            Matrix4f transform =
                                    ((PoseStack) call.getArgument(4)).last().pose();
                            Vector3f bottom = transform.transformPosition(
                                    new Vector3f(bounds.centerX(), bounds.centerY(), bounds.maxZ()));
                            assertEquals(1.0F, bottom.y, 0.00001F);
                            Vector3f center = transform.transformPosition(
                                    new Vector3f(bounds.centerX(), bounds.centerY(), bounds.centerZ()));
                            assertEquals(0.5F, center.x, 0.00001F);
                            assertEquals(0.5F, center.z, 0.00001F);
                            Vector3f start = transform.transformPosition(
                                    new Vector3f(bounds.centerX(), bounds.minY(), bounds.centerZ()));
                            Vector3f end = transform.transformPosition(
                                    new Vector3f(bounds.centerX(), bounds.maxY(), bounds.centerZ()));
                            assertEquals(10.0F / 16.0F, end.x - start.x, 0.00001F);
                            assertEquals(10.0F / 16.0F, end.z - start.z, 0.00001F);
                            return null;
                        })
                        .when(items)
                        .renderStatic(
                                eq(wand),
                                eq(ItemDisplayContext.NONE),
                                anyInt(),
                                anyInt(),
                                any(PoseStack.class),
                                eq(buffers),
                                isNull(),
                                anyInt());
                PoseStack pose = new PoseStack();
                new WorkbenchWandDisplayRenderer(null).render(workbench, 0.0F, pose, buffers, 0, 0);
                assertEquals(new Matrix4f(), pose.last().pose());
            }
            verify(items, times(3))
                    .renderStatic(
                            eq(wand),
                            eq(ItemDisplayContext.NONE),
                            anyInt(),
                            anyInt(),
                            any(PoseStack.class),
                            eq(buffers),
                            isNull(),
                            anyInt());
        }
    }

    @Test
    void unavailableOrFlatMeasurementsDoNotRenderWithAnInvalidScale() {
        ItemStack wand = mock(ItemStack.class);
        when(wand.getItem()).thenReturn(mock(ItemWand.class));
        MultiBufferSource buffers = mock(MultiBufferSource.class);
        try (var measuredBounds = mockStatic(LegacyItemLift.class)) {
            new WorkbenchWandDisplayRenderer(null).render(workbench(wand), 0.0F, new PoseStack(), buffers, 0, 0);
            measuredBounds
                    .when(() -> LegacyItemLift.modelBounds(wand, ItemDisplayContext.NONE))
                    .thenReturn(new ModelBounds(0, 1, 0, 0, 0, 1));
            new WorkbenchWandDisplayRenderer(null).render(workbench(wand), 0.0F, new PoseStack(), buffers, 0, 0);
            verifyNoInteractions(buffers);
        }
    }

    private static BlockEntityArcaneWorkbench workbench(ItemStack wand) {
        BlockEntityArcaneWorkbench workbench = mock(BlockEntityArcaneWorkbench.class);
        InventoryArcaneWorkbench inventory = mock(InventoryArcaneWorkbench.class);
        when(workbench.getInventory()).thenReturn(inventory);
        when(workbench.getBlockPos()).thenReturn(BlockPos.ZERO);
        when(inventory.wandStack()).thenReturn(wand);
        return workbench;
    }
}
