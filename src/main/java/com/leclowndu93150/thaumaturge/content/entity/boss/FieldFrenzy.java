package com.leclowndu93150.thaumaturge.content.entity.boss;

import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.entity.eldritch.CastingArms;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

final class FieldFrenzy {
    private static final int LENGTH = 150;
    private static final int RING_WINDOW = 121;
    private static final int RING_INTERVAL = 10;
    private static final double RING_GROWTH = 8.0;
    private static final int SPOKE_THINNING = 8;
    private static final int SAP_LIFE = 250;
    private static final int SAP_LIFE_SPREAD = 150;
    private static final float BOLT_CHANCE = 0.3F;
    private static final int BOLT_COLOR = 0xBB44BB;
    private static final float ZAP_PITCH = 0.9F;
    private static final float ZAP_PITCH_SPREAD = 0.1F;
    private static final int RECALL_TRIES = 20;
    private static final int RECALL_SCATTER = 8;
    private static final double LANDING_LIFT = 0.1;
    private static final int TRAIL_LENGTH = 128;

    private final Mob caster;
    private final CastingArms arms;
    private boolean spent;
    private int remaining;

    FieldFrenzy(Mob caster, CastingArms arms) {
        this.caster = caster;
        this.arms = arms;
    }

    boolean active() {
        return remaining > 0;
    }

    void ignite() {
        if (!spent) {
            spent = true;
            remaining = LENGTH;
        }
    }

    void tick(ServerLevel level) {
        if (remaining == LENGTH) {
            recall(level);
        }
        if (remaining < RING_WINDOW && remaining % RING_INTERVAL == 0) {
            ring(level);
        }
        remaining--;
    }

    private void ring(ServerLevel level) {
        arms.raiseBoth();
        RandomSource random = caster.getRandom();
        Block sap = TCBlocks.EFFECT_SAP.get();
        double radius = (LENGTH - remaining) / RING_GROWTH;
        int stride = 1 + remaining / SPOKE_THINNING;
        int centreX = Mth.floor(caster.getX());
        int centreZ = Mth.floor(caster.getZ());
        for (int spoke = 0; spoke < 180 / stride; spoke++) {
            double angle = Math.toRadians(spoke * 2 * stride);
            BlockPos spot = BlockPos.containing(centreX + radius * Math.cos(angle), caster.getY(), centreZ + radius * Math.sin(angle));
            if (!level.isEmptyBlock(spot) || !level.getBlockState(spot.below()).isSolidRender()) {
                continue;
            }
            level.setBlockAndUpdate(spot, sap.defaultBlockState());
            level.scheduleTick(spot, sap, SAP_LIFE + random.nextInt(SAP_LIFE_SPREAD));
            if (random.nextFloat() < BOLT_CHANCE) {
                Effects.arcBolt(level, caster.position().add(0.0, caster.getBbHeight() / 2.0, 0.0)).to(Vec3.atCenterOf(spot)).color(BOLT_COLOR).send();
            }
        }
        caster.playSound(TCSounds.ZAP.get(), 1.0F, ZAP_PITCH + random.nextFloat() * ZAP_PITCH_SPREAD);
    }

    private void recall(ServerLevel level) {
        BlockPos home = caster.hasHome() ? caster.getHomePosition() : caster.blockPosition();
        RandomSource random = caster.getRandom();
        BlockPos.MutableBlockPos landing = home.mutable();
        boolean found = false;
        for (int attempt = 0; attempt < RECALL_TRIES && !found; attempt++) {
            found = level.getBlockState(landing.below()).blocksMotion() && !level.getBlockState(landing).blocksMotion();
            if (!found) {
                landing.set(home.getX() + random.nextInt(RECALL_SCATTER) - random.nextInt(RECALL_SCATTER), home.getY(), home.getZ() + random.nextInt(RECALL_SCATTER) - random.nextInt(RECALL_SCATTER));
            }
        }
        if (!found) {
            return;
        }
        Vec3 origin = caster.position();
        caster.setPos(landing.getX() + 0.5, landing.getY() + LANDING_LIFT, landing.getZ() + 0.5);
        if (!level.noCollision(caster, caster.getBoundingBox())) {
            caster.setPos(origin);
            return;
        }
        Vec3 path = caster.position().subtract(origin);
        double spread = caster.getBbWidth() * 2.0;
        for (int step = 0; step < TRAIL_LENGTH; step++) {
            double t = step / (TRAIL_LENGTH - 1.0);
            level.sendParticles(ParticleTypes.PORTAL, origin.x + path.x * t + (random.nextDouble() - 0.5) * spread, origin.y + path.y * t + random.nextDouble() * caster.getBbHeight(),
                    origin.z + path.z * t + (random.nextDouble() - 0.5) * spread, 1, 0.0, 0.0, 0.0, 0.0);
        }
        caster.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
    }
}
