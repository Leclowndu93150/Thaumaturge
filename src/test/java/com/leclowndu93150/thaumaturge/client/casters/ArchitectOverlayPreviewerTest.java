package com.leclowndu93150.thaumaturge.client.casters;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.items.AreaPreviewer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

class ArchitectOverlayPreviewerTest {
    @Test
    void preservesBlockCentersAcrossAPlusShapeInWorldAndPlotCoordinates() {
        for (int coordinate : new int[] {0, 20_480_000}) {
            BlockPos anchor = new BlockPos(coordinate, 64, coordinate);
            Vec3 origin = Vec3.atLowerCornerOf(anchor);
            for (BlockPos offset : new BlockPos[] {
                BlockPos.ZERO, BlockPos.ZERO.east(), BlockPos.ZERO.west(), BlockPos.ZERO.north(), BlockPos.ZERO.south()
            }) {
                PoseStack poseStack = new PoseStack();
                ArchitectOverlayRenderer.translateToBlockCenter(poseStack, anchor.offset(offset), origin);

                assertEquals(
                        new Vector3f(offset.getX() + 0.5F, 0.5F, offset.getZ() + 0.5F),
                        poseStack.last().pose().getTranslation(new Vector3f()));
            }
        }
    }

    @Test
    void modernOnlyItemIsSelectedForNativePreviewRendering() {
        Item modernItem = mock(Item.class, withSettings().extraInterfaces(AreaPreviewer.class));
        ItemStack mainHand = mock(ItemStack.class);
        LocalPlayer player = mock(LocalPlayer.class);
        when(mainHand.getItem()).thenReturn(modernItem);
        when(player.getMainHandItem()).thenReturn(mainHand);

        assertTrue(modernItem instanceof AreaPreviewer);
        assertSame(mainHand, ArchitectOverlayRenderer.heldArchitect(player));
    }
}
