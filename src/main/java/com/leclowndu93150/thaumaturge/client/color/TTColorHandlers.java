package com.leclowndu93150.thaumaturge.client.color;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.GrassColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class TTColorHandlers {
    private static final int ROBES_UNDYED_ARGB = 0xFF6A3880;
    private static final int GREATWOOD_FOLIAGE_ARGB = 0xFF48B518;
    private static final int CRYSTAL_AER_COLOR = 0xFFFFFF7E;
    private static final int CRYSTAL_IGNIS_COLOR = 0xFFFF5A01;
    private static final int CRYSTAL_AQUA_COLOR = 0xFF3CD4FC;
    private static final int CRYSTAL_TERRA_COLOR = 0xFF56C000;
    private static final int CRYSTAL_ORDO_COLOR = 0xFFD5D4EC;
    private static final int CRYSTAL_PERDITIO_COLOR = 0xFF404040;
    private static final int CRYSTAL_VITIUM_COLOR = 0xFF800080;

    private TTColorHandlers() {}

    @SubscribeEvent
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(new CrystalAspectTint(), TTItems.ESSENTIA_CRYSTAL.get());
        event.register(new CrystalAspectTint(), TTItems.MANA_BEAN.get());
        event.register(new AspectColorTint(), TTItems.PHIAL.get());
        event.register(new LabelAspectTint(), TTItems.LABEL.get());
        event.register(new NoteColorTint(), TTItems.RESEARCH_NOTE.get());
        event.register(new GolemMaterialTint(), TTItems.GOLEM_PLACER.get());
        FocusColorTint focusColor = new FocusColorTint();
        event.register(focusColor, TTItems.FOCUS_1.get());
        event.register(focusColor, TTItems.FOCUS_2.get());
        event.register(focusColor, TTItems.FOCUS_3.get());
        for (DyeColor dye : DyeColor.values()) {
            event.register(
                    new AspectFilterTint(dye.getMapColor().col),
                    TTItems.BANNERS.get(dye).get());
            event.register(
                    constant(0xFF000000 | dye.getMapColor().col),
                    TTBlocks.CANDLES.get(dye).get());
            event.register(
                    constant(0xFF000000 | (dye.getTextureDiffuseColor() & 0xFFFFFF)),
                    TTItems.NITORS.get(dye).get());
        }
        event.register(constant(GREATWOOD_FOLIAGE_ARGB), TTItems.LEAVES_GREATWOOD.get());
        event.register(
                (stack, tintIndex) -> tintIndex == 0 ? 0xFF000000 | GrassColor.get(0.5D, 1.0D) : -1,
                TTItems.GRASS_AMBIENT.get());
        event.register(dyed(CRYSTAL_AER_COLOR), TTItems.CRYSTAL_AER.get());
        event.register(dyed(CRYSTAL_IGNIS_COLOR), TTItems.CRYSTAL_IGNIS.get());
        event.register(dyed(CRYSTAL_AQUA_COLOR), TTItems.CRYSTAL_AQUA.get());
        event.register(dyed(CRYSTAL_TERRA_COLOR), TTItems.CRYSTAL_TERRA.get());
        event.register(dyed(CRYSTAL_ORDO_COLOR), TTItems.CRYSTAL_ORDO.get());
        event.register(dyed(CRYSTAL_PERDITIO_COLOR), TTItems.CRYSTAL_PERDITIO.get());
        event.register(dyed(CRYSTAL_VITIUM_COLOR), TTItems.CRYSTAL_VITIUM.get());
        event.register(
                dyed(ROBES_UNDYED_ARGB),
                TTItems.CLOTH_CHEST.get(),
                TTItems.CLOTH_LEGS.get(),
                TTItems.CLOTH_BOOTS.get(),
                TTItems.VOID_ROBE_HELM.get(),
                TTItems.VOID_ROBE_CHEST.get(),
                TTItems.VOID_ROBE_LEGS.get());
    }

    private static ItemColor constant(int argb) {
        return (stack, tintIndex) -> tintIndex == 0 ? argb : -1;
    }

    private static ItemColor dyed(int fallbackArgb) {
        return (stack, tintIndex) -> tintIndex == 0 ? DyedItemColor.getOrDefault(stack, fallbackArgb) : -1;
    }
}
