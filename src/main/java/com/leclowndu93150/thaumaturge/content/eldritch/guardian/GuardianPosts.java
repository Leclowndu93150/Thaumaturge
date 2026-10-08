package com.leclowndu93150.thaumaturge.content.eldritch.guardian;

import com.leclowndu93150.thaumaturge.api.labyrinth.EncounterRole;
import com.leclowndu93150.thaumaturge.api.labyrinth.MarkerTriggerContext;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import com.leclowndu93150.thaumaturge.content.eldritch.EldritchSpawns;
import com.leclowndu93150.thaumaturge.content.eldritch.encounter.LabyrinthBinding;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthData;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthService;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeRecord;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.PendingTrigger;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.GuardianTable;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.LabyrinthDefinition;
import com.leclowndu93150.thaumaturge.content.eldritch.portal.SafeSpot;
import com.leclowndu93150.thaumaturge.content.entity.champion.ChampionHelper;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public final class GuardianPosts {
    public static final String KEY_ROOM_WARD = "key_room";
    private static final int SPAWN_ATTEMPTS = 12;
    private static final int SPAWN_SPREAD = 3;

    private GuardianPosts() {}

    public static boolean canFire(MarkerTriggerContext context) {
        return context.level().getDifficulty() != Difficulty.PEACEFUL;
    }

    public static void fire(MarkerTriggerContext context, BlockPos pos, Optional<String> ward) {
        ServerLevel level = context.level();
        Optional<MazeRecord> record = LabyrinthService.byId(level.getServer(), context.maze());
        if (record.isEmpty()) {
            return;
        }
        Optional<GuardianTable> table =
                LabyrinthService.definition(level.getServer(), record.get()).map(LabyrinthDefinition::guardians);
        List<UUID> guards = new ArrayList<>();
        if (table.isPresent()) {
            int count = table.get().perPost().sample(context.random());
            for (int i = 0; i < count; i++) {
                table.get()
                        .entries()
                        .getRandomValue(context.random())
                        .flatMap(type -> spawn(level, record.get(), type, pos, table.get(), context.random()))
                        .ifPresent(mob -> guards.add(mob.getUUID()));
            }
        }
        record.get()
                .state()
                .posts()
                .add(new PostState(context.index(), ward, pos.immutable(), List.copyOf(guards), guards.isEmpty()));
        LabyrinthData.get(level).setDirty();
    }

    public static void refresh(ServerLevel level, MazeRecord record) {
        PostLedger ledger = record.state().posts();
        boolean changed = false;
        for (int slot = 0; slot < ledger.posts().size(); slot++) {
            PostState post = ledger.posts().get(slot);
            if (post.cleared()
                    || !LabyrinthService.entitiesLoaded(
                            level, post.pos(), ThaumaturgeServerConfig.LABYRINTH.postGuardLeashRadius.get())) {
                continue;
            }
            if (post.guards().stream()
                    .noneMatch(uuid -> level.getEntity(uuid) instanceof LivingEntity living && living.isAlive())) {
                ledger.set(slot, post.clear());
                changed = true;
            }
        }
        if (changed) {
            LabyrinthData.get(level).setDirty();
        }
    }

    public static boolean wardOpen(MazeRecord record, String group) {
        for (PendingTrigger trigger : record.state().triggers()) {
            if (trigger.marker() instanceof WardedMarker warded
                    && warded.ward().filter(group::equals).isPresent()) {
                return false;
            }
        }
        return record.state().posts().cleared(group);
    }

    private static Optional<Mob> spawn(
            ServerLevel level,
            MazeRecord record,
            EntityType<?> type,
            BlockPos post,
            GuardianTable table,
            RandomSource random) {
        Optional<Mob> prepared = EldritchSpawns.prepare(
                level,
                type,
                spot(level, post, random),
                post,
                ThaumaturgeServerConfig.LABYRINTH.postGuardLeashRadius.get(),
                random);
        if (prepared.isEmpty()) {
            return Optional.empty();
        }
        Mob mob = prepared.get();
        mob.setData(
                TTAttachments.LABYRINTH_BINDING,
                LabyrinthBinding.of(record.plan().id(), EncounterRole.MINION));
        double override = ThaumaturgeServerConfig.LABYRINTH.championChanceOverride.get();
        double chance = override >= 0.0 ? override : table.championChance();
        if (ThaumaturgeCommonConfig.ALLOW_CHAMPION_MOBS.get() && random.nextDouble() < chance) {
            ChampionHelper.makeChampion(mob, true);
        } else {
            ChampionHelper.markRolled(mob);
        }
        return level.addFreshEntity(mob) ? Optional.of(mob) : Optional.empty();
    }

    private static BlockPos spot(ServerLevel level, BlockPos post, RandomSource random) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int attempt = 0; attempt < SPAWN_ATTEMPTS; attempt++) {
            cursor.set(
                    post.getX() + random.nextIntBetweenInclusive(-SPAWN_SPREAD, SPAWN_SPREAD),
                    post.getY(),
                    post.getZ() + random.nextIntBetweenInclusive(-SPAWN_SPREAD, SPAWN_SPREAD));
            if (SafeSpot.standable(level, cursor)) {
                return cursor.immutable();
            }
        }
        return post;
    }
}
