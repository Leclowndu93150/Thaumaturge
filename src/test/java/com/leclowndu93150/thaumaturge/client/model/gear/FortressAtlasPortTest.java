package com.leclowndu93150.thaumaturge.client.model.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

class FortressAtlasPortTest {
    @Test
    void setupAnimUpdatesMaskAndGogglesFromEachCurrentHeadStack() {
        ModelPart root = FortressArmorModel.createHead().bakeRoot();
        FortressArmorModel model = new FortressArmorModel(root);
        ModelPart visor = root.getChild("head").getChild("helmet").getChild("mask");
        LivingEntity entity = mock(LivingEntity.class, RETURNS_DEEP_STUBS);
        AtomicReference<ItemStack> equippedHead = new AtomicReference<>(new ItemStack(TTItems.FORTRESS_HELM.get()));
        when(entity.getItemBySlot(EquipmentSlot.HEAD)).thenAnswer(invocation -> equippedHead.get());

        ItemStack stack = equippedHead.get();
        model.setupAnim(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        assertMaskVisibility(visor, -1);
        assertFalse(visor.getChild("goggles").visible);

        for (int mask = 0; mask <= 2; mask++) {
            stack.set(TTDataComponents.FORTRESS_MASK.get(), mask);
            model.setupAnim(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
            assertMaskVisibility(visor, mask);
            assertFalse(visor.getChild("goggles").visible);
        }

        stack.set(TTDataComponents.GOGGLES_UPGRADE.get(), Unit.INSTANCE);
        model.setupAnim(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        assertMaskVisibility(visor, 2);
        assertTrue(visor.getChild("goggles").visible);

        equippedHead.set(new ItemStack(TTItems.FORTRESS_HELM.get()));
        model.setupAnim(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        assertMaskVisibility(visor, -1);
        assertFalse(visor.getChild("goggles").visible);
    }

    private static void assertMaskVisibility(ModelPart visor, int selectedMask) {
        assertEquals(selectedMask == 0, visor.getChild("mask_0").visible);
        assertEquals(selectedMask == 1, visor.getChild("mask_1").visible);
        assertEquals(selectedMask == 2, visor.getChild("mask_2").visible);
    }
}
