package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.registry.TTParticles;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class TTParticleProviders {
    private TTParticleProviders() {}

    @SubscribeEvent
    public static void onRegister(RegisterParticleProvidersEvent event) {
        event.registerSpecial(TTParticles.PUFF.get(), new PuffParticle.Provider());
        event.registerSpecial(TTParticles.FLASH.get(), new FlashParticle.Provider());
        event.registerSpecial(TTParticles.SPARKLE.get(), new SparkleParticle.Provider());
        event.registerSpecial(TTParticles.WISPY_MOTE.get(), new WispyMoteParticle.Provider());
        event.registerSpecial(TTParticles.CURLY_WISP.get(), new CurlyWispParticle.Provider());
        event.registerSpecial(TTParticles.WISP_FLAME.get(), new WispFlameParticle.Provider());
        event.registerSpecial(TTParticles.TAINT_SWARM.get(), new TaintSwarmParticle.Provider());
        event.registerSpecial(TTParticles.TAINT_FUME.get(), new TaintFumeParticle.Provider());
        event.registerSpecial(TTParticles.LIGHTNING_FLASH.get(), new LightningFlashParticle.Provider());
        event.registerSpecial(TTParticles.STABILIZER_RUNE.get(), new StabilizerRuneParticle.Provider());
        event.registerSpecial(TTParticles.BUBBLE.get(), new BubbleParticle.Provider());
        event.registerSpecial(TTParticles.SLIMY_BUBBLE.get(), new SlimyBubbleParticle.Provider());
        event.registerSpecial(TTParticles.SPARK.get(), new SparkParticle.Provider());
        event.registerSpecial(TTParticles.WARD_FLASH.get(), new WardFlashParticle.Provider());
        event.registerSpecial(TTParticles.BURST.get(), new BurstParticle.Provider());
        event.registerSpecial(TTParticles.SCAN_GLYPH.get(), new ScanGlyphParticle.Provider());
        event.registerSpecial(TTParticles.SLASH.get(), new SlashParticle.Provider());
        event.registerSpecial(TTParticles.FOCUS_CLOUD.get(), new FocusCloudParticle.Provider());
        event.registerSpecial(TTParticles.BLOCK_MIST.get(), new BlockMistParticle.Provider());
        event.registerSpecial(TTParticles.MIST_FLAT.get(), new MistFlatParticle.Provider());
        event.registerSpecial(TTParticles.GOO_DRIP.get(), new GooDripParticle.Provider());
        event.registerSpecial(TTParticles.LEAF_MOTE.get(), new LeafMoteParticle.Provider());
        event.registerSpecial(TTParticles.SHIELD_SPARK.get(), new ShieldSparkParticle.Provider());
        event.registerSpecial(TTParticles.FLAME_FAN.get(), new FlameFanParticle.Provider());
        event.registerSpecial(TTParticles.CRACK_SHARD.get(), new CrackShardParticle.Provider());
        event.registerSpecial(TTParticles.AIR_GUST.get(), new AirGustParticle.Provider());
        event.registerSpecial(TTParticles.EARTH_PEBBLE.get(), new EarthPebbleParticle.Provider());
        event.registerSpecial(TTParticles.FROST_FLAKE.get(), new FrostFlakeParticle.Provider());
        event.registerSpecial(TTParticles.FLUX_SWIRL.get(), new FluxSwirlParticle.Provider());
        event.registerSpecial(TTParticles.RIFT_SHARD.get(), new RiftShardParticle.Provider());
        event.registerSpecial(TTParticles.GOLEM_EMOTE.get(), new GolemEmoteParticle.Provider());
        event.registerSpecial(TTParticles.CURSE_SMOKE.get(), new CurseSmokeParticle.Provider());
        event.registerSpecial(TTParticles.PRIMAL_FLARE.get(), new PrimalFlareParticle.Provider());
        event.registerSpecial(TTParticles.HEAL_FLASH.get(), new HealFlashParticle.Provider());
        event.registerSpecial(TTParticles.LEVITATOR_MIST.get(), new LevitatorMistParticle.Provider());
        event.registerSpecial(TTParticles.GOLEM_TRAIL.get(), new GolemTrailParticle.Provider());
        event.registerSpecial(TTParticles.POLLUTION_FUME.get(), new PollutionFumeParticle.Provider());
        event.registerSpecial(TTParticles.PECH_CURSE.get(), new PechCurseParticle.Provider());
        event.registerSpecial(TTParticles.CRIMSON_SMOKE.get(), new CrimsonSmokeParticle.Provider());
        event.registerSpecial(TTParticles.FIRE_MOTE.get(), new FireMoteParticle.Provider());
        event.registerSpecial(TTParticles.NITOR_CORE.get(), new NitorCoreParticle.Provider());
        event.registerSpecial(TTParticles.VENT.get(), new VentParticle.Provider());
        event.registerSpecial(TTParticles.BLOCK_RUNES.get(), new BlockRunesParticle.Provider());
        event.registerSpecial(TTParticles.BOLT.get(), new BoltParticle.Provider());
        event.registerSpecial(TTParticles.SMOKE_SPIRAL.get(), new SmokeSpiralParticle.Provider());
        event.registerSpecial(TTParticles.VIS_SPARKLE.get(), new VisSparkleParticle.Provider());
        event.registerSpecial(TTParticles.WISP.get(), new WispParticle.Provider());
        event.registerSpecial(TTParticles.ESSENTIA_DROP.get(), new EssentiaDropParticle.Provider());
        event.registerSpecial(TTParticles.BORE_SPARKLE.get(), new BoreSparkleParticle.Provider());
        event.registerSpecial(TTParticles.BORE_DEBRIS.get(), new BoreDebrisParticle.Provider());
        event.registerSpecial(TTParticles.INFUSION_CRUMBS.get(), new InfusionCrumbsParticle.Provider());
        event.registerSpecial(TTParticles.FLUX_GOO_DROPLET.get(), new FluxGooDropletParticle.Provider());
        event.registerSpecial(TTParticles.TAINT_SPLOSION.get(), new TaintSplosionParticle.Provider());
    }
}
