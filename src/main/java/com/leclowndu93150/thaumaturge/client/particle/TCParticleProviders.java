package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.registry.TCParticles;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class TCParticleProviders {
    private TCParticleProviders() {}

    @SubscribeEvent
    public static void onRegister(RegisterParticleProvidersEvent event) {
        event.registerSpecial(TCParticles.PUFF.get(), new PuffParticle.Provider());
        event.registerSpecial(TCParticles.FLASH.get(), new FlashParticle.Provider());
        event.registerSpecial(TCParticles.SPARKLE.get(), new SparkleParticle.Provider());
        event.registerSpecial(TCParticles.WISPY_MOTE.get(), new WispyMoteParticle.Provider());
        event.registerSpecial(TCParticles.CURLY_WISP.get(), new CurlyWispParticle.Provider());
        event.registerSpecial(TCParticles.WISP_FLAME.get(), new WispFlameParticle.Provider());
        event.registerSpecial(TCParticles.TAINT_FUME.get(), new TaintFumeParticle.Provider());
        event.registerSpecial(TCParticles.LIGHTNING_FLASH.get(), new LightningFlashParticle.Provider());
        event.registerSpecial(TCParticles.STABILIZER_RUNE.get(), new StabilizerRuneParticle.Provider());
        event.registerSpecial(TCParticles.BUBBLE.get(), new BubbleParticle.Provider());
        event.registerSpecial(TCParticles.SLIMY_BUBBLE.get(), new SlimyBubbleParticle.Provider());
        event.registerSpecial(TCParticles.SPARK.get(), new SparkParticle.Provider());
        event.registerSpecial(TCParticles.WARD_FLASH.get(), new WardFlashParticle.Provider());
        event.registerSpecial(TCParticles.BURST.get(), new BurstParticle.Provider());
        event.registerSpecial(TCParticles.SCAN_GLYPH.get(), new ScanGlyphParticle.Provider());
        event.registerSpecial(TCParticles.SLASH.get(), new SlashParticle.Provider());
        event.registerSpecial(TCParticles.FOCUS_CLOUD.get(), new FocusCloudParticle.Provider());
        event.registerSpecial(TCParticles.BLOCK_MIST.get(), new BlockMistParticle.Provider());
        event.registerSpecial(TCParticles.MIST_FLAT.get(), new MistFlatParticle.Provider());
        event.registerSpecial(TCParticles.GOO_DRIP.get(), new GooDripParticle.Provider());
        event.registerSpecial(TCParticles.LEAF_MOTE.get(), new LeafMoteParticle.Provider());
        event.registerSpecial(TCParticles.SHIELD_SPARK.get(), new ShieldSparkParticle.Provider());
        event.registerSpecial(TCParticles.FLAME_FAN.get(), new FlameFanParticle.Provider());
        event.registerSpecial(TCParticles.CRACK_SHARD.get(), new CrackShardParticle.Provider());
        event.registerSpecial(TCParticles.AIR_GUST.get(), new AirGustParticle.Provider());
        event.registerSpecial(TCParticles.EARTH_PEBBLE.get(), new EarthPebbleParticle.Provider());
        event.registerSpecial(TCParticles.FROST_FLAKE.get(), new FrostFlakeParticle.Provider());
        event.registerSpecial(TCParticles.FLUX_SWIRL.get(), new FluxSwirlParticle.Provider());
        event.registerSpecial(TCParticles.RIFT_SHARD.get(), new RiftShardParticle.Provider());
        event.registerSpecial(TCParticles.GOLEM_EMOTE.get(), new GolemEmoteParticle.Provider());
        event.registerSpecial(TCParticles.CURSE_SMOKE.get(), new CurseSmokeParticle.Provider());
        event.registerSpecial(TCParticles.PRIMAL_FLARE.get(), new PrimalFlareParticle.Provider());
        event.registerSpecial(TCParticles.HEAL_FLASH.get(), new HealFlashParticle.Provider());
        event.registerSpecial(TCParticles.LEVITATOR_MIST.get(), new LevitatorMistParticle.Provider());
        event.registerSpecial(TCParticles.GOLEM_TRAIL.get(), new GolemTrailParticle.Provider());
        event.registerSpecial(TCParticles.POLLUTION_FUME.get(), new PollutionFumeParticle.Provider());
        event.registerSpecial(TCParticles.PECH_CURSE.get(), new PechCurseParticle.Provider());
        event.registerSpecial(TCParticles.CRIMSON_SMOKE.get(), new CrimsonSmokeParticle.Provider());
        event.registerSpecial(TCParticles.FIRE_MOTE.get(), new FireMoteParticle.Provider());
        event.registerSpecial(TCParticles.NITOR_CORE.get(), new NitorCoreParticle.Provider());
        event.registerSpecial(TCParticles.VENT.get(), new VentParticle.Provider());
        event.registerSpecial(TCParticles.BLOCK_RUNES.get(), new BlockRunesParticle.Provider());
        event.registerSpecial(TCParticles.BOLT.get(), new BoltParticle.Provider());
        event.registerSpecial(TCParticles.SMOKE_SPIRAL.get(), new SmokeSpiralParticle.Provider());
        event.registerSpecial(TCParticles.VIS_SPARKLE.get(), new VisSparkleParticle.Provider());
        event.registerSpecial(TCParticles.WISP.get(), new WispParticle.Provider());
        event.registerSpecial(TCParticles.ESSENTIA_DROP.get(), new EssentiaDropParticle.Provider());
        event.registerSpecial(TCParticles.BORE_SPARKLE.get(), new BoreSparkleParticle.Provider());
        event.registerSpecial(TCParticles.BORE_DEBRIS.get(), new BoreDebrisParticle.Provider());
        event.registerSpecial(TCParticles.INFUSION_CRUMBS.get(), new InfusionCrumbsParticle.Provider());
        event.registerSpecial(TCParticles.FLUX_GOO_DROPLET.get(), new FluxGooDropletParticle.Provider());
        event.registerSpecial(TCParticles.TAINT_SPLOSION.get(), new TaintSplosionParticle.Provider());
    }
}
