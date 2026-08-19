package com.leclowndu93150.thaumaturge.client.extensions;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.effect.ClientEffects;
import com.leclowndu93150.thaumaturge.content.misc.nitor.BlockNitor;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class NitorClientExtensions implements IClientBlockExtensions {
    private static final NitorClientExtensions INSTANCE = new NitorClientExtensions();

    private NitorClientExtensions() {}

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        Block[] nitors = TCBlocks.NITORS.values().stream().map(b -> (Block) b.get()).toArray(Block[]::new);
        event.registerBlock(INSTANCE, nitors);
    }

    @Override
    public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
        if (!(state.getBlock() instanceof BlockNitor nitor))
            return true;
        int rgb = nitor.dyeColor();
        RandomSource rand = level.getRandom();
        for (int i = 0; i < 12; i++) {
            double cx = pos.getX() + 0.5 + rand.nextGaussian() * 0.08;
            double cy = pos.getY() + 0.5 + rand.nextGaussian() * 0.08;
            double cz = pos.getZ() + 0.5 + rand.nextGaussian() * 0.08;
            double vx = rand.nextGaussian() * 0.04;
            double vy = rand.nextFloat() * 0.1 + 0.02;
            double vz = rand.nextGaussian() * 0.04;
            ClientEffects.nitorFlames(level, cx, cy, cz, vx, vy, vz, rgb, 0);
        }
        ClientEffects.nitorCore(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0.0, 0.05, 0.0, rgb);
        return true;
    }

    @Override
    public boolean addHitEffects(BlockState state, Level level, @Nullable HitResult target, ParticleEngine manager) {
        if (target == null)
            return true;
        if (!(state.getBlock() instanceof BlockNitor nitor))
            return true;
        int rgb = nitor.dyeColor();
        RandomSource rand = level.getRandom();
        BlockPos pos = target instanceof BlockHitResult bhr ? bhr.getBlockPos() : BlockPos.containing(target.getLocation());
        for (int i = 0; i < 3; i++) {
            double cx = pos.getX() + 0.5 + rand.nextGaussian() * 0.05;
            double cy = pos.getY() + 0.5 + rand.nextGaussian() * 0.05;
            double cz = pos.getZ() + 0.5 + rand.nextGaussian() * 0.05;
            double vx = rand.nextGaussian() * 0.02;
            double vy = rand.nextFloat() * 0.06 + 0.01;
            double vz = rand.nextGaussian() * 0.02;
            ClientEffects.nitorFlames(level, cx, cy, cz, vx, vy, vz, rgb, 0);
        }
        return true;
    }
}
