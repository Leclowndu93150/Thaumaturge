package com.leclowndu93150.thaumaturge.content.eldritch.encounter;

import com.leclowndu93150.thaumaturge.api.labyrinth.EncounterRole;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;

public record BoundEntity(UUID uuid, EntityType<?> type, EncounterRole role, boolean defeated, long missingSince) {
    public static final Codec<BoundEntity> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    UUIDUtil.CODEC.fieldOf("uuid").forGetter(BoundEntity::uuid),
                    BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("type").forGetter(BoundEntity::type),
                    EncounterRole.CODEC.fieldOf("role").forGetter(BoundEntity::role),
                    Codec.BOOL.fieldOf("defeated").forGetter(BoundEntity::defeated),
                    Codec.LONG.optionalFieldOf("missing_since", -1L).forGetter(BoundEntity::missingSince))
            .apply(instance, BoundEntity::new));

    static BoundEntity spawned(UUID uuid, EntityType<?> type, EncounterRole role) {
        return new BoundEntity(uuid, type, role, false, -1L);
    }

    public boolean primary() {
        return role == EncounterRole.PRIMARY;
    }

    BoundEntity defeat() {
        return new BoundEntity(uuid, type, role, true, -1L);
    }

    BoundEntity missingFrom(long gameTime) {
        return missingSince >= 0 ? this : new BoundEntity(uuid, type, role, defeated, gameTime);
    }

    BoundEntity present() {
        return missingSince < 0 ? this : new BoundEntity(uuid, type, role, defeated, -1L);
    }
}
