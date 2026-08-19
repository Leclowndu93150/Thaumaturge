package com.leclowndu93150.thaumaturge.content.research.table;

import net.minecraft.util.StringRepresentable;

public enum ResearchTablePart implements StringRepresentable {
    MAIN("main"), EXT("ext");

    private final String name;

    ResearchTablePart(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
