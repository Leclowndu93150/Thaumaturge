package com.leclowndu93150.thaumaturge.content.essentia.storage;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.items.ILabel;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

public final class LabelledVesselActions {
    private static final float LABEL_VOLUME = 1.0F;
    private static final float NEUTRAL_PITCH = 1.0F;
    private static final float POUR_JAR_VOLUME = 0.4F;
    private static final float POUR_FILL_VOLUME = 0.6F;
    private static final float POUR_FILL_BASE_PITCH = 0.85F;
    private static final float POUR_FILL_SPREAD = 0.3F;

    private LabelledVesselActions() {}

    public static void removeLabel(Level level, BlockPos pos, Direction clickedFace) {
        playLabelSound(level, pos);
        Block.popResourceFromFace(level, pos, clickedFace, new ItemStack(TTItems.LABEL.get()));
    }

    public static void pourOut(Level level, BlockPos pos, int amount) {
        if (amount > 0) {
            AuraHelper.addFlux(level, pos, amount);
        }
        RandomSource random = level.getRandom();
        level.playSound(null, pos, TTSounds.JAR.get(), SoundSource.BLOCKS, POUR_JAR_VOLUME, NEUTRAL_PITCH);
        level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, POUR_FILL_VOLUME, POUR_FILL_BASE_PITCH + random.nextFloat() * POUR_FILL_SPREAD);
    }

    public static void playLabelSound(Level level, BlockPos pos) {
        level.playSound(null, pos, TTSounds.PAGE.get(), SoundSource.BLOCKS, LABEL_VOLUME, NEUTRAL_PITCH);
    }

    public static @Nullable ResourceKey<IAspect> aspectToLabel(ItemStack labelStack, @Nullable ResourceKey<IAspect> existingFilter, @Nullable ResourceKey<IAspect> heldAspect, int amount) {
        if (!(labelStack.getItem() instanceof ILabel label) || existingFilter != null) {
            return null;
        }
        if (amount > 0 && heldAspect != null) {
            return heldAspect;
        }
        return label.getFilteredAspect(labelStack);
    }

    public static Direction labelSideFacing(Player player) {
        return player.getDirection().getOpposite();
    }
}
