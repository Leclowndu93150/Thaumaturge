package com.leclowndu93150.thaumaturge.content.golem.seals;

import com.leclowndu93150.thaumaturge.api.golems.seals.ISeal;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealConfigArea;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealConfigToggles;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealType;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskHandler;
import com.leclowndu93150.thaumaturge.content.legacy.LegacyIds;
import com.leclowndu93150.thaumaturge.network.ClientboundSealPayload;
import com.leclowndu93150.thaumaturge.registry.TCSeals;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;

public final class SealEntity implements ISealEntity {
    public static final Codec<SealEntity> CODEC = RecordCodecBuilder.create(instance -> instance.group(SealPos.CODEC.fieldOf("pos").forGetter(SealEntity::sealPos),
            LegacyIds.IDENTIFIER_CODEC.fieldOf("type").forGetter(seal -> seal.typeId), Codec.BYTE.optionalFieldOf("priority", (byte) 0).forGetter(SealEntity::getPriority),
            Codec.BYTE.optionalFieldOf("color", (byte) 0).forGetter(SealEntity::getColor), Codec.BOOL.optionalFieldOf("locked", false).forGetter(SealEntity::isLocked),
            Codec.BOOL.optionalFieldOf("redstone", false).forGetter(SealEntity::isRedstoneSensitive), UUIDUtil.CODEC.optionalFieldOf("owner").forGetter(seal -> Optional.ofNullable(seal.owner)),
            BlockPos.CODEC.optionalFieldOf("area", new BlockPos(1, 1, 1)).forGetter(SealEntity::getArea),
            CompoundTag.CODEC.optionalFieldOf("data", new CompoundTag()).forGetter(SealEntity::writeSealData)).apply(instance, SealEntity::fromCodec));

    private final SealPos sealPos;
    private final Identifier typeId;
    private final ISeal seal;
    private byte priority;
    private byte color;
    private boolean locked;
    private boolean redstone;
    private UUID owner;
    private BlockPos area = new BlockPos(1, 1, 1);
    private boolean stopped;

    public SealEntity(SealPos sealPos, Identifier typeId, ISeal seal) {
        this.sealPos = sealPos;
        this.typeId = typeId;
        this.seal = seal;
        if (seal instanceof ISealConfigArea) {
            int x = sealPos.face().getStepX() == 0 ? 3 : 1;
            int y = sealPos.face().getStepY() == 0 ? 3 : 1;
            int z = sealPos.face().getStepZ() == 0 ? 3 : 1;
            this.area = new BlockPos(x, y, z);
        }
    }

    private static SealEntity fromCodec(SealPos pos, Identifier typeId, byte priority, byte color, boolean locked, boolean redstone, Optional<UUID> owner, BlockPos area, CompoundTag data) {
        SealType type = TCSeals.registry().getValue(typeId);
        if (type == null) {
            throw new IllegalStateException("Unknown seal type " + typeId);
        }
        SealEntity entity = new SealEntity(pos, typeId, type.factory().get());
        entity.priority = priority;
        entity.color = color;
        entity.locked = locked;
        entity.redstone = redstone;
        entity.owner = owner.orElse(null);
        entity.area = area;
        entity.readSealData(data);
        return entity;
    }

    private CompoundTag writeSealData() {
        CompoundTag data = new CompoundTag();
        seal.writeCustomNBT(data);
        if (seal instanceof ISealConfigToggles toggles) {
            for (ISealConfigToggles.SealToggle toggle : toggles.getToggles()) {
                data.putBoolean(toggle.getKey(), toggle.getValue());
            }
        }
        return data;
    }

    private void readSealData(CompoundTag data) {
        seal.readCustomNBT(data);
        if (seal instanceof ISealConfigToggles toggles) {
            for (ISealConfigToggles.SealToggle toggle : toggles.getToggles()) {
                data.getBoolean(toggle.getKey()).ifPresent(toggle::setValue);
            }
        }
    }

    public Identifier getTypeId() {
        return typeId;
    }

    private SealPos sealPos() {
        return sealPos;
    }

    @Override
    public void tickSealEntity(Level level) {
        if (isStoppedByRedstone(level)) {
            if (!stopped) {
                for (Task task : TaskHandler.getTasks(level).values()) {
                    if (sealPos.equals(task.getSealPos())) {
                        task.setSuspended(true);
                    }
                }
            }
            stopped = true;
            return;
        }
        stopped = false;
        seal.tickSeal(level, this);
    }

    @Override
    public boolean isStoppedByRedstone(Level level) {
        return isRedstoneSensitive() && (level.hasNeighborSignal(sealPos.pos()) || level.hasNeighborSignal(sealPos.pos().relative(sealPos.face())));
    }

    @Override
    public ISeal getSeal() {
        return seal;
    }

    @Override
    public SealPos getSealPos() {
        return sealPos;
    }

    @Override
    public byte getPriority() {
        return priority;
    }

    @Override
    public void setPriority(byte priority) {
        this.priority = priority;
    }

    @Override
    public byte getColor() {
        return color;
    }

    @Override
    public void setColor(byte color) {
        this.color = color;
    }

    @Override
    public @Nullable UUID getOwner() {
        return owner;
    }

    @Override
    public void setOwner(@Nullable UUID owner) {
        this.owner = owner;
    }

    @Override
    public boolean isLocked() {
        return locked;
    }

    @Override
    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    @Override
    public boolean isRedstoneSensitive() {
        return redstone;
    }

    @Override
    public void setRedstoneSensitive(boolean redstoneSensitive) {
        this.redstone = redstoneSensitive;
    }

    @Override
    public void syncToClient(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            PacketDistributor.sendToPlayersInDimension(serverLevel, ClientboundSealPayload.update(this));
        }
    }

    @Override
    public BlockPos getArea() {
        return area;
    }

    @Override
    public void setArea(BlockPos area) {
        this.area = area;
    }
}
