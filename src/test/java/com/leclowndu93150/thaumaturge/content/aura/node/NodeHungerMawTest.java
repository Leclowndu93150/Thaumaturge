package com.leclowndu93150.thaumaturge.content.aura.node;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NodeHungerMawTest {
    @Test
    void swallowsOnlyInsideTheCurrentMawRadius() {
        assertFalse(NodeHunger.shouldSwallow(1.4));
        assertTrue(NodeHunger.shouldSwallow(1.399));
        assertFalse(NodeHunger.shouldSwallow(1.4001));
    }
}
