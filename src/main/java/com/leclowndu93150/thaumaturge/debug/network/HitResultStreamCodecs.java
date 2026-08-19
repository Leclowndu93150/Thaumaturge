package com.leclowndu93150.thaumaturge.debug.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;

public final class HitResultStreamCodecs {

    // Must not be used on the server side, as it relies on client-side entity lookups to decode entity hit results.
    @ApiStatus.Internal
    public static final StreamCodec<FriendlyByteBuf, HitResult> HIT_RESULT = StreamCodec.of(HitResultStreamCodecs::encode, HitResultStreamCodecs::decode);

    private static void encode(FriendlyByteBuf buf, HitResult hit) {

        buf.writeEnum(hit.getType());

        switch (hit.getType()) {
            case MISS -> {
                buf.writeVector3f(hit.getLocation().toVector3f());
            }

            case BLOCK -> {
                BlockHitResult block = (BlockHitResult) hit;

                buf.writeVector3f(block.getLocation().toVector3f());
                buf.writeBlockPos(block.getBlockPos());
                buf.writeEnum(block.getDirection());
                buf.writeBoolean(block.isInside());
                buf.writeBoolean(block.isWorldBorderHit());
            }

            case ENTITY -> {
                EntityHitResult entity = (EntityHitResult) hit;

                buf.writeVector3f(entity.getLocation().toVector3f());
                buf.writeVarInt(entity.getEntity().getId());
            }
        }
    }

    private static HitResult decode(FriendlyByteBuf buf) {
        HitResult.Type type = buf.readEnum(HitResult.Type.class);

        return switch (type) {
            case MISS -> BlockHitResult.miss(new Vec3(buf.readVector3f()), Direction.NORTH, BlockPos.ZERO);

            case BLOCK -> {
                Vec3 location = new Vec3(buf.readVector3f());
                BlockPos pos = buf.readBlockPos();

                yield new BlockHitResult(location, buf.readEnum(net.minecraft.core.Direction.class), pos, buf.readBoolean(), buf.readBoolean());
            }

            case ENTITY -> {
                Vec3 location = new Vec3(buf.readVector3f());
                int entityId = buf.readVarInt();

                yield new EntityHitResult(Minecraft.getInstance().level.getEntity(entityId), location);
            }
        };
    }

    private HitResultStreamCodecs() {}
}
