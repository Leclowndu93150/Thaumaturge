package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.registry.TCAttachments;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.NoteBlockEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class ArcaneEarEvents {
    private static final double MAX_RANGE_SQ = 4096.0;

    private ArcaneEarEvents() {}

    @SubscribeEvent
    public static void onNotePlayed(NoteBlockEvent.Play event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        BlockPos source = event.getPos();
        List<BlockPos> ears = List.copyOf(level.getData(TCAttachments.EAR_INDEX.get()));
        for (BlockPos earPos : ears) {
            if (earPos.distToCenterSqr(source.getX() + 0.5, source.getY() + 0.5, source.getZ() + 0.5) > MAX_RANGE_SQ) {
                continue;
            }
            if (level.getBlockEntity(earPos) instanceof BlockEntityArcaneEar ear && ear.matches(event.getInstrument(), event.getVanillaNoteId())) {
                ear.trigger();
            }
        }
    }
}
