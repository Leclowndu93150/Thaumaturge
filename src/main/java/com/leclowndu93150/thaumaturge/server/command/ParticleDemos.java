package com.leclowndu93150.thaumaturge.server.command;

import com.leclowndu93150.thaumaturge.content.effect.EffectDispatch;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class ParticleDemos {
    public static final Map<String, Demo> DEMOS = new LinkedHashMap<>();

    private ParticleDemos() {}

    public record Demo(String description, BiConsumer<ServerPlayer, Vec3> exec) {
    }

    static {
        register("sparkle", "A random 67% chance white sparkle", (p, v) -> Effects.sparkle(p.level(), v).color(1F, 1F, 1F).send());
        register("simpleSparkle", "Looping cell-320 sparkle with random alpha keys + gravity",
                (p, v) -> Effects.simpleSparkle(p.level(), v).color(1F, 0.8F, 0.4F).scale(0.5F).gravity(0.05F).baseAge(16).send());
        register("lineSparkle", "3-stop alpha sparkle for trail effects", (p, v) -> Effects.lineSparkle(p.level(), v).motion(0, 0.01, 0).color(0.5F, 0.8F, 1F).scale(0.5F).send());
        register("blockSparkles", "Per-face sparkles on a block (uses block below target)",
                (p, v) -> Effects.blockSparkles(p.level(), BlockPos.containing(v.x, v.y - 1, v.z)).from(p.getEyePosition()).send());

        register("bamf", "Salis-mundus end transformation puff (purple + poof sound + curly wisps)", (p, v) -> Effects.bamf(p.level(), v).withSound().fancy().send());
        register("wispyMotes", "Pink wispy motes drifting upward", (p, v) -> Effects.wispyMotes(p.level(), v).color(0.9F, 0.5F, 0.9F).age(40).gravity(-0.01F).send());
        register("curlyWisp", "Angled rotating curly wisp", (p, v) -> Effects.curlyWisp(p.level(), v).color(0.5F, 0.7F, 1F).seed(2).send());
        register("pechsCurse", "Angled curse particle + wispy motes", (p, v) -> Effects.pechsCurse(p.level(), v).send());
        register("cultistSpawn", "Cultist spawn-in animation (white→red gradient)", (p, v) -> Effects.cultistSpawn(p.level(), v).motion(0, 0.05, 0).send());

        register("fireMote", "Tiny glowing red mote (FXFireMote, full-bright)", (p, v) -> Effects.fireMote(p.level(), v).color(1F, 0.3F, 0F).scale(1.5F).send());
        register("alumentum", "Alumentum mote (always translucent FireMote variant)", (p, v) -> Effects.alumentum(p.level(), v).color(0.3F, 0F, 0.8F).scale(1.2F).send());
        register("taint", "Taint mist particle", (p, v) -> Effects.taint(p.level(), v).motion(0, 0.02, 0).scale(2F).send());
        register("lightningFlash", "Brief lightning flash particle (5-9 ticks)", (p, v) -> Effects.lightningFlash(p.level(), v).color(1F, 1F, 0.6F).scale(3F).send());
        register("levitator", "Pollen-yellow levitator drift (200+rand age)", (p, v) -> Effects.levitator(p.level(), v).motion(0, 0.03, 0).send());
        register("stabilizer", "Purple infusion stabilizer cone", (p, v) -> Effects.stabilizer(p.level(), v).motion(0, 0.05, 0).life(40).send());
        register("golemFly", "Translucent flying golem trail (no color)", (p, v) -> Effects.golemFly(p.level(), v).motion(0, 0.04, 0).send());
        register("pollution", "Pink pollution mist (uses block below as anchor)", (p, v) -> Effects.pollution(p.level(), BlockPos.containing(v.x, v.y - 1, v.z)).send());
        register("focusCloud", "Colored focus cloud (cyan)", (p, v) -> Effects.focusCloud(p.level(), v).motion(0, 0.02, 0).color(0x4080FF).send());
        register("blockMist", "Block-bound colored mist (uses block below)", (p, v) -> Effects.blockMist(p.level(), BlockPos.containing(v.x, v.y - 1, v.z)).color(0x80FF80).send());
        register("blockMistFlat", "Block-bound flat-grid mist (uses block below)", (p, v) -> Effects.blockMistFlat(p.level(), BlockPos.containing(v.x, v.y - 1, v.z)).color(0xFFFF80).send());
        register("nitor_flames_demo", "Single nitor flame (yellow)", (p, v) -> Effects.cultistSpawn(p.level(), v).send());
        register("wispParticles", "Looping wisp ring particles (red)", (p, v) -> Effects.wispParticles(p.level(), v).motion(0, 0.03, 0).color(0xFF4040).send());
        register("wispyMotesOnBlock", "Random-color wispy motes on block (uses block below)",
                (p, v) -> Effects.wispyMotesOnBlock(p.level(), BlockPos.containing(v.x, v.y - 1, v.z)).age(30).gravity(-0.01F).send());

        register("crucibleBubble", "Crucible bubble with pop animation (final 3 frames)", (p, v) -> Effects.crucibleBubble(p.level(), v).color(0.5F, 0.2F, 0.8F).send());
        register("crucibleBoil", "2 bubbles + heat-scaled gravity (negative = rising)", (p, v) -> Effects.crucibleBoil(p.level(), v).color(0.4F, 0.8F, 0.4F).heat(2).send());
        register("crucibleFroth", "White-blue froth puffs (gravity falls)", (p, v) -> Effects.crucibleFroth(p.level(), v).send());
        register("crucibleFrothDown", "Purple frothy droplets falling", (p, v) -> Effects.crucibleFrothDown(p.level(), v).send());

        register("spark", "Small white spark (16-grid cells 8/24/40 flipped random)", (p, v) -> Effects.spark(p.level(), v).color(1F, 1F, 0.8F).size(0.4F).send());
        register("burst", "31-frame aged burst animation (cell 208..238)", (p, v) -> Effects.burst(p.level(), v).size(2F).send());
        register("essentiaDrop", "Single essentia drop with gravity", (p, v) -> Effects.essentiaDrop(p.level(), v).color(0.4F, 0.8F, 1F).alpha(0.7F).send());
        register("jarSplash", "Jar splash droplet (TC dec-color 2650102 = #286176)", (p, v) -> Effects.jarSplash(p.level(), v).send());

        register("vent", "FXVent puff (faithful TC vent)", (p, v) -> Effects.vent(p.level(), v).motion(0, 0.1, 0).color(0x808080).scale(1F).send());
        register("vent2", "FXVent2 puff with jittered colors + gaussian gravity", (p, v) -> Effects.vent2(p.level(), v).motion(0, 0.15, 0).color(0x800080).scale(1F).withFlame().send());

        register("arcLightning", "Parabolic arc from eye to target with child sparkles", (p, v) -> Effects.arcLightning(p.level(), p.getEyePosition()).to(v).color(0xFFFFFF).gravity(0.2F).send());
        register("arcBolt", "Lightning bolt twin-cone from eye to target", (p, v) -> Effects.arcBolt(p.level(), p.getEyePosition()).to(v).color(0x80C0FF).width(1F).send());

        register("essentiaStream", "Essentia trail polycone from eye to target (red)", (p, v) -> EffectDispatch.spawnEssentiaStream(p.level(), p.getEyePosition(), v, 0xFF4040, 0, 4, 0.15F, 30, 0.0));

        register("fluxVent_legacy", "Legacy flux vent path (now Effects.vent2 with flame)", (p, v) -> Effects.vent2(p.level(), v).motion(0, 0.15, 0).color(0x800080).scale(1F).withFlame().send());

        register("blockRunes", "Block runes (4 calls, 4 face rotations) — purple", (p, v) -> {
            for (int i = 0; i < 4; i++)
                Effects.blockRunes(p.level(), v).color(0.8F, 0.2F, 1.0F).duration(20).send();
        });
        register("blockRunes2", "Block runes variant 2 (smaller, no x-offset)", (p, v) -> {
            for (int i = 0; i < 4; i++)
                Effects.blockRunes2(p.level(), v).color(0.4F, 1.0F, 0.6F).duration(20).send();
        });
        register("smokeSpiral", "Spiral smoke trail spawned 20× in burst", (p, v) -> {
            for (int i = 0; i < 20; i++) {
                Effects.smokeSpiral(p.level(), v).radius(1.5F).start(i * 18).minY((int) v.y).color(0x808080).send();
            }
        });
        register("beamWand", "Wand beam from caller eye to target (5s)", (p, v) -> Effects.beamWand(p.level(), p).to(v).color(0x80C0FF).age(100).type(0).send());
        register("beamBore", "Bore beam with spinning source orb from eye to target (5s)", (p, v) -> Effects.beamBore(p.level(), p.getEyePosition()).to(v).color(0xFF8040).age(100).type(0).send());

        register("boreParticles", "Stone-textured bore particles homing toward target (10x)", (p, v) -> {
            BlockState stone = Blocks.STONE.defaultBlockState();
            for (int i = 0; i < 10; i++) {
                Vec3 spawn = v.add(p.level().getRandom().nextGaussian() * 0.5, p.level().getRandom().nextGaussian() * 0.5, p.level().getRandom().nextGaussian() * 0.5);
                Effects.boreDebris(p.level(), spawn, stone).to(p.getEyePosition()).send();
            }
        });
        register("boreSparkle", "Purple bore sparkle homing toward eye (10x)", (p, v) -> {
            for (int i = 0; i < 10; i++) {
                Vec3 spawn = v.add(p.level().getRandom().nextGaussian() * 0.5, p.level().getRandom().nextGaussian() * 0.5, p.level().getRandom().nextGaussian() * 0.5);
                Effects.boreSparkle(p.level(), spawn).to(p.getEyePosition()).color(0.6F, 0.2F, 0.9F).send();
            }
        });
        register("boreStream", "Bore stream from target back to caster (entity-tracked)",
                (p, v) -> Effects.boreStream(p.level(), v, p).color(0x8040C0).count(p.level().getRandom().nextInt(8)).scale(0.15F).extend(20).send());
        register("voidStream", "Black void stream (end-portal texture) from caster to target",
                (p, v) -> Effects.voidStream(p.level(), p.getEyePosition()).to(v).seed(p.level().getRandom().nextInt(8)).scale(0.15F).send());
    }

    private static void register(String name, String description, BiConsumer<ServerPlayer, Vec3> exec) {
        DEMOS.put(name, new Demo(description, exec));
    }

    public static void run(ServerPlayer player, String name) {
        Demo demo = DEMOS.get(name);
        if (demo == null)
            return;
        Vec3 look = player.getLookAngle();
        Vec3 base = player.getEyePosition();
        Vec3 target = base.add(look.x * 3.0, look.y * 3.0, look.z * 3.0);
        demo.exec.accept(player, target);
    }
}
