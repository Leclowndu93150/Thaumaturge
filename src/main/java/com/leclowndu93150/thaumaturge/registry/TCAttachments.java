package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.aura.AuraData;
import com.leclowndu93150.thaumaturge.content.casters.BlockWorkQueues;
import com.leclowndu93150.thaumaturge.content.entity.FocusCloudCooldowns;
import com.leclowndu93150.thaumaturge.content.equipment.runic.RunicShieldState;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealWorldIndex;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealsChunkData;
import com.leclowndu93150.thaumaturge.content.golem.tasks.GolemTasks;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerSwapQueue;
import com.leclowndu93150.thaumaturge.content.research.PlayerKnowledge;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPoolData;
import com.leclowndu93150.thaumaturge.content.warding.WardChunkData;
import com.leclowndu93150.thaumaturge.content.warp.WarpData;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
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

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<SealsChunkData>> SEALS = register("seals",
            () -> AttachmentType.builder(SealsChunkData::new).serialize(SealsChunkData.CODEC).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<SealWorldIndex>> SEAL_INDEX = register("seal_index", () -> AttachmentType.builder(SealWorldIndex::new).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<WardChunkData>> WARDS = register("wards",
            () -> AttachmentType.builder(WardChunkData::new).serialize(WardChunkData.CODEC).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<GolemTasks>> GOLEM_TASKS = register("golem_tasks", () -> AttachmentType.builder(GolemTasks::new).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Set<BlockPos>>> EAR_INDEX = register("ear_index",
            () -> AttachmentType.<Set<BlockPos>>builder(() -> ConcurrentHashMap.newKeySet()).build());

    private TCAttachments() {}

    private static <T> DeferredHolder<AttachmentType<?>, AttachmentType<T>> register(String name, Supplier<AttachmentType<T>> supplier) {
        return ATTACHMENTS.register(name, supplier);
    }

    public static void register(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
    }
}
