package com.leclowndu93150.thaumaturge.client.network;

import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTube;
import com.leclowndu93150.thaumaturge.network.ClientboundTubeCreakPayload;
import com.leclowndu93150.thaumaturge.network.ClientboundTubeVentPayload;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class TubeEventClientHandler {
    private TubeEventClientHandler() {}

    public static void handleVent(ClientboundTubeVentPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            if (level == null)
                return;
            BlockPos pos = payload.pos();
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BlockEntityTube tube) {
                tube.triggerVent(payload.color());
            }
            RandomSource random = level.getRandom();
            playSound(level, pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.1F, 1.0F + random.nextFloat() * 0.1F);
        });
    }

    public static void handleCreak(ClientboundTubeCreakPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            if (level == null)
                return;
            playSound(level, payload.pos(), TCSounds.CREAK.get(), SoundSource.AMBIENT, 1.0F, 1.3F + level.getRandom().nextFloat() * 0.2F);
        });
    }

    private static void playSound(ClientLevel level, BlockPos pos, SoundEvent event, SoundSource source, float volume, float pitch) {
        level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, event, source, volume, pitch, false);
    }
}
