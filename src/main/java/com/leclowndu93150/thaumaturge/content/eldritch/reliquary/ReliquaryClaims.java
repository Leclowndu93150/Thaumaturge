package com.leclowndu93150.thaumaturge.content.eldritch.reliquary;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthPhase;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import com.leclowndu93150.thaumaturge.config.labyrinth.PearlPolicy;
import com.leclowndu93150.thaumaturge.content.eldritch.encounter.EncounterResolver;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthData;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthService;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeRecord;
import com.leclowndu93150.thaumaturge.content.eldritch.lock.LabyrinthKeys;
import com.leclowndu93150.thaumaturge.content.misc.TTActionBar;
import com.leclowndu93150.thaumaturge.registry.TTLootTables;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

final class ReliquaryClaims {
    private static final int CLAIM_PARTICLES = 24;
    private static final double PARTICLE_SPREAD = 0.25;
    private static final double PARTICLE_SPEED = 0.08;
    private static final double ALCOVE_OFFSET = 0.15;

    private ReliquaryClaims() {}

    static void use(ServerLevel level, BlockPos pos, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)
                || !(level.getBlockEntity(pos) instanceof BlockEntityEldritchReliquary reliquary)) {
            return;
        }
        Optional<MazeRecord> record = ReliquaryViews.record(level, reliquary);
        if (record.isEmpty()) {
            TTActionBar.sendPurple(player, "gui.thaumaturge.labyrinth.unavailable");
            return;
        }
        switch (ReliquaryViews.view(record.get(), reliquary, player.getUUID())) {
            case CLAIMABLE -> claim(level, reliquary, record.get(), serverPlayer);
            case CLAIMED -> reissue(reliquary, record.get(), serverPlayer);
            case INELIGIBLE -> TTActionBar.sendPurple(player, "gui.thaumaturge.reliquary.ineligible");
            case WARDED -> TTActionBar.sendPurple(player, "gui.thaumaturge.reliquary.warded");
        }
        ReliquaryViews.send(reliquary, record.get(), serverPlayer, true);
    }

    private static void claim(
            ServerLevel level, BlockEntityEldritchReliquary reliquary, MazeRecord record, ServerPlayer player) {
        BlockPos pos = reliquary.getBlockPos();
        for (ResourceKey<LootTable> table : tables(level, reliquary, record, player)) {
            roll(level, pos, player, table);
        }
        if (reliquary.role() == ReliquaryRole.KEY_ROOM && record.state().phase() == LabyrinthPhase.SEALED) {
            player.getInventory()
                    .placeItemBackInInventory(LabyrinthKeys.bound(record.plan().id()));
        }
        record.state().claims().claim(reliquary.claimKey(), player.getUUID());
        LabyrinthData.get(level).setDirty();
        Vec3 alcove = Vec3.atCenterOf(pos)
                .add(Vec3.atLowerCornerOf(level.getBlockState(pos)
                                .getValue(BlockEldritchReliquary.FACING)
                                .getNormal())
                        .scale(ALCOVE_OFFSET));
        level.sendParticles(
                ParticleTypes.REVERSE_PORTAL,
                alcove.x,
                alcove.y,
                alcove.z,
                CLAIM_PARTICLES,
                PARTICLE_SPREAD,
                PARTICLE_SPREAD,
                PARTICLE_SPREAD,
                PARTICLE_SPEED);
        level.playSound(null, pos, SoundEvents.VAULT_OPEN_SHUTTER, SoundSource.BLOCKS, 1.0F, 1.0F);
        TTActionBar.sendPurple(player, "gui.thaumaturge.reliquary.claimed");
    }

    private static void reissue(BlockEntityEldritchReliquary reliquary, MazeRecord record, ServerPlayer player) {
        if (ThaumaturgeServerConfig.LABYRINTH.reissueTablets.get()
                && reliquary.role() == ReliquaryRole.KEY_ROOM
                && record.state().phase() == LabyrinthPhase.SEALED
                && !LabyrinthKeys.carries(player, record.plan().id())) {
            player.getInventory()
                    .placeItemBackInInventory(LabyrinthKeys.bound(record.plan().id()));
            TTActionBar.sendPurple(player, "gui.thaumaturge.reliquary.reissued");
            return;
        }
        TTActionBar.sendPurple(player, "gui.thaumaturge.reliquary.already_claimed");
    }

    private static List<ResourceKey<LootTable>> tables(
            ServerLevel level, BlockEntityEldritchReliquary reliquary, MazeRecord record, ServerPlayer player) {
        List<ResourceKey<LootTable>> tables = new ArrayList<>();
        ResourceKey<LootTable> keyRoom = LabyrinthService.definition(level.getServer(), record)
                .map(definition -> definition.gameplay().keyRoomLoot())
                .orElse(TTLootTables.LABYRINTH_KEY_ROOM);
        switch (reliquary.role()) {
            case KEY_ROOM -> tables.add(keyRoom);
            case CACHE -> tables.add(reliquary.loot().orElse(TTLootTables.TREASURE_LIBRARY));
            case BOSS -> {
                tables.add(EncounterResolver.rewardTable(level, record).orElse(keyRoom));
                if (earnsPearl(record, player)) {
                    tables.add(TTLootTables.LABYRINTH_PRIMORDIAL_PEARL);
                }
            }
        }
        return tables;
    }

    private static boolean earnsPearl(MazeRecord record, ServerPlayer player) {
        PearlPolicy policy = ThaumaturgeServerConfig.LABYRINTH.pearlPolicy.get();
        return switch (policy) {
            case EACH_ELIGIBLE -> true;
            case TOP_CONTRIBUTOR ->
                record.state()
                        .encounter()
                        .participation()
                        .top()
                        .filter(player.getUUID()::equals)
                        .isPresent();
            case NONE -> false;
        };
    }

    private static void roll(ServerLevel level, BlockPos pos, ServerPlayer player, ResourceKey<LootTable> key) {
        LootTable table = level.getServer().reloadableRegistries().getLootTable(key);
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.THIS_ENTITY, player)
                .withLuck(player.getLuck())
                .create(LootContextParamSets.CHEST);
        for (ItemStack stack : table.getRandomItems(params)) {
            player.getInventory().placeItemBackInInventory(stack);
        }
    }
}
