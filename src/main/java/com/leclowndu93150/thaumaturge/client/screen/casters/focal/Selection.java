package com.leclowndu93150.thaumaturge.client.screen.casters.focal;

import java.util.List;

public record Selection(List<Integer> path, boolean ghost) {
    public static final Selection ROOT = new Selection(List.of(), false);

    public Selection {
        path = List.copyOf(path);
    }

    public static Selection node(List<Integer> path) {
        return new Selection(path, false);
    }

    public static Selection below(List<Integer> parent) {
        return new Selection(parent, true);
    }
}
