package com.leclowndu93150.thaumaturge.content.eldritch.portal;

import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockEntityEldritchPortal extends BlockEntity {
    private static final int OPEN_TICKS = 30;
    private static final int SOUND_INTERVAL = 250;
    private static final String LINK = "link";

    private Optional<PortalLink> link = Optional.empty();
    private int openTicks = -1;
    private int age;

    public BlockEntityEldritchPortal(BlockPos pos, BlockState state) {
        super(TTBlockEntities.ELDRITCH_PORTAL.get(), pos, state);
    }

    public void clientTick(Level level, BlockPos pos) {
        age++;
        if (age % SOUND_INTERVAL == 1) {
            level.playLocalSound(
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    TTSounds.EVILPORTAL.get(),
                    SoundSource.BLOCKS,
                    1.0F,
                    1.0F,
                    false);
        }
        if (openTicks < OPEN_TICKS) {
            openTicks++;
        }
    }

    public int openTicks() {
        return openTicks;
    }

    public Optional<PortalLink> link() {
        return link;
    }

    public void setLink(PortalLink link) {
        this.link = Optional.of(link);
        setChanged();
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        link = TTNbt.read(input, LINK, PortalLink.CODEC, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        link.ifPresent(value -> TTNbt.store(output, LINK, PortalLink.CODEC, registries, value));
    }
}
