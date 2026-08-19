package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.particle.AirGustParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.BlockRunesParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.BoltParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.BoreDebrisParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.BoreSparkleParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.BubbleParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.BurstParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.CrackShardParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.CurlyWispParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.EarthPebbleParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.EssentiaDropParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.FireMoteParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.FlameFanParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.FluxGooDropletParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.FluxSwirlParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.FrostFlakeParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.GolemEmoteParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.InfusionCrumbsParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.LightningFlashParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.NitorCoreParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.RiftShardParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.ScanGlyphParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.ShieldSparkParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.SlashParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.SlimyBubbleParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.SmokeSpiralParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.SparkParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.SparkleParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.StabilizerRuneParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.TaintFumeParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.VentParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.VisSparkleParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.WardFlashParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.WispFlameParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.WispParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.WispyMoteParticleOptions;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TCParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, TCIds.MODID);

    public static final DeferredHolder<ParticleType<?>, ParticleType<SparkleParticleOptions>> SPARKLE = register("sparkle", SparkleParticleOptions.CODEC, SparkleParticleOptions.STREAM_CODEC);

    public static final DeferredHolder<ParticleType<?>, ParticleType<WispyMoteParticleOptions>> WISPY_MOTE = register("wispy_mote", WispyMoteParticleOptions.CODEC,
            WispyMoteParticleOptions.STREAM_CODEC);

    public static final DeferredHolder<ParticleType<?>, ParticleType<CurlyWispParticleOptions>> CURLY_WISP = register("curly_wisp", CurlyWispParticleOptions.CODEC,
            CurlyWispParticleOptions.STREAM_CODEC);

    public static final DeferredHolder<ParticleType<?>, ParticleType<WispFlameParticleOptions>> WISP_FLAME = register("wisp_flame", WispFlameParticleOptions.CODEC,
            WispFlameParticleOptions.STREAM_CODEC);

    public static final DeferredHolder<ParticleType<?>, ParticleType<TaintFumeParticleOptions>> TAINT_FUME = register("taint_fume", TaintFumeParticleOptions.CODEC,
            TaintFumeParticleOptions.STREAM_CODEC);

    public static final DeferredHolder<ParticleType<?>, ParticleType<LightningFlashParticleOptions>> LIGHTNING_FLASH = register("lightning_flash", LightningFlashParticleOptions.CODEC,
            LightningFlashParticleOptions.STREAM_CODEC);

    public static final DeferredHolder<ParticleType<?>, ParticleType<StabilizerRuneParticleOptions>> STABILIZER_RUNE = register("stabilizer_rune", StabilizerRuneParticleOptions.CODEC,
            StabilizerRuneParticleOptions.STREAM_CODEC);

    public static final DeferredHolder<ParticleType<?>, ParticleType<BubbleParticleOptions>> BUBBLE = register("bubble", BubbleParticleOptions.CODEC, BubbleParticleOptions.STREAM_CODEC);

    public static final DeferredHolder<ParticleType<?>, ParticleType<SlimyBubbleParticleOptions>> SLIMY_BUBBLE = register("slimy_bubble", SlimyBubbleParticleOptions.CODEC,
            SlimyBubbleParticleOptions.STREAM_CODEC);

    public static final DeferredHolder<ParticleType<?>, ParticleType<SparkParticleOptions>> SPARK = register("spark", SparkParticleOptions.CODEC, SparkParticleOptions.STREAM_CODEC);

    public static final DeferredHolder<ParticleType<?>, ParticleType<BurstParticleOptions>> BURST = register("burst", BurstParticleOptions.CODEC, BurstParticleOptions.STREAM_CODEC);

    public static final DeferredHolder<ParticleType<?>, ParticleType<ScanGlyphParticleOptions>> SCAN_GLYPH = register("scan_glyph", ScanGlyphParticleOptions.CODEC,
            ScanGlyphParticleOptions.STREAM_CODEC);

    public static final DeferredHolder<ParticleType<?>, ParticleType<SlashParticleOptions>> SLASH = register("slash", SlashParticleOptions.CODEC, SlashParticleOptions.STREAM_CODEC);

    public static final DeferredHolder<ParticleType<?>, TCColorParticleType> PUFF = color("puff");
    public static final DeferredHolder<ParticleType<?>, TCColorParticleType> FLASH = color("flash");
    public static final DeferredHolder<ParticleType<?>, TCColorParticleType> FOCUS_CLOUD = color("focus_cloud");
    public static final DeferredHolder<ParticleType<?>, TCColorParticleType> BLOCK_MIST = color("block_mist");
    public static final DeferredHolder<ParticleType<?>, TCColorParticleType> MIST_FLAT = color("mist_flat");
    public static final DeferredHolder<ParticleType<?>, TCColorParticleType> GOO_DRIP = color("goo_drip");
    public static final DeferredHolder<ParticleType<?>, TCColorParticleType> LEAF_MOTE = color("leaf_mote");

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> LEVITATOR_MIST = simple("levitator_mist");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> GOLEM_TRAIL = simple("golem_trail");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> POLLUTION_FUME = simple("pollution_fume");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> PECH_CURSE = simple("pech_curse");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> CRIMSON_SMOKE = simple("crimson_smoke");

    public static final DeferredHolder<ParticleType<?>, ParticleType<FireMoteParticleOptions>> FIRE_MOTE = register("fire_mote", FireMoteParticleOptions.CODEC, FireMoteParticleOptions.STREAM_CODEC);

    public static final DeferredHolder<ParticleType<?>, ParticleType<NitorCoreParticleOptions>> NITOR_CORE = register("nitor_core", NitorCoreParticleOptions.CODEC,
            NitorCoreParticleOptions.STREAM_CODEC);

    public static final DeferredHolder<ParticleType<?>, ParticleType<VentParticleOptions>> VENT = register("vent", VentParticleOptions.CODEC, VentParticleOptions.STREAM_CODEC);

    public static final DeferredHolder<ParticleType<?>, ParticleType<BlockRunesParticleOptions>> BLOCK_RUNES = register("block_runes", BlockRunesParticleOptions.CODEC,
            BlockRunesParticleOptions.STREAM_CODEC);

    public static final DeferredHolder<ParticleType<?>, ParticleType<SmokeSpiralParticleOptions>> SMOKE_SPIRAL = register("smoke_spiral", SmokeSpiralParticleOptions.CODEC,
            SmokeSpiralParticleOptions.STREAM_CODEC);

    public static final DeferredHolder<ParticleType<?>, ParticleType<VisSparkleParticleOptions>> VIS_SPARKLE = register("vis_sparkle", VisSparkleParticleOptions.CODEC,
            VisSparkleParticleOptions.STREAM_CODEC);

    public static final DeferredHolder<ParticleType<?>, ParticleType<WispParticleOptions>> WISP = register("wisp", WispParticleOptions.CODEC, WispParticleOptions.STREAM_CODEC);

    public static final DeferredHolder<ParticleType<?>, ParticleType<BoltParticleOptions>> BOLT = register("bolt", BoltParticleOptions.CODEC, BoltParticleOptions.STREAM_CODEC);

    public static final DeferredHolder<ParticleType<?>, ParticleType<BoreDebrisParticleOptions>> BORE_DEBRIS = register("bore_debris", BoreDebrisParticleOptions.CODEC,
            BoreDebrisParticleOptions.STREAM_CODEC);

    public static final DeferredHolder<ParticleType<?>, ParticleType<InfusionCrumbsParticleOptions>> INFUSION_CRUMBS = register("infusion_crumbs", InfusionCrumbsParticleOptions.CODEC,
            InfusionCrumbsParticleOptions.STREAM_CODEC);

    public static final DeferredHolder<ParticleType<?>, ParticleType<BoreSparkleParticleOptions>> BORE_SPARKLE = register("bore_sparkle", BoreSparkleParticleOptions.CODEC,
            BoreSparkleParticleOptions.STREAM_CODEC);

    public static final DeferredHolder<ParticleType<?>, ParticleType<EssentiaDropParticleOptions>> ESSENTIA_DROP = register("essentia_drop", EssentiaDropParticleOptions.CODEC,
            EssentiaDropParticleOptions.STREAM_CODEC);

    public static final DeferredHolder<ParticleType<?>, ParticleType<FluxGooDropletParticleOptions>> FLUX_GOO_DROPLET = register("flux_goo_droplet", FluxGooDropletParticleOptions.CODEC,
            FluxGooDropletParticleOptions.STREAM_CODEC);

    public static final DeferredHolder<ParticleType<?>, ParticleType<ShieldSparkParticleOptions>> SHIELD_SPARK = register("shield_spark", ShieldSparkParticleOptions.CODEC,
            ShieldSparkParticleOptions.STREAM_CODEC);

    public static final DeferredHolder<ParticleType<?>, ParticleType<WardFlashParticleOptions>> WARD_FLASH = register("ward_flash", WardFlashParticleOptions.CODEC,
            WardFlashParticleOptions.STREAM_CODEC);

    public static final DeferredHolder<ParticleType<?>, ParticleType<FlameFanParticleOptions>> FLAME_FAN = register("flame_fan", FlameFanParticleOptions.CODEC, FlameFanParticleOptions.STREAM_CODEC);

    public static final DeferredHolder<ParticleType<?>, ParticleType<CrackShardParticleOptions>> CRACK_SHARD = register("crack_shard", CrackShardParticleOptions.CODEC,
            CrackShardParticleOptions.STREAM_CODEC);

    public static final DeferredHolder<ParticleType<?>, ParticleType<AirGustParticleOptions>> AIR_GUST = register("air_gust", AirGustParticleOptions.CODEC, AirGustParticleOptions.STREAM_CODEC);

    public static final DeferredHolder<ParticleType<?>, ParticleType<EarthPebbleParticleOptions>> EARTH_PEBBLE = register("earth_pebble", EarthPebbleParticleOptions.CODEC,
            EarthPebbleParticleOptions.STREAM_CODEC);

    public static final DeferredHolder<ParticleType<?>, ParticleType<FrostFlakeParticleOptions>> FROST_FLAKE = register("frost_flake", FrostFlakeParticleOptions.CODEC,
            FrostFlakeParticleOptions.STREAM_CODEC);

    public static final DeferredHolder<ParticleType<?>, ParticleType<FluxSwirlParticleOptions>> FLUX_SWIRL = register("flux_swirl", FluxSwirlParticleOptions.CODEC,
            FluxSwirlParticleOptions.STREAM_CODEC);

    public static final DeferredHolder<ParticleType<?>, ParticleType<RiftShardParticleOptions>> RIFT_SHARD = register("rift_shard", RiftShardParticleOptions.CODEC,
            RiftShardParticleOptions.STREAM_CODEC);

    public static final DeferredHolder<ParticleType<?>, ParticleType<GolemEmoteParticleOptions>> GOLEM_EMOTE = register("golem_emote", GolemEmoteParticleOptions.CODEC,
            GolemEmoteParticleOptions.STREAM_CODEC);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> CURSE_SMOKE = simple("curse_smoke");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> PRIMAL_FLARE = simple("primal_flare");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> HEAL_FLASH = simple("heal_flash");

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> TAINT_SPLOSION = simple("taint_splosion");

    private TCParticles() {}

    private static <T extends ParticleOptions> DeferredHolder<ParticleType<?>, ParticleType<T>> register(String name, MapCodec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        return PARTICLES.register(name, () -> new TCParticleType<>(codec, streamCodec));
    }

    private static DeferredHolder<ParticleType<?>, TCColorParticleType> color(String name) {
        return PARTICLES.register(name, TCColorParticleType::new);
    }

    private static DeferredHolder<ParticleType<?>, SimpleParticleType> simple(String name) {
        return PARTICLES.register(name, () -> new SimpleParticleType(false));
    }

    public static ColorParticleOption colorOf(DeferredHolder<ParticleType<?>, TCColorParticleType> type, int color) {
        return ColorParticleOption.create(type.get(), color);
    }

    public static ColorParticleOption colorOf(DeferredHolder<ParticleType<?>, TCColorParticleType> type, float r, float g, float b) {
        return ColorParticleOption.create(type.get(), r, g, b);
    }

    public static void register(IEventBus modBus) {
        PARTICLES.register(modBus);
    }
}
