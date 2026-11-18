package com.leclowndu93150.thaumaturge.content.recipe.dust;

import com.leclowndu93150.thaumaturge.api.recipe.DustTrigger;
import com.leclowndu93150.thaumaturge.api.recipe.DustTriggerPlacement;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.particle.SparkleParticleOptions;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import com.leclowndu93150.thaumaturge.network.effect.ClientboundParticleBatchPayload;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class DustTriggerFx {
    public static final double SPARKLE_TICKS_PER_BLOCK = Effects.SHELL_TICKS_PER_BLOCK;

    private static final double HAND_HEIGHT = 0.25;
    private static final double HAND_SIDE_OFFSET = 0.3;
    private static final double HAND_LOOK_OFFSET = 0.3;

    private static final int BURST_COUNT = 36;
    private static final int BURST_FLOATY_ODDS = 3;
    private static final double BURST_SCATTER = 0.025;
    private static final double BURST_FLOATY_TOSS = 0.04;
    private static final double BURST_ARC_TOSS = 0.12;
    private static final float BURST_FLOATY_DECAY_MIN = 0.6F;
    private static final float BURST_FLOATY_DECAY_SPAN = 0.2F;
    private static final float BURST_ARC_DECAY = 0.88F;
    private static final float BURST_FLOATY_GRAVITY = 0.12F;
    private static final float BURST_ARC_GRAVITY = 0.45F;
    private static final float BURST_SCALE = 0.5F;
    private static final int BURST_BASE_AGE = 10;
    private static final int BURST_DELAY_BOUND = 4;

    private DustTriggerFx() {}

    public static Vec3 posToHand(ServerPlayer player, InteractionHand hand) {
        HumanoidArm arm = hand == InteractionHand.MAIN_HAND
                ? player.getMainArm()
                : player.getMainArm().getOpposite();
        float bodyYaw = player.yBodyRot * Mth.DEG_TO_RAD;
        double sideSign = arm == HumanoidArm.RIGHT ? 1.0 : -1.0;
        double rightX = -Mth.cos(bodyYaw);
        double rightZ = -Mth.sin(bodyYaw);
        Vec3 look = player.getLookAngle();
        double x = player.getX() + rightX * HAND_SIDE_OFFSET * sideSign + look.x * HAND_LOOK_OFFSET;
        double y = player.getY() + player.getBbHeight() / 2.0 + HAND_HEIGHT + look.y * HAND_LOOK_OFFSET;
        double z = player.getZ() + rightZ * HAND_SIDE_OFFSET * sideSign + look.z * HAND_LOOK_OFFSET;
        return new Vec3(x, y, z);
    }

    public static void emitUseBurst(ServerLevel level, ServerPlayer player, InteractionHand hand, BlockPos pos) {
        RandomSource rand = level.getRandom();
        Vec3 handPos = LevelSpaces.frameOf(level, pos).toLocal(posToHand(player, hand));
        Vec3 toBlock = Vec3.atCenterOf(pos).subtract(handPos);
        List<ClientboundParticleBatchPayload.Spawn> particles = new ArrayList<>(BURST_COUNT);
        for (int i = 0; i < BURST_COUNT; i++) {
            boolean floaty = rand.nextInt(BURST_FLOATY_ODDS) == 0;
            float decay =
                    floaty ? BURST_FLOATY_DECAY_MIN + rand.nextFloat() * BURST_FLOATY_DECAY_SPAN : BURST_ARC_DECAY;
            double toss = floaty ? BURST_FLOATY_TOSS : BURST_ARC_TOSS;
            float gravity = floaty ? BURST_FLOATY_GRAVITY : BURST_ARC_GRAVITY;
            Vec3 velocity = toBlock.scale(1.0 - decay)
                    .add(
                            rand.nextGaussian() * BURST_SCATTER,
                            toss + rand.nextGaussian() * BURST_SCATTER,
                            rand.nextGaussian() * BURST_SCATTER);
            int delay = rand.nextInt(BURST_DELAY_BOUND);
            SparkleParticleOptions options = new SparkleParticleOptions(
                    Effects.dustSparkleColor(rand), BURST_SCALE, delay, decay, gravity, BURST_BASE_AGE, true);
            particles.add(new ClientboundParticleBatchPayload.Spawn(
                    options, handPos.x, handPos.y, handPos.z, velocity.x, velocity.y, velocity.z));
        }
        Effects.spawnBatch(level, pos, particles);
    }

    public static void emitTriggerSparkles(
            ServerLevel level,
            ServerPlayer player,
            BlockPos triggerPos,
            DustTrigger trigger,
            Vec3 hitStart,
            @Nullable DustTriggerPlacement placement) {
        if (placement == null || placement.facing() == null) {
            emitBlockSparkles(level, triggerPos, hitStart);
            return;
        }
        List<BlockPos> sparkles = trigger.sparkle(level, player, triggerPos, placement);
        if (sparkles == null || sparkles.isEmpty()) {
            return;
        }
        Effects.blockSparkles(level, sparkles).from(hitStart).send();
    }

    public static void emitBlockSparkles(ServerLevel level, BlockPos p, Vec3 start) {
        Effects.blockSparkles(level, p).from(start).send();
    }
}
