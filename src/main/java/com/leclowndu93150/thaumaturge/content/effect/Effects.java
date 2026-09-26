package com.leclowndu93150.thaumaturge.content.effect;

import com.leclowndu93150.thaumaturge.client.effect.instance.BeamPayloadIds;
import com.leclowndu93150.thaumaturge.content.particle.BlockRunesParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.BoreDebrisParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.BoreSparkleParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.BubbleParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.BurstParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.CurlyWispParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.FireMoteParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.LightningFlashParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.ScanGlyphParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.SlashParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.SmokeSpiralParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.SparkParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.SparkleParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.StabilizerRuneParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.TaintFumeParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.VentParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.WispFlameParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.WispyMoteParticleOptions;
import com.leclowndu93150.thaumaturge.network.effect.ClientboundBoreDigPayload;
import com.leclowndu93150.thaumaturge.network.effect.ClientboundSpawnParticlePayload;
import com.leclowndu93150.thaumaturge.network.effect.ClientboundStreamEffectPayload;
import com.leclowndu93150.thaumaturge.registry.TCParticles;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public final class Effects {
    static final double DEFAULT_RADIUS = 64.0;
    private static final double BORE_DIG_RADIUS = 32.0;

    private Effects() {}

    public static Bamf bamf(ServerLevel level, Vec3 pos) {
        return new Bamf(level, pos);
    }

    public static Bamf bamf(ServerLevel level, BlockPos pos) {
        return new Bamf(level, Vec3.atCenterOf(pos));
    }

    public static Sparkle sparkle(ServerLevel level, Vec3 pos) {
        return new Sparkle(level, pos);
    }

    public static Sparkle sparkle(ServerLevel level, BlockPos pos) {
        return new Sparkle(level, Vec3.atCenterOf(pos));
    }

    public static SimpleSparkle simpleSparkle(ServerLevel level, Vec3 pos) {
        return new SimpleSparkle(level, pos);
    }

    public static WispyMotes wispyMotes(ServerLevel level, Vec3 pos) {
        return new WispyMotes(level, pos);
    }

    public static CurlyWisp curlyWisp(ServerLevel level, Vec3 pos) {
        return new CurlyWisp(level, pos);
    }

    public static Vent vent(ServerLevel level, Vec3 pos) {
        return new Vent(level, pos, false);
    }

    public static Vent vent2(ServerLevel level, Vec3 pos) {
        return new Vent(level, pos, true);
    }

    public static ArcLightning arcLightning(ServerLevel level, Vec3 from) {
        return new ArcLightning(level, from);
    }

    public static ArcBolt arcBolt(ServerLevel level, Vec3 from) {
        return new ArcBolt(level, from);
    }

    public static BlockRunes blockRunes(ServerLevel level, Vec3 pos) {
        return new BlockRunes(level, pos, false);
    }

    public static BlockRunes blockRunes2(ServerLevel level, Vec3 pos) {
        return new BlockRunes(level, pos, true);
    }

    public static SmokeSpiral smokeSpiral(ServerLevel level, Vec3 pos) {
        return new SmokeSpiral(level, pos);
    }

    public static BeamWand beamWand(ServerLevel level, LivingEntity source) {
        return new BeamWand(level, source);
    }

    public static BeamBore beamBore(ServerLevel level, Vec3 source) {
        return new BeamBore(level, source);
    }

    public static BoreDebris boreDebris(ServerLevel level, Vec3 pos, BlockState state) {
        return new BoreDebris(level, pos, state);
    }

    public static BoreSparkle boreSparkle(ServerLevel level, Vec3 pos) {
        return new BoreSparkle(level, pos);
    }

    public static void boreDig(ServerLevel level, BlockPos target, Entity bore, int delay) {
        sendBoreDig(level, target, bore.getId(), bore.blockPosition(), delay);
    }

    public static void boreDig(ServerLevel level, BlockPos target, BlockPos bore, int delay) {
        sendBoreDig(level, target, BoreDebrisParticleOptions.NO_ENTITY, bore, delay);
    }

    private static void sendBoreDig(ServerLevel level, BlockPos target, int boreEntityId, BlockPos borePos, int delay) {
        PacketDistributor.sendToPlayersNear(level, null, target.getX(), target.getY(), target.getZ(), BORE_DIG_RADIUS, new ClientboundBoreDigPayload(target, boreEntityId, borePos, delay));
    }

    public static BoreStream boreStream(ServerLevel level, Vec3 source, Entity target) {
        return new BoreStream(level, source, target);
    }

    public static VoidStream voidStream(ServerLevel level, Vec3 source) {
        return new VoidStream(level, source);
    }

    public static FireMote fireMote(ServerLevel level, Vec3 pos) {
        return new FireMote(level, pos);
    }

    public static Alumentum alumentum(ServerLevel level, Vec3 pos) {
        return new Alumentum(level, pos);
    }

    public static Taint taint(ServerLevel level, Vec3 pos) {
        return new Taint(level, pos);
    }

    public static LightningFlash lightningFlash(ServerLevel level, Vec3 pos) {
        return new LightningFlash(level, pos);
    }

    public static Levitator levitator(ServerLevel level, Vec3 pos) {
        return new Levitator(level, pos);
    }

    public static Stabilizer stabilizer(ServerLevel level, Vec3 pos) {
        return new Stabilizer(level, pos);
    }

    public static GolemFly golemFly(ServerLevel level, Vec3 pos) {
        return new GolemFly(level, pos);
    }

    public static Pollution pollution(ServerLevel level, BlockPos pos) {
        return new Pollution(level, pos);
    }

    public static FocusCloud focusCloud(ServerLevel level, Vec3 pos) {
        return new FocusCloud(level, pos);
    }

    public static BlockMist blockMist(ServerLevel level, BlockPos pos) {
        return new BlockMist(level, pos);
    }

    public static BlockMistFlat blockMistFlat(ServerLevel level, BlockPos pos) {
        return new BlockMistFlat(level, pos);
    }

    public static WispParticles wispParticles(ServerLevel level, Vec3 pos) {
        return new WispParticles(level, pos);
    }

    public static CrucibleBubble crucibleBubble(ServerLevel level, Vec3 pos) {
        return new CrucibleBubble(level, pos);
    }

    public static CrucibleBoil crucibleBoil(ServerLevel level, Vec3 pos) {
        return new CrucibleBoil(level, pos);
    }

    public static CrucibleFroth crucibleFroth(ServerLevel level, Vec3 pos) {
        return new CrucibleFroth(level, pos);
    }

    public static CrucibleFrothDown crucibleFrothDown(ServerLevel level, Vec3 pos) {
        return new CrucibleFrothDown(level, pos);
    }

    public static Spark spark(ServerLevel level, Vec3 pos) {
        return new Spark(level, pos);
    }

    public static Burst burst(ServerLevel level, Vec3 pos) {
        return new Burst(level, pos);
    }

    public static EssentiaDrop essentiaDrop(ServerLevel level, Vec3 pos) {
        return new EssentiaDrop(level, pos);
    }

    public static JarSplash jarSplash(ServerLevel level, Vec3 pos) {
        return new JarSplash(level, pos);
    }

    public static LineSparkle lineSparkle(ServerLevel level, Vec3 pos) {
        return new LineSparkle(level, pos);
    }

    public static BlockSparkles blockSparkles(ServerLevel level, BlockPos pos) {
        return new BlockSparkles(level, pos);
    }

    public static PechsCurse pechsCurse(ServerLevel level, Vec3 pos) {
        return new PechsCurse(level, pos);
    }

    public static CultistSpawn cultistSpawn(ServerLevel level, Vec3 pos) {
        return new CultistSpawn(level, pos);
    }

    public static WispyMotesEntity wispyMotesEntity(ServerLevel level, Vec3 origin, int targetEntityId) {
        return new WispyMotesEntity(level, origin, targetEntityId);
    }

    public static WispyMotesOnBlock wispyMotesOnBlock(ServerLevel level, BlockPos pos) {
        return new WispyMotesOnBlock(level, pos);
    }

    public static FluxFume fluxFume(ServerLevel level, Vec3 pos) {
        return new FluxFume(level, pos);
    }

    public static FluxFume fluxFume(ServerLevel level, BlockPos pos) {
        return new FluxFume(level, Vec3.atCenterOf(pos));
    }

    public static FireMoteParticleOptions fireMoteData(RandomSource rand, double vx, double vy, double vz, float r, float g, float b, float alpha, float scale) {
        boolean translucent = rand.nextBoolean();
        return new FireMoteParticleOptions(vx, vy, vz, r, g, b, alpha, translucent ? scale / 3.0F : scale, translucent);
    }

    public static void spawn(ServerLevel level, ParticleOptions options, double x, double y, double z) {
        spawn(level, options, x, y, z, 0.0, 0.0, 0.0);
    }

    public static void spawn(ServerLevel level, ParticleOptions options, double x, double y, double z, double vx, double vy, double vz) {
        PacketDistributor.sendToPlayersNear(level, null, x, y, z, DEFAULT_RADIUS, new ClientboundSpawnParticlePayload(options, x, y, z, vx, vy, vz));
    }

    public static void scanGlyph(ServerPlayer viewer, double x, double y, double z, int color, int delay) {
        float brightness = (ARGB.red(color) + ARGB.green(color) + ARGB.blue(color)) / (3.0F * 255.0F);
        ScanGlyphParticleOptions options = new ScanGlyphParticleOptions(color, delay, brightness >= 0.25F);
        PacketDistributor.sendToPlayer(viewer, new ClientboundSpawnParticlePayload(options, x, y, z));
    }

    public static void slash(ServerLevel level, double x, double y, double z, double x2, double y2, double z2, int duration) {
        RandomSource rand = level.getRandom();
        double dx = x2 - x;
        double dy = y2 - y;
        double dz = z2 - z;
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        float yaw = 0.0F;
        float pitch = 0.0F;
        if (horizontal >= 1.0E-7) {
            yaw = (float) (Mth.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
            pitch = (float) (-(Mth.atan2(dy, horizontal) * 180.0 / Math.PI));
        }
        float roll = (float) Math.toRadians(rand.nextGaussian() * 20.0);
        SlashParticleOptions options = new SlashParticleOptions(duration, yaw, pitch, roll);
        spawn(level, options, x, y, z, dx / duration, dy / duration, dz / duration);
    }

    public static final class Bamf {
        private final ServerLevel level;
        private final Vec3 pos;
        private float r = 0.5F, g = 0.1F, b = 0.6F;
        private boolean sound = false;
        private boolean fancy = false;
        private Direction side = null;

        Bamf(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public Bamf color(float r, float g, float b) {
            this.r = r;
            this.g = g;
            this.b = b;
            return this;
        }

        public Bamf withSound() {
            this.sound = true;
            return this;
        }

        public Bamf fancy() {
            this.fancy = true;
            return this;
        }

        public Bamf side(Direction side) {
            this.side = side;
            return this;
        }

        public void send() {
            RandomSource rand = level.getRandom();
            if (sound) {
                level.playSound(null, pos.x, pos.y, pos.z, TCSounds.POOF.get(), SoundSource.BLOCKS, 0.4F, 1.0F + (float) rand.nextGaussian() * 0.05F);
            }
            int puffs = 6 + rand.nextInt(3) + 2;
            for (int a = 0; a < puffs; a++) {
                double vx = (0.05F + rand.nextFloat() * 0.05F) * (rand.nextBoolean() ? -1 : 1);
                double vy = (0.05F + rand.nextFloat() * 0.05F) * (rand.nextBoolean() ? -1 : 1);
                double vz = (0.05F + rand.nextFloat() * 0.05F) * (rand.nextBoolean() ? -1 : 1);
                if (side != null) {
                    vx += side.getStepX() * 0.1F;
                    vy += side.getStepY() * 0.1F;
                    vz += side.getStepZ() * 0.1F;
                }
                float pr = Mth.clamp(r * (1.0F + (float) rand.nextGaussian() * 0.1F), 0.0F, 1.0F);
                float pg = Mth.clamp(g * (1.0F + (float) rand.nextGaussian() * 0.1F), 0.0F, 1.0F);
                float pb = Mth.clamp(b * (1.0F + (float) rand.nextGaussian() * 0.1F), 0.0F, 1.0F);
                spawn(level, TCParticles.colorOf(TCParticles.PUFF, pr, pg, pb), pos.x + vx * 2.0, pos.y + vy * 2.0, pos.z + vz * 2.0, vx / 2.0, vy / 2.0, vz / 2.0);
            }
            if (fancy) {
                int motes = 2 + rand.nextInt(3);
                for (int a = 0; a < motes; a++) {
                    double vx = (0.025F + rand.nextFloat() * 0.025F) * (rand.nextBoolean() ? -1 : 1);
                    double vy = (0.025F + rand.nextFloat() * 0.025F) * (rand.nextBoolean() ? -1 : 1);
                    double vz = (0.025F + rand.nextFloat() * 0.025F) * (rand.nextBoolean() ? -1 : 1);
                    wispyMotes(level, new Vec3(pos.x + vx * 2.0, pos.y + vy * 2.0, pos.z + vz * 2.0)).motion(vx, vy, vz).age(15 + rand.nextInt(10)).randomColor().gravity(-0.01F).send();
                }
                spawn(level, TCParticles.colorOf(TCParticles.FLASH, 1.0F, 0.9F, 1.0F), pos.x, pos.y, pos.z);
            }
            int wisps = (fancy ? 2 : 0) + rand.nextInt(3);
            for (int a = 0; a < wisps; a++) {
                curlyWisp(level, pos).color((0.9F + rand.nextFloat() * 0.1F + r) / 2.0F, (0.1F + g) / 2.0F, (0.5F + rand.nextFloat() * 0.1F + b) / 2.0F).alpha(0.75F).side(side).seed(a).send();
            }
        }
    }

    public static final class Sparkle {
        private final ServerLevel level;
        private final Vec3 pos;
        private float r = 1.0F, g = 1.0F, b = 1.0F;

        Sparkle(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public Sparkle color(float r, float g, float b) {
            this.r = r;
            this.g = g;
            this.b = b;
            return this;
        }

        public void send() {
            RandomSource rand = level.getRandom();
            if (rand.nextInt(6) >= 4)
                return;
            SparkleParticleOptions options = new SparkleParticleOptions(ARGB.colorFromFloat(1.0F, r, g, b), 0.6F + rand.nextFloat() * 0.2F, 0, 1.0F, 0.0F, 2, false);
            spawn(level, options, pos.x, pos.y, pos.z);
        }
    }

    public static final class SimpleSparkle {
        private final ServerLevel level;
        private final Vec3 pos;
        private double vx, vy, vz;
        private float scale = 0.4F;
        private float r = 1.0F, g = 1.0F, b = 1.0F;
        private int delay = 0;
        private float decay = 0.98F;
        private float gravity = 0.0F;
        private int baseAge = 16;

        SimpleSparkle(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public SimpleSparkle motion(double vx, double vy, double vz) {
            this.vx = vx;
            this.vy = vy;
            this.vz = vz;
            return this;
        }

        public SimpleSparkle color(float r, float g, float b) {
            this.r = r;
            this.g = g;
            this.b = b;
            return this;
        }

        public SimpleSparkle scale(float scale) {
            this.scale = scale;
            return this;
        }

        public SimpleSparkle delay(int delay) {
            this.delay = delay;
            return this;
        }

        public SimpleSparkle decay(float decay) {
            this.decay = decay;
            return this;
        }

        public SimpleSparkle gravity(float gravity) {
            this.gravity = gravity;
            return this;
        }

        public SimpleSparkle baseAge(int baseAge) {
            this.baseAge = baseAge;
            return this;
        }

        public void send() {
            SparkleParticleOptions options = new SparkleParticleOptions(ARGB.colorFromFloat(1.0F, r, g, b), scale, delay, decay, gravity, baseAge, true);
            spawn(level, options, pos.x, pos.y, pos.z, vx, vy, vz);
        }
    }

    public static final class WispyMotes {
        private final ServerLevel level;
        private final Vec3 pos;
        private double vx, vy, vz;
        private int age = 30;
        private float r = 0.5F, g = 0.5F, b = 0.5F;
        private boolean randomColor = false;
        private float gravity = 0.0F;

        WispyMotes(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public WispyMotes motion(double vx, double vy, double vz) {
            this.vx = vx;
            this.vy = vy;
            this.vz = vz;
            return this;
        }

        public WispyMotes age(int age) {
            this.age = age;
            return this;
        }

        public WispyMotes color(float r, float g, float b) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.randomColor = false;
            return this;
        }

        public WispyMotes randomColor() {
            this.randomColor = true;
            return this;
        }

        public WispyMotes gravity(float gravity) {
            this.gravity = gravity;
            return this;
        }

        public void send() {
            RandomSource rand = level.getRandom();
            float cr = randomColor ? 0.25F + rand.nextFloat() * 0.75F : r;
            float cg = randomColor ? 0.25F + rand.nextFloat() * 0.75F : g;
            float cb = randomColor ? 0.25F + rand.nextFloat() * 0.75F : b;
            WispyMoteParticleOptions options = new WispyMoteParticleOptions(ARGB.colorFromFloat(1.0F, cr, cg, cb), age, gravity, WispyMoteParticleOptions.NO_ENTITY);
            spawn(level, options, pos.x, pos.y, pos.z, vx, vy, vz);
        }
    }

    public static final class CurlyWisp {
        private final ServerLevel level;
        private final Vec3 pos;
        private double vx, vy, vz;
        private float scale = 1.0F;
        private float r = 1.0F, g = 1.0F, b = 1.0F;
        private float alpha = 1.0F;
        private Direction side = null;
        private int seed = 0;
        private int delay = 0;

        CurlyWisp(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public CurlyWisp motion(double vx, double vy, double vz) {
            this.vx = vx;
            this.vy = vy;
            this.vz = vz;
            return this;
        }

        public CurlyWisp scale(float scale) {
            this.scale = scale;
            return this;
        }

        public CurlyWisp color(float r, float g, float b) {
            this.r = r;
            this.g = g;
            this.b = b;
            return this;
        }

        public CurlyWisp alpha(float alpha) {
            this.alpha = alpha;
            return this;
        }

        public CurlyWisp side(Direction side) {
            this.side = side;
            return this;
        }

        public CurlyWisp seed(int seed) {
            this.seed = seed;
            return this;
        }

        public CurlyWisp delay(int delay) {
            this.delay = delay;
            return this;
        }

        public void send() {
            RandomSource rand = level.getRandom();
            double dx = vx + (0.0025F + rand.nextFloat() * 0.005F) * (rand.nextBoolean() ? -1 : 1);
            double dy = vy + (0.0025F + rand.nextFloat() * 0.005F) * (rand.nextBoolean() ? -1 : 1);
            double dz = vz + (0.0025F + rand.nextFloat() * 0.005F) * (rand.nextBoolean() ? -1 : 1);
            if (side != null) {
                dx += side.getStepX() * 0.025F;
                dy += side.getStepY() * 0.025F;
                dz += side.getStepZ() * 0.025F;
            }
            CurlyWispParticleOptions options = new CurlyWispParticleOptions(ARGB.colorFromFloat(1.0F, r, g, b), alpha, scale, delay, seed);
            spawn(level, options, pos.x + dx * 5.0, pos.y + dy * 5.0, pos.z + dz * 5.0, dx, dy, dz);
        }
    }

    public static final class Vent {
        private final ServerLevel level;
        private final Vec3 pos;
        private final boolean variant;
        private double vx, vy, vz;
        private int color = 0xFFFFFF;
        private float scale = 1.0F;
        private boolean spawnFlame = false;

        Vent(ServerLevel level, Vec3 pos, boolean variant) {
            this.level = level;
            this.pos = pos;
            this.variant = variant;
        }

        public Vent motion(double vx, double vy, double vz) {
            this.vx = vx;
            this.vy = vy;
            this.vz = vz;
            return this;
        }

        public Vent color(int color) {
            this.color = color;
            return this;
        }

        public Vent scale(float scale) {
            this.scale = scale;
            return this;
        }

        public Vent withFlame() {
            this.spawnFlame = true;
            return this;
        }

        public void send() {
            spawn(level, new VentParticleOptions(vx, vy, vz, color, scale, variant), pos.x, pos.y, pos.z);
            RandomSource rand = level.getRandom();
            if (spawnFlame && rand.nextInt(6) < 2) {
                WispFlameParticleOptions flame = new WispFlameParticleOptions(ARGB.colorFromFloat(1.0F, 1.0F, 0.7F, 0.2F), 0.9F, 0.25F + rand.nextFloat() * 0.1F, 0.25F, 0);
                spawn(level, flame, pos.x, pos.y, pos.z, vx / 2.0, vy / 2.0, vz / 2.0);
            }
        }
    }

    public static final class BlockRunes {
        private final ServerLevel level;
        private final Vec3 pos;
        private final boolean variant;
        private float r = 1.0F, g = 1.0F, b = 1.0F;
        private int duration = 30;
        private float gravity = 0.0F;

        BlockRunes(ServerLevel level, Vec3 pos, boolean variant) {
            this.level = level;
            this.pos = pos;
            this.variant = variant;
        }

        public BlockRunes color(float r, float g, float b) {
            this.r = r;
            this.g = g;
            this.b = b;
            return this;
        }

        public BlockRunes duration(int duration) {
            this.duration = duration;
            return this;
        }

        public BlockRunes gravity(float gravity) {
            this.gravity = gravity;
            return this;
        }

        public void send() {
            spawn(level, new BlockRunesParticleOptions(r, g, b, duration, gravity, variant), pos.x + 0.5, pos.y + 0.5, pos.z + 0.5);
        }
    }

    public static final class SmokeSpiral {
        private final ServerLevel level;
        private final Vec3 pos;
        private float radius = 1.0F;
        private int start = 0;
        private int minY = 0;
        private int color = 0xFFFFFF;

        SmokeSpiral(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public SmokeSpiral radius(float radius) {
            this.radius = radius;
            return this;
        }

        public SmokeSpiral start(int startDeg) {
            this.start = startDeg;
            return this;
        }

        public SmokeSpiral minY(int minY) {
            this.minY = minY;
            return this;
        }

        public SmokeSpiral color(int color) {
            this.color = color;
            return this;
        }

        public void send() {
            float cr = ARGB.red(color) / 255.0F;
            float cg = ARGB.green(color) / 255.0F;
            float cb = ARGB.blue(color) / 255.0F;
            spawn(level, new SmokeSpiralParticleOptions(radius, start, minY, cr, cg, cb), pos.x, pos.y, pos.z);
        }
    }

    public static final class BeamWand {
        private final ServerLevel level;
        private final LivingEntity source;
        private Vec3 target = null;
        private int color = 0xFFFFFF;
        private int age = 20;
        private int beamType = 0;
        private float endMod = 1.0F;
        private boolean reverse = false;

        BeamWand(ServerLevel level, LivingEntity source) {
            this.level = level;
            this.source = source;
        }

        public BeamWand to(Vec3 target) {
            this.target = target;
            return this;
        }

        public BeamWand color(int color) {
            this.color = color;
            return this;
        }

        public BeamWand age(int age) {
            this.age = age;
            return this;
        }

        public BeamWand type(int beamType) {
            this.beamType = beamType;
            return this;
        }

        public BeamWand endMod(float endMod) {
            this.endMod = endMod;
            return this;
        }

        public BeamWand reverse(boolean reverse) {
            this.reverse = reverse;
            return this;
        }

        public void send() {
            if (target == null)
                return;
            ClientboundStreamEffectPayload payload = ClientboundStreamEffectPayload.beam(source.getX(), source.getY(), source.getZ(), target.x, target.y, target.z, color, age, beamType, endMod,
                    reverse, source.getId(), false);
            PacketDistributor.sendToPlayersNear(level, null, source.getX(), source.getY(), source.getZ(), DEFAULT_RADIUS, payload);
        }
    }

    public static final class BeamBore {
        private final ServerLevel level;
        private final Vec3 source;
        private Vec3 target = null;
        private int color = 0xFFFFFF;
        private int age = 20;
        private int beamType = 0;
        private float endMod = 1.0F;
        private boolean reverse = false;

        BeamBore(ServerLevel level, Vec3 source) {
            this.level = level;
            this.source = source;
        }

        public BeamBore to(Vec3 target) {
            this.target = target;
            return this;
        }

        public BeamBore color(int color) {
            this.color = color;
            return this;
        }

        public BeamBore age(int age) {
            this.age = age;
            return this;
        }

        public BeamBore type(int beamType) {
            this.beamType = beamType;
            return this;
        }

        public BeamBore endMod(float endMod) {
            this.endMod = endMod;
            return this;
        }

        public BeamBore reverse(boolean reverse) {
            this.reverse = reverse;
            return this;
        }

        public void send() {
            if (target == null)
                return;
            ClientboundStreamEffectPayload payload = ClientboundStreamEffectPayload.beam(source.x, source.y, source.z, target.x, target.y, target.z, color, age, beamType, endMod, reverse,
                    BeamPayloadIds.NO_ENTITY, true);
            PacketDistributor.sendToPlayersNear(level, null, source.x, source.y, source.z, DEFAULT_RADIUS, payload);
        }
    }

    public static final class ArcLightning {
        private final ServerLevel level;
        private final Vec3 from;
        private Vec3 to = null;
        private int color = 0xFFFFFF;
        private float gravity = 0.1F;

        ArcLightning(ServerLevel level, Vec3 from) {
            this.level = level;
            this.from = from;
        }

        public ArcLightning to(Vec3 to) {
            this.to = to;
            return this;
        }

        public ArcLightning color(int color) {
            this.color = color;
            return this;
        }

        public ArcLightning gravity(float gravity) {
            this.gravity = gravity;
            return this;
        }

        public void send() {
            if (to == null)
                return;
            EffectDispatch.spawnArc(level, from, to, color, gravity);
        }
    }

    public static final class ArcBolt {
        private final ServerLevel level;
        private final Vec3 from;
        private Vec3 to = null;
        private int color = 0xFFFFFF;
        private float width = 1.0F;

        ArcBolt(ServerLevel level, Vec3 from) {
            this.level = level;
            this.from = from;
        }

        public ArcBolt to(Vec3 to) {
            this.to = to;
            return this;
        }

        public ArcBolt color(int color) {
            this.color = color;
            return this;
        }

        public ArcBolt width(float width) {
            this.width = width;
            return this;
        }

        public void send() {
            if (to == null)
                return;
            EffectDispatch.spawnBolt(level, from, to, color, width);
        }
    }

    public static final class FireMote {
        private final ServerLevel level;
        private final Vec3 pos;
        private double vx, vy, vz;
        private float r = 1.0F, g = 1.0F, b = 1.0F;
        private float alpha = 1.0F;
        private float scale = 1.0F;

        FireMote(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public FireMote motion(double vx, double vy, double vz) {
            this.vx = vx;
            this.vy = vy;
            this.vz = vz;
            return this;
        }

        public FireMote color(float r, float g, float b) {
            this.r = r;
            this.g = g;
            this.b = b;
            return this;
        }

        public FireMote alpha(float alpha) {
            this.alpha = alpha;
            return this;
        }

        public FireMote scale(float scale) {
            this.scale = scale;
            return this;
        }

        public void send() {
            spawn(level, fireMoteData(level.getRandom(), vx, vy, vz, r, g, b, alpha, scale), pos.x, pos.y, pos.z);
        }
    }

    public static final class Alumentum {
        private final ServerLevel level;
        private final Vec3 pos;
        private double vx, vy, vz;
        private float r = 1.0F, g = 1.0F, b = 1.0F;
        private float alpha = 1.0F;
        private float scale = 1.0F;

        Alumentum(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public Alumentum motion(double vx, double vy, double vz) {
            this.vx = vx;
            this.vy = vy;
            this.vz = vz;
            return this;
        }

        public Alumentum color(float r, float g, float b) {
            this.r = r;
            this.g = g;
            this.b = b;
            return this;
        }

        public Alumentum alpha(float alpha) {
            this.alpha = alpha;
            return this;
        }

        public Alumentum scale(float scale) {
            this.scale = scale;
            return this;
        }

        public void send() {
            spawn(level, new FireMoteParticleOptions(vx, vy, vz, r, g, b, alpha, scale, true), pos.x, pos.y, pos.z);
        }
    }

    public static final class Taint {
        private final ServerLevel level;
        private final Vec3 pos;
        private double vx, vy, vz;
        private float scale = 1.0F;

        Taint(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public Taint motion(double vx, double vy, double vz) {
            this.vx = vx;
            this.vy = vy;
            this.vz = vz;
            return this;
        }

        public Taint scale(float scale) {
            this.scale = scale;
            return this;
        }

        public void send() {
            spawn(level, new TaintFumeParticleOptions(TaintFumeParticleOptions.RANDOM_COLOR, scale), pos.x, pos.y, pos.z, vx, vy, vz);
        }
    }

    public static final class LightningFlash {
        private final ServerLevel level;
        private final Vec3 pos;
        private float r = 1.0F, g = 1.0F, b = 1.0F;
        private float alpha = 1.0F;
        private float scale = 1.0F;

        LightningFlash(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public LightningFlash color(float r, float g, float b) {
            this.r = r;
            this.g = g;
            this.b = b;
            return this;
        }

        public LightningFlash alpha(float alpha) {
            this.alpha = alpha;
            return this;
        }

        public LightningFlash scale(float scale) {
            this.scale = scale;
            return this;
        }

        public void send() {
            spawn(level, new LightningFlashParticleOptions(ARGB.colorFromFloat(1.0F, r, g, b), alpha, scale), pos.x, pos.y, pos.z);
        }
    }

    public static final class Levitator {
        private final ServerLevel level;
        private final Vec3 pos;
        private double vx, vy, vz;

        Levitator(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public Levitator motion(double vx, double vy, double vz) {
            this.vx = vx;
            this.vy = vy;
            this.vz = vz;
            return this;
        }

        public void send() {
            spawn(level, TCParticles.LEVITATOR_MIST.get(), pos.x, pos.y, pos.z, vx, vy, vz);
        }
    }

    public static final class Stabilizer {
        private final ServerLevel level;
        private final Vec3 pos;
        private double vx, vy, vz;
        private int life = 20;

        Stabilizer(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public Stabilizer motion(double vx, double vy, double vz) {
            this.vx = vx;
            this.vy = vy;
            this.vz = vz;
            return this;
        }

        public Stabilizer life(int life) {
            this.life = life;
            return this;
        }

        public void send() {
            spawn(level, new StabilizerRuneParticleOptions(life), pos.x, pos.y, pos.z, vx, vy, vz);
        }
    }

    public static final class GolemFly {
        private final ServerLevel level;
        private final Vec3 pos;
        private double vx, vy, vz;

        GolemFly(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public GolemFly motion(double vx, double vy, double vz) {
            this.vx = vx;
            this.vy = vy;
            this.vz = vz;
            return this;
        }

        public void send() {
            spawn(level, TCParticles.GOLEM_TRAIL.get(), pos.x, pos.y, pos.z, vx, vy, vz);
        }
    }

    public static final class Pollution {
        private final ServerLevel level;
        private final BlockPos pos;

        Pollution(ServerLevel level, BlockPos pos) {
            this.level = level;
            this.pos = pos;
        }

        public void send() {
            RandomSource rand = level.getRandom();
            spawn(level, TCParticles.POLLUTION_FUME.get(), pos.getX() + 0.2F + rand.nextFloat() * 0.6F, pos.getY() + 0.2F + rand.nextFloat() * 0.6F, pos.getZ() + 0.2F + rand.nextFloat() * 0.6F);
        }
    }

    public static final class FocusCloud {
        private final ServerLevel level;
        private final Vec3 pos;
        private double vx, vy, vz;
        private int color = 0xFFFFFF;

        FocusCloud(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public FocusCloud motion(double vx, double vy, double vz) {
            this.vx = vx;
            this.vy = vy;
            this.vz = vz;
            return this;
        }

        public FocusCloud color(int color) {
            this.color = color;
            return this;
        }

        public void send() {
            spawn(level, TCParticles.colorOf(TCParticles.FOCUS_CLOUD, color), pos.x, pos.y, pos.z, vx, vy, vz);
        }
    }

    public static final class BlockMist {
        private final ServerLevel level;
        private final BlockPos pos;
        private int color = 0xFFFFFF;

        BlockMist(ServerLevel level, BlockPos pos) {
            this.level = level;
            this.pos = pos;
        }

        public BlockMist color(int color) {
            this.color = color;
            return this;
        }

        public void send() {
            RandomSource rand = level.getRandom();
            AABB bs = level.getBlockState(pos).getShape(level, pos).bounds();
            for (int a = 0; a < 8; a++) {
                double x = pos.getX() + bs.minX + rand.nextFloat() * (bs.maxX - bs.minX);
                double y = pos.getY() + bs.minY + rand.nextFloat() * (bs.maxY - bs.minY);
                double z = pos.getZ() + bs.minZ + rand.nextFloat() * (bs.maxZ - bs.minZ);
                spawn(level, TCParticles.colorOf(TCParticles.BLOCK_MIST, color), x, y, z, rand.nextGaussian() * 0.01, rand.nextFloat() * 0.075, rand.nextGaussian() * 0.01);
            }
        }
    }

    public static final class BlockMistFlat {
        private final ServerLevel level;
        private final BlockPos pos;
        private int color = 0xFFFFFF;

        BlockMistFlat(ServerLevel level, BlockPos pos) {
            this.level = level;
            this.pos = pos;
        }

        public BlockMistFlat color(int color) {
            this.color = color;
            return this;
        }

        public void send() {
            RandomSource rand = level.getRandom();
            for (int a = 0; a < 6; a++) {
                spawn(level, TCParticles.colorOf(TCParticles.MIST_FLAT, color), pos.getX() + rand.nextFloat(), pos.getY() + rand.nextFloat() * 0.125F, pos.getZ() + rand.nextFloat(),
                        (rand.nextFloat() - rand.nextFloat()) * 0.005, 0.005, (rand.nextFloat() - rand.nextFloat()) * 0.005);
            }
        }
    }

    public static final class WispParticles {
        private final ServerLevel level;
        private final Vec3 pos;
        private double vx, vy, vz;
        private int color = 0xFFFFFF;
        private int delay = 0;

        WispParticles(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public WispParticles motion(double vx, double vy, double vz) {
            this.vx = vx;
            this.vy = vy;
            this.vz = vz;
            return this;
        }

        public WispParticles color(int color) {
            this.color = color;
            return this;
        }

        public WispParticles delay(int delay) {
            this.delay = delay;
            return this;
        }

        public void send() {
            RandomSource rand = level.getRandom();
            WispFlameParticleOptions options = new WispFlameParticleOptions(color, 0.5F, 1.0F + rand.nextFloat() * 0.25F, 0.05F, delay);
            spawn(level, options, pos.x, pos.y, pos.z, vx, vy, vz);
        }
    }

    public static final class WispyMotesOnBlock {
        private final ServerLevel level;
        private final BlockPos pos;
        private int age = 30;
        private float gravity = 0.0F;

        WispyMotesOnBlock(ServerLevel level, BlockPos pos) {
            this.level = level;
            this.pos = pos;
        }

        public WispyMotesOnBlock age(int age) {
            this.age = age;
            return this;
        }

        public WispyMotesOnBlock gravity(float gravity) {
            this.gravity = gravity;
            return this;
        }

        public void send() {
            RandomSource rand = level.getRandom();
            wispyMotes(level, new Vec3(pos.getX() + rand.nextFloat(), pos.getY(), pos.getZ() + rand.nextFloat())).age(age)
                    .color(0.4F + rand.nextFloat() * 0.6F, 0.6F + rand.nextFloat() * 0.4F, 0.6F + rand.nextFloat() * 0.4F).gravity(gravity).send();
        }
    }

    public static final class CrucibleBubble {
        private final ServerLevel level;
        private final Vec3 pos;
        private float r = 1.0F, g = 1.0F, b = 1.0F;

        CrucibleBubble(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public CrucibleBubble color(float r, float g, float b) {
            this.r = r;
            this.g = g;
            this.b = b;
            return this;
        }

        public void send() {
            RandomSource rand = level.getRandom();
            BubbleParticleOptions options = new BubbleParticleOptions(ARGB.colorFromFloat(1.0F, r, g, b), 1.0F, rand.nextFloat() * 0.3F + 0.3F, 15 + rand.nextInt(10), -0.001F, false);
            spawn(level, options, pos.x, pos.y, pos.z);
        }
    }

    public static final class CrucibleBoil {
        private final ServerLevel level;
        private final Vec3 pos;
        private float r = 1.0F, g = 1.0F, b = 1.0F;
        private int heat = 1;

        CrucibleBoil(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public CrucibleBoil color(float r, float g, float b) {
            this.r = r;
            this.g = g;
            this.b = b;
            return this;
        }

        public CrucibleBoil heat(int heat) {
            this.heat = heat;
            return this;
        }

        public void send() {
            RandomSource rand = level.getRandom();
            for (int a = 0; a < 2; a++) {
                BubbleParticleOptions options = new BubbleParticleOptions(ARGB.colorFromFloat(1.0F, r, g, b), 1.0F, rand.nextFloat() * 0.3F + 0.2F, (int) (7.0 + 8.0 / (rand.nextDouble() * 0.8 + 0.2)),
                        -0.025F * heat, false);
                spawn(level, options, pos.x + 0.2 + rand.nextFloat() * 0.6, pos.y, pos.z + 0.2 + rand.nextFloat() * 0.6, 0.0, 0.002, 0.0);
            }
        }
    }

    public static final class CrucibleFroth {
        private final ServerLevel level;
        private final Vec3 pos;

        CrucibleFroth(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public void send() {
            RandomSource rand = level.getRandom();
            BubbleParticleOptions options = new BubbleParticleOptions(ARGB.colorFromFloat(1.0F, 0.5F, 0.5F, 0.7F), 1.0F, rand.nextFloat() * 0.2F + 0.2F, 4 + rand.nextInt(3), 0.1F, false);
            spawn(level, options, pos.x, pos.y, pos.z);
        }
    }

    public static final class CrucibleFrothDown {
        private final ServerLevel level;
        private final Vec3 pos;

        CrucibleFrothDown(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public void send() {
            RandomSource rand = level.getRandom();
            BubbleParticleOptions options = new BubbleParticleOptions(ARGB.colorFromFloat(1.0F, 0.25F, 0.0F, 0.75F), 0.8F, rand.nextFloat() * 0.2F + 0.4F, 12 + rand.nextInt(12), 0.05F, true);
            spawn(level, options, pos.x, pos.y, pos.z);
        }
    }

    public static final class Spark {
        private final ServerLevel level;
        private final Vec3 pos;
        private float size = 1.0F;
        private float r = 1.0F, g = 1.0F, b = 1.0F;
        private float alpha = 1.0F;

        Spark(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public Spark size(float size) {
            this.size = size;
            return this;
        }

        public Spark color(float r, float g, float b) {
            this.r = r;
            this.g = g;
            this.b = b;
            return this;
        }

        public Spark alpha(float alpha) {
            this.alpha = alpha;
            return this;
        }

        public void send() {
            spawn(level, new SparkParticleOptions(ARGB.colorFromFloat(1.0F, r, g, b), alpha, size), pos.x, pos.y, pos.z);
        }
    }

    public static final class Burst {
        private final ServerLevel level;
        private final Vec3 pos;
        private float size = 1.0F;

        Burst(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public Burst size(float size) {
            this.size = size;
            return this;
        }

        public void send() {
            spawn(level, new BurstParticleOptions(size), pos.x, pos.y, pos.z);
        }
    }

    public static final class EssentiaDrop {
        private final ServerLevel level;
        private final Vec3 pos;
        private float r = 1.0F, g = 1.0F, b = 1.0F;
        private float alpha = 1.0F;

        EssentiaDrop(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public EssentiaDrop color(float r, float g, float b) {
            this.r = r;
            this.g = g;
            this.b = b;
            return this;
        }

        public EssentiaDrop alpha(float alpha) {
            this.alpha = alpha;
            return this;
        }

        public void send() {
            RandomSource rand = level.getRandom();
            BubbleParticleOptions options = new BubbleParticleOptions(ARGB.colorFromFloat(1.0F, r, g, b), alpha, 0.4F + rand.nextFloat() * 0.2F, 20 + rand.nextInt(10), 0.01F, false);
            spawn(level, options, pos.x, pos.y, pos.z, rand.nextGaussian() * 0.005, rand.nextGaussian() * 0.005, rand.nextGaussian() * 0.005);
        }
    }

    public static final class JarSplash {
        private static final int JAR_COLOR = 0x286176;
        private final ServerLevel level;
        private final Vec3 pos;

        JarSplash(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public void send() {
            RandomSource rand = level.getRandom();
            BubbleParticleOptions options = new BubbleParticleOptions(JAR_COLOR, 0.5F, 0.4F + rand.nextFloat() * 0.3F, 20 + rand.nextInt(10), 0.3F, true);
            spawn(level, options, pos.x + rand.nextGaussian() * 0.075, pos.y, pos.z + rand.nextGaussian() * 0.075, rand.nextGaussian() * 0.015, 0.075 + rand.nextFloat() * 0.05,
                    rand.nextGaussian() * 0.015);
        }
    }

    public static final class LineSparkle {
        private final ServerLevel level;
        private final Vec3 pos;
        private double vx, vy, vz;
        private float scale = 0.4F;
        private float r = 1.0F, g = 1.0F, b = 1.0F;
        private int delay = 0;
        private float decay = 0.98F;
        private float gravity = 0.0F;
        private int baseAge = 16;

        LineSparkle(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public LineSparkle motion(double vx, double vy, double vz) {
            this.vx = vx;
            this.vy = vy;
            this.vz = vz;
            return this;
        }

        public LineSparkle color(float r, float g, float b) {
            this.r = r;
            this.g = g;
            this.b = b;
            return this;
        }

        public LineSparkle scale(float scale) {
            this.scale = scale;
            return this;
        }

        public LineSparkle delay(int delay) {
            this.delay = delay;
            return this;
        }

        public LineSparkle decay(float decay) {
            this.decay = decay;
            return this;
        }

        public LineSparkle gravity(float gravity) {
            this.gravity = gravity;
            return this;
        }

        public LineSparkle baseAge(int baseAge) {
            this.baseAge = baseAge;
            return this;
        }

        public void send() {
            SparkleParticleOptions options = new SparkleParticleOptions(ARGB.colorFromFloat(1.0F, r, g, b), scale, delay, decay, gravity, baseAge, false);
            spawn(level, options, pos.x, pos.y, pos.z, vx, vy, vz);
        }
    }

    public static final class BlockSparkles {
        private final ServerLevel level;
        private final BlockPos pos;
        private Vec3 source = null;

        BlockSparkles(ServerLevel level, BlockPos pos) {
            this.level = level;
            this.pos = pos;
        }

        public BlockSparkles from(Vec3 source) {
            this.source = source;
            return this;
        }

        public void send() {
            RandomSource rand = level.getRandom();
            AABB bs = level.getBlockState(pos).getShape(level, pos).bounds().inflate(0.1);
            int num = (int) (((bs.getXsize() + bs.getYsize() + bs.getZsize()) / 3.0) * 20.0);
            if (num < 1)
                num = 1;
            Vec3 start = source != null ? source : new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            for (Direction face : Direction.values()) {
                BlockPos neighbor = pos.relative(face);
                var neighborState = level.getBlockState(neighbor);
                if (neighborState.isSolidRender() && neighborState.isFaceSturdy(level, neighbor, face.getOpposite()))
                    continue;
                boolean rx = face.getStepX() == 0;
                boolean ry = face.getStepY() == 0;
                boolean rz = face.getStepZ() == 0;
                double mx = 0.5 + face.getStepX() * 0.51;
                double my = 0.5 + face.getStepY() * 0.51;
                double mz = 0.5 + face.getStepZ() * 0.51;
                for (int a = 0; a < num * 2; a++) {
                    double x = mx;
                    double y = my;
                    double z = mz;
                    if (rx)
                        x = mx + rand.nextGaussian() * 0.6;
                    if (ry)
                        y = my + rand.nextGaussian() * 0.6;
                    if (rz)
                        z = mz + rand.nextGaussian() * 0.6;
                    x = Mth.clamp(x, bs.minX, bs.maxX);
                    y = Mth.clamp(y, bs.minY, bs.maxY);
                    z = Mth.clamp(z, bs.minZ, bs.maxZ);
                    float r = 1.0F;
                    float g = (189 + rand.nextInt(67)) / 255.0F;
                    float b = (64 + rand.nextInt(192)) / 255.0F;
                    double wx = pos.getX() + x;
                    double wy = pos.getY() + y;
                    double wz = pos.getZ() + z;
                    double dist = start.distanceTo(new Vec3(wx, wy, wz));
                    int delay = rand.nextInt(5) + (int) (dist * 16.0);
                    float sparkScale = 0.4F + (float) rand.nextGaussian() * 0.1F;
                    simpleSparkle(level, new Vec3(wx, wy, wz)).motion(0.0, 0.0025, 0.0).scale(sparkScale).color(r, g, b).delay(delay).decay(1.0F).gravity(0.01F).baseAge(16).send();
                }
            }
        }
    }

    public static final class PechsCurse {
        private final ServerLevel level;
        private final Vec3 pos;

        PechsCurse(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public void send() {
            RandomSource rand = level.getRandom();
            spawn(level, TCParticles.PECH_CURSE.get(), pos.x, pos.y, pos.z);
            wispyMotes(level, pos).age(10 + rand.nextInt(10)).randomColor().gravity(-0.01F).send();
        }
    }

    public static final class CultistSpawn {
        private final ServerLevel level;
        private final Vec3 pos;
        private double vx, vy, vz;

        CultistSpawn(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public CultistSpawn motion(double vx, double vy, double vz) {
            this.vx = vx;
            this.vy = vy;
            this.vz = vz;
            return this;
        }

        public void send() {
            spawn(level, TCParticles.CRIMSON_SMOKE.get(), pos.x, pos.y, pos.z, vx, vy, vz);
        }
    }

    public static final class WispyMotesEntity {
        private final ServerLevel level;
        private final Vec3 origin;
        private final int targetEntityId;
        private float r = 1.0F, g = 1.0F, b = 1.0F;

        WispyMotesEntity(ServerLevel level, Vec3 origin, int targetEntityId) {
            this.level = level;
            this.origin = origin;
            this.targetEntityId = targetEntityId;
        }

        public WispyMotesEntity color(float r, float g, float b) {
            this.r = r;
            this.g = g;
            this.b = b;
            return this;
        }

        public void send() {
            WispyMoteParticleOptions options = new WispyMoteParticleOptions(ARGB.colorFromFloat(1.0F, r, g, b), 30, 0.2F, targetEntityId);
            spawn(level, options, origin.x, origin.y, origin.z);
        }
    }

    public static final class BoreDebris {
        private final ServerLevel level;
        private final Vec3 pos;
        private final BlockState state;
        private Vec3 target = null;
        private double sx = 0.0;
        private double sy = 0.0;
        private double sz = 0.0;

        BoreDebris(ServerLevel level, Vec3 pos, BlockState state) {
            this.level = level;
            this.pos = pos;
            this.state = state;
        }

        public BoreDebris to(Vec3 target) {
            this.target = target;
            return this;
        }

        public BoreDebris motion(double sx, double sy, double sz) {
            this.sx = sx;
            this.sy = sy;
            this.sz = sz;
            return this;
        }

        public void send() {
            if (target == null)
                return;
            spawn(level, new BoreDebrisParticleOptions(state, target.x, target.y, target.z, sx, sy, sz), pos.x, pos.y, pos.z);
        }
    }

    public static final class BoreSparkle {
        private final ServerLevel level;
        private final Vec3 pos;
        private Vec3 target = null;
        private float r = 0.6F;
        private float g = 0.2F;
        private float b = 0.8F;

        BoreSparkle(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public BoreSparkle to(Vec3 target) {
            this.target = target;
            return this;
        }

        public BoreSparkle color(float r, float g, float b) {
            this.r = r;
            this.g = g;
            this.b = b;
            return this;
        }

        public BoreSparkle color(int rgb) {
            this.r = ARGB.red(rgb) / 255.0F;
            this.g = ARGB.green(rgb) / 255.0F;
            this.b = ARGB.blue(rgb) / 255.0F;
            return this;
        }

        public void send() {
            if (target == null)
                return;
            spawn(level, new BoreSparkleParticleOptions(target.x, target.y, target.z, r, g, b), pos.x, pos.y, pos.z);
        }
    }

    public static final class BoreStream {
        private final ServerLevel level;
        private final Vec3 source;
        private final Entity target;
        private int color = 0x8040C0;
        private int count = 0;
        private float scale = 0.15F;
        private int extend = 10;
        private double my = 0.0;

        BoreStream(ServerLevel level, Vec3 source, Entity target) {
            this.level = level;
            this.source = source;
            this.target = target;
        }

        public BoreStream color(int color) {
            this.color = color;
            return this;
        }

        public BoreStream count(int count) {
            this.count = count;
            return this;
        }

        public BoreStream scale(float scale) {
            this.scale = scale;
            return this;
        }

        public BoreStream extend(int extend) {
            this.extend = extend;
            return this;
        }

        public BoreStream upward(double my) {
            this.my = my;
            return this;
        }

        public void send() {
            ClientboundStreamEffectPayload payload = ClientboundStreamEffectPayload.bore(source.x, source.y, source.z, target.getId(), color, count, scale, extend, my);
            PacketDistributor.sendToPlayersNear(level, null, source.x, source.y, source.z, DEFAULT_RADIUS, payload);
        }
    }

    public static final class VoidStream {
        private final ServerLevel level;
        private final Vec3 source;
        private Vec3 target = null;
        private int seed = 0;
        private float scale = 0.15F;

        VoidStream(ServerLevel level, Vec3 source) {
            this.level = level;
            this.source = source;
        }

        public VoidStream to(Vec3 target) {
            this.target = target;
            return this;
        }

        public VoidStream seed(int seed) {
            this.seed = seed;
            return this;
        }

        public VoidStream scale(float scale) {
            this.scale = scale;
            return this;
        }

        public void send() {
            if (target == null)
                return;
            ClientboundStreamEffectPayload payload = ClientboundStreamEffectPayload.voidStream(source.x, source.y, source.z, target.x, target.y, target.z, seed, scale);
            PacketDistributor.sendToPlayersNear(level, null, source.x, source.y, source.z, DEFAULT_RADIUS, payload);
        }
    }

    public static final class FluxFume {
        private final ServerLevel level;
        private final Vec3 pos;
        private float r = 1.0F, g = 0.0F, b = 0.5F;
        private float scale = 0.3F;
        private int maxAge = 3;

        FluxFume(ServerLevel level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public FluxFume color(int rgb) {
            this.r = ARGB.red(rgb) / 255.0F;
            this.g = ARGB.green(rgb) / 255.0F;
            this.b = ARGB.blue(rgb) / 255.0F;
            return this;
        }

        public FluxFume color(float r, float g, float b) {
            this.r = r;
            this.g = g;
            this.b = b;
            return this;
        }

        public FluxFume scale(float scale) {
            this.scale = scale;
            return this;
        }

        public FluxFume maxAge(int maxAge) {
            this.maxAge = maxAge;
            return this;
        }

        public void send() {
            BubbleParticleOptions options = new BubbleParticleOptions(ARGB.colorFromFloat(1.0F, r, g, b), 0.25F, scale, maxAge, -0.01F, false);
            spawn(level, options, pos.x, pos.y, pos.z);
        }
    }
}
