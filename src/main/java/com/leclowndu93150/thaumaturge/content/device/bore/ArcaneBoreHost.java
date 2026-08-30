package com.leclowndu93150.thaumaturge.content.device.bore;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.common.util.FakePlayer;

public interface ArcaneBoreHost {
    Level boreLevel();

    BlockPos borePos();

    Vec3 borePosition();

    float boreEyeHeight();

    Direction boreFacing();

    boolean boreActive();

    RandomSource boreRandom();

    CollisionContext boreCollisionContext();

    ItemStack boreTool();

    void setBoreTool(ItemStack stack);

    void hurtBoreTool();

    void aimBore(double x, double y, double z, float yawStep, float pitchStep);

    void setBoreDigging(boolean digging);

    void playBoreSound(SoundEvent sound, float volume, float pitch);

    FakePlayer boreDigger(ServerLevel level);

    void dropBoreOutput(ServerLevel level, ItemStack stack);

    void showBoreDig(ServerLevel level, BlockPos target, int delay);

    float boreHealth();

    float boreMaxHealth();

    boolean boreValid();

    Component boreDisplayName();

    void writeBoreRef(RegistryFriendlyByteBuf buf);

    default Vec3 boreEye() {
        Vec3 position = borePosition();
        return new Vec3(position.x, position.y + boreEyeHeight(), position.z);
    }
}
