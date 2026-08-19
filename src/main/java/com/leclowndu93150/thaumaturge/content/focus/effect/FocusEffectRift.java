package com.leclowndu93150.thaumaturge.content.focus.effect;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.casters.CastContext;
import com.leclowndu93150.thaumaturge.api.casters.FocusEffect;
import com.leclowndu93150.thaumaturge.api.casters.FocusSettings;
import com.leclowndu93150.thaumaturge.api.casters.SettingDefinition;
import com.leclowndu93150.thaumaturge.api.casters.Trajectory;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.content.focus.BlockEntityHole;
import com.leclowndu93150.thaumaturge.content.particle.RiftShardParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TCBlockTags;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class FocusEffectRift implements FocusEffect {
    private static final Identifier KEY = TCIds.rl("rift");

    private static final int BASE_COMPLEXITY = 3;
    private static final int DURATION_COMPLEXITY_DIVISOR = 2;
    private static final int DEPTH_COMPLEXITY_DIVISOR = 4;
    private static final int DURATION_TICKS_FACTOR = 20;
    private static final float INDESTRUCTIBLE = -1.0F;

    @Override
    public Identifier id() {
        return KEY;
    }

    @Override
    public ResearchGate research() {
        return new ResearchGate(TCIds.rl("focus_rift"), Optional.empty(), false);
    }

    @Override
    public ResourceKey<IAspect> aspect() {
        return TCAspects.ALIENIS;
    }

    @Override
    public int complexity(FocusSettings settings) {
        return BASE_COMPLEXITY + settings.value("duration") / DURATION_COMPLEXITY_DIVISOR + settings.value("depth") / DEPTH_COMPLEXITY_DIVISOR;
    }

    @Override
    public boolean apply(CastContext ctx, FocusSettings settings, HitResult target, @Nullable Trajectory trajectory, int index) {
        if (!(target instanceof BlockHitResult blockHit)) {
            return false;
        }
        Level level = ctx.level();
        float maxdis = settings.value("depth") * ctx.power();
        int dur = DURATION_TICKS_FACTOR * settings.value("duration");
        int distance = 0;
        BlockPos pos = blockHit.getBlockPos();
        for (distance = 0; distance < maxdis; distance++) {
            BlockState bi = level.getBlockState(pos);
            if (bi.is(TCBlockTags.PORTABLE_HOLE_BLACKLIST) || bi.is(Blocks.BEDROCK) || bi.is(TCBlocks.HOLE.get()) || bi.isAir() || bi.getDestroySpeed(level, pos) == INDESTRUCTIBLE) {
                break;
            }
            pos = pos.relative(blockHit.getDirection().getOpposite());
        }
        createHole(level, blockHit.getBlockPos(), blockHit.getDirection(), distance + 1, dur);
        return true;
    }

    public static boolean createHole(Level level, BlockPos pos, @Nullable Direction side, int count, int max) {
        BlockState bs = level.getBlockState(pos);
        if (level.isClientSide() || level.getBlockEntity(pos) != null || bs.is(TCBlockTags.PORTABLE_HOLE_BLACKLIST) || bs.is(Blocks.BEDROCK) || bs.is(TCBlocks.HOLE.get())
                || (!bs.isAir() && bs.canBeReplaced()) || bs.getDestroySpeed(level, pos) == INDESTRUCTIBLE) {
            return false;
        }
        if (level.setBlock(pos, TCBlocks.HOLE.get().defaultBlockState(), Block.UPDATE_ALL) && level.getBlockEntity(pos) instanceof BlockEntityHole hole) {
            hole.configure(bs, max, count, side);
            hole.setChanged();
        }
        return true;
    }

    @Override
    public List<SettingDefinition> settings() {
        int[] depth = new int[]{8, 16, 24, 32};
        String[] depthDesc = new String[]{"8", "16", "24", "32"};
        return List.of(new SettingDefinition("depth", "focus.rift.depth", new SettingDefinition.IntList(depth, depthDesc)),
                new SettingDefinition("duration", "focus.common.duration", new SettingDefinition.IntRange(2, 10)));
    }

    @Override
    public void impactParticles(Level level, Vec3 pos, Vec3 motion, Vec3 drift) {
        RiftShardParticleOptions data = new RiftShardParticleOptions((float) (0.7F + level.getRandom().nextGaussian() * 0.3F));
        level.addParticle(data, pos.x, pos.y, pos.z, 0.0, 0.0, 0.0);
    }

    @Override
    public void onCast(LivingEntity caster) {
        caster.level().playSound(null, caster.blockPosition().above(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.2F, 0.7F);
    }
}
