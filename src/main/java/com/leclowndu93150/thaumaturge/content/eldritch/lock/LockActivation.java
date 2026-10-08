package com.leclowndu93150.thaumaturge.content.eldritch.lock;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthPhase;
import com.leclowndu93150.thaumaturge.content.eldritch.encounter.EncounterPhases;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthService;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeRecord;
import com.leclowndu93150.thaumaturge.content.misc.TTActionBar;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

final class LockActivation {
    private LockActivation() {}

    static void use(ServerLevel level, BlockPos pos, Player player, ItemStack stack) {
        Optional<MazeRecord> record = resolve(level, pos);
        if (record.isEmpty()) {
            TTActionBar.sendPurple(player, "gui.thaumaturge.labyrinth.unavailable");
            return;
        }
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            TTActionBar.sendPurple(player, "gui.thaumaturge.labyrinth.lock.peaceful");
            return;
        }
        if (record.get().state().phase() != LabyrinthPhase.SEALED) {
            TTActionBar.sendPurple(player, "gui.thaumaturge.labyrinth.lock.open");
            return;
        }
        if (LabyrinthKeys.boundTo(stack)
                .filter(maze -> !maze.equals(record.get().plan().id()))
                .isPresent()) {
            TTActionBar.sendPurple(player, "gui.thaumaturge.labyrinth.lock.wrong_key");
            return;
        }
        stack.consume(1, player);
        EncounterPhases.charge(level, record.get());
    }

    private static Optional<MazeRecord> resolve(ServerLevel level, BlockPos pos) {
        return LabyrinthService.resolve(
                level,
                level.getBlockEntity(pos, TTBlockEntities.ELDRITCH_LOCK.get()).flatMap(BlockEntityEldritchLock::maze),
                pos);
    }
}
