package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.aura.AuraData;
import com.leclowndu93150.thaumaturge.content.aura.pressure.FluxPressureState;
import com.leclowndu93150.thaumaturge.content.casters.BlockWorkQueues;
import com.leclowndu93150.thaumaturge.content.entity.FocusCloudCooldowns;
import com.leclowndu93150.thaumaturge.content.entity.trait.MobTraitRuntime;
import com.leclowndu93150.thaumaturge.content.entity.trait.MobTraitState;
import com.leclowndu93150.thaumaturge.content.equipment.runic.RunicShieldState;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealWorldIndex;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealsChunkData;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskBoard;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerSwapQueue;
import com.leclowndu93150.thaumaturge.content.research.PlayerKnowledge;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPoolData;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBloomIndex;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintColumns;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintPressure;
import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFluxSamples;
import com.leclowndu93150.thaumaturge.content.warding.ArcaneLockChunkData;
import com.leclowndu93150.thaumaturge.content.warding.WardChunkData;
import com.leclowndu93150.thaumaturge.content.warp.WarpData;
import com.mojang.serialization.Codec;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class TCAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, TCIds.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerKnowledge>> KNOWLEDGE = register("knowledge",
            () -> AttachmentType.builder(PlayerKnowledge::new).serialize(PlayerKnowledge.CODEC).sync(PlayerKnowledge.STREAM_CODEC).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<AuraData>> AURA = register("aura", () -> AttachmentType.builder(AuraData::new).serialize(AuraData.CODEC).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<AspectPoolData>> ASPECT_POOL = register("aspect_pool",
            () -> AttachmentType.builder(AspectPoolData::new).serialize(AspectPoolData.CODEC).sync(AspectPoolData.STREAM_CODEC).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<WarpData>> WARP = register("warp",
            () -> AttachmentType.builder(WarpData::new).serialize(WarpData.CODEC).sync((holder, to) -> holder == to, WarpData.STREAM_CODEC).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<DustTriggerSwapQueue>> DUST_TRIGGER_QUEUE = register("dust_trigger_queue",
            () -> AttachmentType.builder(DustTriggerSwapQueue::new).serialize(DustTriggerSwapQueue.CODEC).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> GRAPPLE_ID = register("grapple_id", () -> AttachmentType.builder(() -> -1).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Long>> CASTER_COOLDOWN = register("caster_cooldown", () -> AttachmentType.builder(() -> 0L).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Long>> CLOUD_JUMP_TIME = register("cloud_jump_time", () -> AttachmentType.builder(() -> 0L).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<RunicShieldState>> RUNIC_SHIELD = register("runic_shield", () -> AttachmentType.builder(RunicShieldState::new).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<FocusCloudCooldowns>> FOCUS_CLOUD_COOLDOWNS = register("focus_cloud_cooldowns",
            () -> AttachmentType.builder(FocusCloudCooldowns::new).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<BlockWorkQueues>> BLOCK_WORK_QUEUES = register("block_work_queues",
            () -> AttachmentType.builder(BlockWorkQueues::new).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PhysicalFluxSamples>> PHYSICAL_FLUX_SAMPLES = register("physical_flux_samples",
            () -> AttachmentType.builder(PhysicalFluxSamples::new).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<SealsChunkData>> SEALS = register("seals",
            () -> AttachmentType.builder(SealsChunkData::new).serialize(SealsChunkData.CODEC).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<SealWorldIndex>> SEAL_INDEX = register("seal_index", () -> AttachmentType.builder(SealWorldIndex::new).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<WardChunkData>> WARDS = register("wards",
            () -> AttachmentType.builder(WardChunkData::new).serialize(WardChunkData.CODEC).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<TaintPressure>> TAINT_PRESSURE = register("taint_pressure",
            () -> AttachmentType.builder(TaintPressure::new).serialize(TaintPressure.CODEC).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<TaintColumns>> TAINT_COLUMNS = register("taint_columns",
            () -> AttachmentType.builder(TaintColumns::new).serialize(TaintColumns.CODEC).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<FluxPressureState>> FLUX_PRESSURE = register("flux_pressure", () -> AttachmentType.builder(FluxPressureState::new).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<TaintBloomIndex>> TAINT_BLOOMS = register("taint_blooms", () -> AttachmentType.builder(TaintBloomIndex::new).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ArcaneLockChunkData>> ARCANE_LOCKS = register("arcane_locks",
            () -> AttachmentType.builder(ArcaneLockChunkData::new).serialize(ArcaneLockChunkData.CODEC).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<TaskBoard>> GOLEM_TASKS = register("golem_tasks", () -> AttachmentType.builder(TaskBoard::new).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Set<BlockPos>>> EAR_INDEX = register("ear_index",
            () -> AttachmentType.<Set<BlockPos>>builder(() -> ConcurrentHashMap.newKeySet()).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<MobTraitState>> MOB_TRAITS = register("mob_traits",
            () -> AttachmentType.builder(() -> MobTraitState.EMPTY).serialize(MobTraitState.CODEC, state -> !state.isEmpty()).sync(MobTraitState.STREAM_CODEC).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<MobTraitRuntime>> MOB_TRAIT_RUNTIME = register("mob_trait_runtime",
            () -> AttachmentType.builder(MobTraitRuntime::new).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> CHAMPION_ROLLED = register("champion_rolled",
            () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL.fieldOf("rolled")).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> HOVERING = register("hovering",
            () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL.fieldOf("hovering"), hovering -> hovering).sync(ByteBufCodecs.BOOL).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> HOVER_CHARGE = register("hover_charge",
            () -> AttachmentType.builder(() -> 0).serialize(Codec.INT.fieldOf("charge"), charge -> charge > 0).build());

    private TCAttachments() {}

    private static <T> DeferredHolder<AttachmentType<?>, AttachmentType<T>> register(String name, Supplier<AttachmentType<T>> supplier) {
        return ATTACHMENTS.register(name, supplier);
    }

    public static void register(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
    }
}
