package com.leclowndu93150.thaumaturge.content.golem.seals;

import com.leclowndu93150.thaumaturge.api.golems.seals.ISealBehavior;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealFilter;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealSetting;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealType;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskBoard;
import com.leclowndu93150.thaumaturge.content.legacy.LegacyIds;
import com.leclowndu93150.thaumaturge.network.ClientboundSealPayload;
import com.leclowndu93150.thaumaturge.registry.TTSeals;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;

public final class SealEntity implements ISealEntity {
    public static final Codec<SealEntity> CODEC = LegacyIds.IDENTIFIER_CODEC.partialDispatch("type", seal -> DataResult.success(seal.typeId), SealEntity::formatFor);

    private static final BlockPos UNIT_AREA = new BlockPos(1, 1, 1);
    private static final int OPENING_WIDTH = 3;

    private final SealPos pos;
    private final Identifier typeId;
    private final SealType type;
    private final Contents contents;
    private byte priority;
    private byte color;
    private boolean locked;
    private boolean redstoneControlled;
    private @Nullable UUID owner;
    private BlockPos area;
    private boolean halted;

    private SealEntity(SealPos pos, Identifier typeId, SealType type, Contents contents, BlockPos area) {
        this.pos = pos;
        this.typeId = typeId;
        this.type = type;
        this.contents = contents;
        this.area = area;
    }

    public static SealEntity place(SealPos pos, Identifier typeId, SealType type, UUID owner) {
        SealEntity seal = new SealEntity(pos, typeId, type, Contents.fresh(type), type.hasArea() ? openingArea(pos.face()) : UNIT_AREA);
        seal.owner = owner;
        return seal;
    }

    private static BlockPos openingArea(Direction face) {
        return new BlockPos(face.getStepX() == 0 ? OPENING_WIDTH : 1, face.getStepY() == 0 ? OPENING_WIDTH : 1, face.getStepZ() == 0 ? OPENING_WIDTH : 1);
    }

    private static DataResult<MapCodec<SealEntity>> formatFor(Identifier typeId) {
        return TTSeals.registry().getOptional(typeId).map(type -> DataResult.success(format(typeId, type))).orElseGet(() -> DataResult.error(() -> "Unknown seal type " + typeId));
    }

    private static MapCodec<SealEntity> format(Identifier typeId, SealType type) {
        Codec<Contents> contents = Contents.codec(type).codec();
        return RecordCodecBuilder
                .mapCodec(instance -> instance
                        .group(SealPos.CODEC.fieldOf("pos").forGetter(SealEntity::pos), Codec.BYTE.optionalFieldOf("priority", (byte) 0).forGetter(SealEntity::priority),
                                Codec.BYTE.optionalFieldOf("color", (byte) 0).forGetter(SealEntity::color), Codec.BOOL.optionalFieldOf("locked", false).forGetter(SealEntity::isLocked),
                                Codec.BOOL.optionalFieldOf("redstone", false).forGetter(SealEntity::isRedstoneControlled),
                                UUIDUtil.CODEC.optionalFieldOf("owner").forGetter(seal -> Optional.ofNullable(seal.owner)),
                                BlockPos.CODEC.optionalFieldOf("area", UNIT_AREA).forGetter(SealEntity::area), contents.lenientOptionalFieldOf("data").forGetter(seal -> Optional.of(seal.contents)))
                        .apply(instance, (pos, priority, color, locked, redstone, owner, area, data) -> {
                            SealEntity seal = new SealEntity(pos, typeId, type, data.orElseGet(() -> Contents.fresh(type)), area);
                            seal.priority = priority;
                            seal.color = color;
                            seal.locked = locked;
                            seal.redstoneControlled = redstone;
                            seal.owner = owner.orElse(null);
                            return seal;
                        }));
    }

    public void tick(ServerLevel level) {
        if (isStoppedByRedstone(level)) {
            if (!halted) {
                TaskBoard.of(level).suspendAllFrom(pos);
            }
            halted = true;
            return;
        }
        halted = false;
        contents.behavior().tick(level, this);
    }

    @Override
    public SealPos pos() {
        return pos;
    }

    public Identifier typeId() {
        return typeId;
    }

    @Override
    public SealType type() {
        return type;
    }

    @Override
    public ISealBehavior behavior() {
        return contents.behavior();
    }

    @Override
    public Optional<ISealFilter> filter() {
        return contents.filter().map(ISealFilter.class::cast);
    }

    @Override
    public boolean setting(SealSetting setting) {
        return contents.settings().get(setting);
    }

    @Override
    public void setSetting(SealSetting setting, boolean value) {
        contents.settings().set(setting, value);
    }

    @Override
    public byte priority() {
        return priority;
    }

    @Override
    public void setPriority(byte priority) {
        this.priority = priority;
    }

    @Override
    public byte color() {
        return color;
    }

    @Override
    public void setColor(byte color) {
        this.color = color;
    }

    @Override
    public BlockPos area() {
        return area;
    }

    @Override
    public void setArea(BlockPos area) {
        this.area = area;
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
    public boolean isRedstoneControlled() {
        return redstoneControlled;
    }

    @Override
    public void setRedstoneControlled(boolean controlled) {
        this.redstoneControlled = controlled;
    }

    @Override
    public @Nullable UUID owner() {
        return owner;
    }

    @Override
    public void setOwner(@Nullable UUID owner) {
        this.owner = owner;
    }

    @Override
    public boolean isStoppedByRedstone(Level level) {
        return redstoneControlled && (level.hasNeighborSignal(pos.pos()) || level.hasNeighborSignal(pos.pos().relative(pos.face())));
    }

    @Override
    public void markChanged(Level level) {
        if (level instanceof ServerLevel server) {
            SealHandler.markDirty(server, pos.pos());
            PacketDistributor.sendToPlayersInDimension(server, ClientboundSealPayload.update(this));
        }
    }

    private record Contents(Optional<SealFilterState> filter, SealSettingValues settings, ISealBehavior behavior) {
        static Contents fresh(SealType type) {
            return new Contents(type.filter().map(SealFilterState::new), new SealSettingValues(type), type.newBehavior());
        }

        static MapCodec<Contents> codec(SealType type) {
            MapCodec<Optional<SealFilterState>> filter = type.filter().map(spec -> SealFilterState.codec(spec).xmap(Optional::of, Optional::orElseThrow))
                    .orElseGet(() -> MapCodec.unit(Optional::empty));
            return RecordCodecBuilder.mapCodec(
                    instance -> instance.group(filter.forGetter(Contents::filter), SealSettingValues.codec(type).forGetter(Contents::settings), type.behaviorCodec().forGetter(Contents::behavior))
                            .apply(instance, Contents::new));
        }
    }
}
