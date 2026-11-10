package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

class CellWorkBehaviorPortTest {
    @Test
    void doesNotInspectOrPostWorkInAnUnloadedChunk() {
        ServerLevel level = mock(ServerLevel.class);
        when(level.hasChunk(anyInt(), anyInt())).thenReturn(false);
        ISealEntity seal = mock(ISealEntity.class);
        BlockPos sealBlock = new BlockPos(8, 64, 8);
        when(seal.pos()).thenReturn(new SealPos(sealBlock, Direction.NORTH));
        when(seal.area()).thenReturn(new BlockPos(1, 1, 1));
        ProbeBehavior behavior = new ProbeBehavior();

        behavior.tick(level, seal);

        assertFalse(behavior.inspectedCell);
        verify(level).hasChunk(0, 0);
    }

    private static final class ProbeBehavior extends CellWorkBehavior {
        private boolean inspectedCell;

        private ProbeBehavior() {
            super(1);
        }

        @Override
        protected boolean isWorkable(Level level, ISealEntity seal, BlockPos pos) {
            inspectedCell = true;
            return true;
        }

        @Override
        public boolean completeTask(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
            return true;
        }
    }
}
