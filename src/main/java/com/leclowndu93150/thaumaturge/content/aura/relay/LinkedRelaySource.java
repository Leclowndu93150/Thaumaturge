package com.leclowndu93150.thaumaturge.content.aura.relay;

import com.leclowndu93150.thaumaturge.api.aura.IVisRelaySource;
import net.minecraft.core.BlockPos;

public record LinkedRelaySource(BlockPos position, IVisRelaySource source) {}
