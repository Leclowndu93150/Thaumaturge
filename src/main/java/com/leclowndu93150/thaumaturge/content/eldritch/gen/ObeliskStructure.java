package com.leclowndu93150.thaumaturge.content.eldritch.gen;

import com.leclowndu93150.thaumaturge.registry.TTStructures;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.structure.SinglePieceStructure;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

public class ObeliskStructure extends SinglePieceStructure {
    public static final MapCodec<ObeliskStructure> CODEC = simpleCodec(ObeliskStructure::new);

    public ObeliskStructure(Structure.StructureSettings settings) {
        super(ObeliskPiece::new, ObeliskPiece.SIZE_XZ, ObeliskPiece.SIZE_XZ, settings);
    }

    @Override
    public StructureType<?> type() {
        return TTStructures.ELDRITCH_OBELISK.get();
    }
}
