package com.leclowndu93150.thaumaturge.client.color;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.GrassColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class TCColorHandlers {
    private static final int ROBES_UNDYED_ARGB = 0xFF6A3880;
    private static final int GREATWOOD_FOLIAGE_ARGB = 0xFF48B518;
    private static final int CRYSTAL_AER_COLOR = 0xFFFFFF7E;
    private static final int CRYSTAL_IGNIS_COLOR = 0xFFFF5A01;
    private static final int CRYSTAL_AQUA_COLOR = 0xFF3CD4FC;
    private static final int CRYSTAL_TERRA_COLOR = 0xFF56C000;
    private static final int CRYSTAL_ORDO_COLOR = 0xFFD5D4EC;
    private static final int CRYSTAL_PERDITIO_COLOR = 0xFF404040;
    private static final int CRYSTAL_VITIUM_COLOR = 0xFF800080;

    private TCColorHandlers() {}

    @SubscribeEvent
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(new CrystalAspectTint(), TCItems.ESSENTIA_CRYSTAL.get());
        event.register(new CrystalAspectTint(), TCItems.MANA_BEAN.get());
        event.register(new AspectColorTint(), TCItems.PHIAL.get());
        event.register(new LabelAspectTint(), TCItems.LABEL.get());
        event.register(new NoteColorTint(), TCItems.RESEARCH_NOTE.get());
        event.register(new GolemMaterialTint(), TCItems.GOLEM_PLACER.get());
        FocusColorTint focusColor = new FocusColorTint();
        event.register(focusColor, TCItems.FOCUS_1.get());
        event.register(focusColor, TCItems.FOCUS_2.get());
        event.register(focusColor, TCItems.FOCUS_3.get());
        for (DyeColor dye : DyeColor.values()) {
            event.register(
                    new AspectFilterTint(dye.getMapColor().col),
                    TCItems.BANNERS.get(dye).get());
            event.register(
                    constant(0xFF000000 | dye.getMapColor().col),
                    TCBlocks.CANDLES.get(dye).get());
            event.register(
                    constant(0xFF000000 | (dye.getTextureDiffuseColor() & 0xFFFFFF)),
                    TCItems.NITORS.get(dye).get());
        }
        event.register(constant(GREATWOOD_FOLIAGE_ARGB), TCItems.LEAVES_GREATWOOD.get());
        event.register(
                (stack, tintIndex) -> tintIndex == 0 ? 0xFF000000 | GrassColor.get(0.5D, 1.0D) : -1,
                TCItems.GRASS_AMBIENT.get());
        event.register(dyed(CRYSTAL_AER_COLOR), TCItems.CRYSTAL_AER.get());
        event.register(dyed(CRYSTAL_IGNIS_COLOR), TCItems.CRYSTAL_IGNIS.get());
        event.register(dyed(CRYSTAL_AQUA_COLOR), TCItems.CRYSTAL_AQUA.get());
        event.register(dyed(CRYSTAL_TERRA_COLOR), TCItems.CRYSTAL_TERRA.get());
        event.register(dyed(CRYSTAL_ORDO_COLOR), TCItems.CRYSTAL_ORDO.get());
        event.register(dyed(CRYSTAL_PERDITIO_COLOR), TCItems.CRYSTAL_PERDITIO.get());
        event.register(dyed(CRYSTAL_VITIUM_COLOR), TCItems.CRYSTAL_VITIUM.get());
        event.register(
                dyed(ROBES_UNDYED_ARGB),
                TCItems.CLOTH_CHEST.get(),
                TCItems.CLOTH_LEGS.get(),
                TCItems.CLOTH_BOOTS.get(),
                TCItems.VOID_ROBE_HELM.get(),
                TCItems.VOID_ROBE_CHEST.get(),
                TCItems.VOID_ROBE_LEGS.get());
    }

    private static ItemColor constant(int argb) {
        return (stack, tintIndex) -> tintIndex == 0 ? argb : -1;
    }

    private static ItemColor dyed(int fallbackArgb) {
        return (stack, tintIndex) -> tintIndex == 0 ? DyedItemColor.getOrDefault(stack, fallbackArgb) : -1;
    }
}
