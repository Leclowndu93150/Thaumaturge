package com.leclowndu93150.thaumaturge.api.nodes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;

/** A portable snapshot. Only its matching server journal entry authorizes release. */
public record CapturedNode(int version, UUID transferId, NodeSnapshot snapshot) {
    public static final int VERSION = 1;
    public static final Codec<CapturedNode> CODEC = RecordCodecBuilder.create(instance -> instance.group(Codec.intRange(VERSION, VERSION).fieldOf("version").forGetter(CapturedNode::version),
            UUIDUtil.CODEC.fieldOf("transfer_id").forGetter(CapturedNode::transferId), NodeSnapshot.CODEC.fieldOf("node").forGetter(CapturedNode::snapshot)).apply(instance, CapturedNode::new));

    public CapturedNode {
        if (version != VERSION) {
            throw new IllegalArgumentException("Unsupported captured node version");
        }
    }
}
