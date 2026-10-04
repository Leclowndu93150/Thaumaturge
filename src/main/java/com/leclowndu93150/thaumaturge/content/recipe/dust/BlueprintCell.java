package com.leclowndu93150.thaumaturge.content.recipe.dust;

import com.leclowndu93150.thaumaturge.api.recipe.BlueprintPart;
import net.minecraft.core.BlockPos;

public record BlueprintCell(BlockPos offset, BlueprintPart part) {
}
