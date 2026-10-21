package com.leclowndu93150.thaumaturge.content.taint.spread;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

class TaintOriginRegistryTest {
    @Test
    void movingSeedReplacesItsOldInfluenceAnchor() {
        TaintOriginRegistry registry = new TaintOriginRegistry();
        BlockPos original = new BlockPos(2, 64, 3);
        BlockPos moved = new BlockPos(4, 65, 3);

        registry.move(null, original);
        registry.move(original, moved);

        assertEquals(List.of(moved), registry.inChunk(0, 0));
    }

    @Test
    void repeatedPositionUpdateDoesNotDuplicateAnchor() {
        TaintOriginRegistry registry = new TaintOriginRegistry();
        BlockPos anchor = new BlockPos(2, 64, 3);

        registry.move(null, anchor);
        registry.move(anchor, anchor);

        assertEquals(List.of(anchor), registry.inChunk(0, 0));
    }
}
