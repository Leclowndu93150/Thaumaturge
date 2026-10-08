package com.leclowndu93150.thaumaturge.data.sound;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

public final class TTSoundDefinitionsProvider extends SoundDefinitionsProvider {
    public TTSoundDefinitionsProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, TTIds.MODID, existingFileHelper);
    }

    @Override
    public void registerSounds() {
        add(
                TTSounds.BRAIN,
                definition()
                        .with(
                                sound(TTIds.rl("brain1")),
                                sound(TTIds.rl("brain2")),
                                sound(TTIds.rl("brain3")),
                                sound(TTIds.rl("brain4"))));
        add(
                TTSounds.BUBBLE,
                definition()
                        .with(
                                sound(TTIds.rl("bubble1")),
                                sound(TTIds.rl("bubble2")),
                                sound(TTIds.rl("bubble3")),
                                sound(TTIds.rl("bubble4"))));
        add(TTSounds.CAMERA_TICKS, definition().with(sound(TTIds.rl("cameraticks"))));
        add(
                TTSounds.CHANT,
                definition().with(sound(TTIds.rl("chant1")), sound(TTIds.rl("chant2")), sound(TTIds.rl("chant3"))));
        add(
                TTSounds.CLACK,
                definition().with(sound(TTIds.rl("clack1")), sound(TTIds.rl("clack2")), sound(TTIds.rl("clack3"))));
        add(TTSounds.COINS, definition().with(sound(TTIds.rl("coins"))));
        add(TTSounds.CRABCLAW, definition().with(sound(TTIds.rl("crabclaw"))));
        add(TTSounds.CRABDEATH, definition().with(sound(TTIds.rl("crabdeath"))));
        add(
                TTSounds.CRABTALK,
                definition()
                        .with(
                                sound(TTIds.rl("crabtalk1")),
                                sound(TTIds.rl("crabtalk2")),
                                sound(TTIds.rl("crabtalk3"))));
        add(TTSounds.CRAFTFAIL, definition().with(sound(TTIds.rl("craftfail"))));
        add(TTSounds.CRAFTSTART, definition().with(sound(TTIds.rl("craftstart"))));
        add(TTSounds.CREAK, definition().with(sound(TTIds.rl("creak1")), sound(TTIds.rl("creak2"))));
        add(TTSounds.CRYSTAL, definition().with(sound(TTIds.rl("crystal"))));
        add(TTSounds.DUST, definition().with(sound(TTIds.rl("dust"))));
        add(TTSounds.EGATTACK, definition().with(sound(TTIds.rl("egattack"))));
        add(TTSounds.EGDEATH, definition().with(sound(TTIds.rl("egdeath"))));
        add(TTSounds.EGIDLE, definition().with(sound(TTIds.rl("egidle1")), sound(TTIds.rl("egidle2"))));
        add(TTSounds.EGSCREECH, definition().with(sound(TTIds.rl("egscreech"))));
        add(TTSounds.ERASE, definition().with(sound(TTIds.rl("erase"))));
        add(TTSounds.EVILPORTAL, definition().with(sound(TTIds.rl("evilportal"))));
        add(TTSounds.FLY, definition().with(sound(TTIds.rl("fly1")), sound(TTIds.rl("fly2"))));
        add(TTSounds.GORE, definition().with(sound(TTIds.rl("gore1")), sound(TTIds.rl("gore2"))));
        add(TTSounds.GRIND, definition().with(sound(TTIds.rl("grind"))));
        add(TTSounds.HEARTBEAT, definition().with(sound(TTIds.rl("heartbeat"))));
        add(TTSounds.HHOFF, definition().with(sound(TTIds.rl("hhoff"))));
        add(TTSounds.HHON, definition().with(sound(TTIds.rl("hhon"))));
        add(TTSounds.ICE, definition().with(sound(TTIds.rl("ice1")), sound(TTIds.rl("ice2")), sound(TTIds.rl("ice3"))));
        add(TTSounds.INFUSER, definition().with(sound(TTIds.rl("infuser"))));
        add(TTSounds.INFUSERSTART, definition().with(sound(TTIds.rl("infuserstart"))));
        add(TTSounds.JACOBS, definition().with(sound(TTIds.rl("jacobs"))));
        add(
                TTSounds.JAR,
                definition()
                        .with(
                                sound(TTIds.rl("jar1")),
                                sound(TTIds.rl("jar2")),
                                sound(TTIds.rl("jar3")),
                                sound(TTIds.rl("jar4"))));
        add(TTSounds.KEY, definition().with(sound(TTIds.rl("key"))));
        add(TTSounds.LEARN, definition().with(sound(TTIds.rl("learn"))));
        add(TTSounds.MONOLITH, definition().with(sound(TTIds.rl("monolith"))));
        add(TTSounds.PAGE, definition().with(sound(TTIds.rl("page1")), sound(TTIds.rl("page2"))));
        add(TTSounds.PAGETURN, definition().with(sound(TTIds.rl("pageturn"))));
        add(TTSounds.PECH_CHARGE, definition().with(sound(TTIds.rl("pech_charge1")), sound(TTIds.rl("pech_charge2"))));
        add(TTSounds.PECH_DEATH, definition().with(sound(TTIds.rl("pech_death"))));
        add(TTSounds.PECH_DICE, definition().with(sound(TTIds.rl("pech_dice"))));
        add(TTSounds.PECH_HIT, definition().with(sound(TTIds.rl("pech_hit1")), sound(TTIds.rl("pech_hit2"))));
        add(
                TTSounds.PECH_IDLE,
                definition()
                        .with(
                                sound(TTIds.rl("pech_idle1")),
                                sound(TTIds.rl("pech_idle2")),
                                sound(TTIds.rl("pech_idle3"))));
        add(TTSounds.PECH_TRADE, definition().with(sound(TTIds.rl("pech_trade"))));
        add(TTSounds.POOF, definition().with(sound(TTIds.rl("poof1")), sound(TTIds.rl("poof2"))));
        add(
                TTSounds.PUMP,
                definition().with(sound(TTIds.rl("pump1")), sound(TTIds.rl("pump2")), sound(TTIds.rl("pump3"))));
        add(TTSounds.RUMBLE, definition().with(sound(TTIds.rl("rumble"))));
        add(TTSounds.RUNICSHIELDCHARGE, definition().with(sound(TTIds.rl("runicshieldcharge"))));
        add(TTSounds.RUNICSHIELDEFFECT, definition().with(sound(TTIds.rl("runicshieldeffect"))));
        add(TTSounds.SCAN, definition().with(sound(TTIds.rl("scan"))));
        add(TTSounds.SHOCK, definition().with(sound(TTIds.rl("shock1")), sound(TTIds.rl("shock2"))));
        add(TTSounds.SPILL, definition().with(sound(TTIds.rl("spill"))));
        add(TTSounds.SQUEEK, definition().with(sound(TTIds.rl("squeek1")), sound(TTIds.rl("squeek2"))));
        add(
                TTSounds.SWARM,
                definition().with(sound(TTIds.rl("swarm1")), sound(TTIds.rl("swarm2")), sound(TTIds.rl("swarm3"))));
        add(TTSounds.SWARMATTACK, definition().with(sound(TTIds.rl("swarmattack"))));
        add(
                TTSounds.TENTACLE,
                definition()
                        .with(
                                sound(TTIds.rl("tentacle1")),
                                sound(TTIds.rl("tentacle2")),
                                sound(TTIds.rl("tentacle3"))));
        add(TTSounds.TICKS, definition().with(sound(TTIds.rl("ticks"))));
        add(TTSounds.TOOL, definition().with(sound(TTIds.rl("tool1")), sound(TTIds.rl("tool2"))));
        add(TTSounds.UPGRADE, definition().with(sound(TTIds.rl("upgrade"))));
        add(TTSounds.URNBREAK, definition().with(sound(TTIds.rl("urnbreak"))));
        add(
                TTSounds.WAND,
                definition().with(sound(TTIds.rl("wand1")), sound(TTIds.rl("wand2")), sound(TTIds.rl("wand3"))));
        add(TTSounds.WANDFAIL, definition().with(sound(TTIds.rl("wandfail"))));
        add(TTSounds.WHISPERS, definition().with(sound(TTIds.rl("whispers"))));
        add(TTSounds.WIND, definition().with(sound(TTIds.rl("wind1")), sound(TTIds.rl("wind2"))));
        add(TTSounds.WISPDEAD, definition().with(sound(TTIds.rl("wispdead"))));
        add(
                TTSounds.WISPLIVE,
                definition()
                        .with(
                                sound(TTIds.rl("wisplive1")),
                                sound(TTIds.rl("wisplive2")),
                                sound(TTIds.rl("wisplive3"))));
        add(TTSounds.WRITE, definition().with(sound(TTIds.rl("write1")), sound(TTIds.rl("write2"))));
        add(
                TTSounds.ZAP,
                definition()
                        .with(sound(TTIds.rl("zap1")), sound(TTIds.rl("zap2")))
                        .subtitle("subtitles.thaumaturge.zap"));
    }
}
