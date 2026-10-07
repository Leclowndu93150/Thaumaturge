package com.leclowndu93150.thaumaturge.client.color;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.List;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class CrystalBlockColors {
    private static final int AER = 0xFFFF7E;
    private static final int IGNIS = 0xFF5A01;
    private static final int AQUA = 0x3CD4FC;
    private static final int TERRA = 0x56C000;
    private static final int ORDO = 0xD5D4EC;
    private static final int PERDITIO = 0x404040;
    private static final int VITIUM = 0x800080;

    private CrystalBlockColors() {}

    @SubscribeEvent
    public static void onRegisterBlockColors(RegisterColorHandlersEvent.BlockTintSources event) {
        register(event, TTBlocks.CRYSTAL_AER.get(), AER);
        register(event, TTBlocks.CRYSTAL_IGNIS.get(), IGNIS);
        register(event, TTBlocks.CRYSTAL_AQUA.get(), AQUA);
        register(event, TTBlocks.CRYSTAL_TERRA.get(), TERRA);
        register(event, TTBlocks.CRYSTAL_ORDO.get(), ORDO);
        register(event, TTBlocks.CRYSTAL_PERDITIO.get(), PERDITIO);
        register(event, TTBlocks.CRYSTAL_VITIUM.get(), VITIUM);
    }

    private static void register(RegisterColorHandlersEvent.BlockTintSources event, Block block, int rgb) {
        int argb = ARGB.opaque(rgb);
        BlockTintSource source = state -> argb;
        event.register(List.of(source), block);
    }
}
