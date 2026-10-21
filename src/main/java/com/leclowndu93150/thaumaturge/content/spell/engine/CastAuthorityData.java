package com.leclowndu93150.thaumaturge.content.spell.engine;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastAuthority;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.phys.Vec3;

final class CastAuthorityData extends SavedData {
    static final class Grant {
        static final Codec<Grant> CODEC = RecordCodecBuilder
                .create(builder -> builder.group(CastAuthority.CODEC.fieldOf("authority").forGetter(grant -> grant.authority), Vec3.CODEC.fieldOf("origin").forGetter(grant -> grant.origin),
                        Codec.intRange(1, 128).fieldOf("radius").forGetter(grant -> grant.radius), Codec.LONG.fieldOf("expires").forGetter(grant -> grant.expires),
                        Codec.BOOL.fieldOf("offline").forGetter(grant -> grant.offline), Codec.intRange(0, 2).fieldOf("state").forGetter(grant -> grant.state),
                        Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.LONG).fieldOf("tokens").forGetter(grant -> grant.tokens)).apply(builder, Grant::new));

        final CastAuthority authority;
        final Vec3 origin;
        final int radius;
        final long expires;
        final boolean offline;
        int state;
        final Map<UUID, Long> tokens;

        Grant(CastAuthority authority, Vec3 origin, int radius, long expires, boolean offline, int state, Map<UUID, Long> tokens) {
            this.authority = authority;
            this.origin = origin;
            this.radius = radius;
            this.expires = expires;
            this.offline = offline;
            this.state = state;
            this.tokens = new HashMap<>(tokens);
        }
    }

    static final Codec<CastAuthorityData> CODEC = RecordCodecBuilder
            .create(builder -> builder.group(Grant.CODEC.listOf().fieldOf("grants").forGetter(data -> new ArrayList<>(data.grants.values()))).apply(builder, CastAuthorityData::new));
    private static final SavedDataType<CastAuthorityData> TYPE = new SavedDataType<>(TTIds.rl("cast_authority"), CastAuthorityData::new, CODEC, DataFixTypes.LEVEL);
    final Map<UUID, Grant> grants = new HashMap<>();

    private CastAuthorityData() {}

    private CastAuthorityData(List<Grant> grants) {
        grants.forEach(grant -> this.grants.put(grant.authority.grantId(), grant));
    }

    static CastAuthorityData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }
}
