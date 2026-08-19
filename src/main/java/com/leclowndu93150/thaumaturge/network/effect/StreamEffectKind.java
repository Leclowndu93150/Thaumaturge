package com.leclowndu93150.thaumaturge.network.effect;

public enum StreamEffectKind {
    ARC, BOLT, BEAM, ESSENTIA, BORE, VOID;

    private static final StreamEffectKind[] BY_ORDINAL = values();

    public static StreamEffectKind byOrdinal(int ordinal) {
        return BY_ORDINAL[ordinal];
    }
}
