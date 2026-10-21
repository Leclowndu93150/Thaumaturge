package com.leclowndu93150.thaumaturge.api.golems.duty;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.resources.Identifier;

/** Registry of exclusive duties. Registration must use a single mod initialization path. */
public final class GolemDuties {
    private static final Map<Identifier, GolemDuty> DUTIES = new LinkedHashMap<>();

    private GolemDuties() {}

    /** Registers one duty per accessory. Duplicate ids and accessory bindings are rejected. */
    public static GolemDuty register(GolemDuty duty) {
        Objects.requireNonNull(duty.id(), "id");
        Objects.requireNonNull(duty.accessory(), "accessory");
        if (duty.version() < 0 || DUTIES.containsKey(duty.id()) || forAccessory(duty.accessory()).isPresent()) {
            throw new IllegalArgumentException("Invalid or duplicate golem duty " + duty.id());
        }
        DUTIES.put(duty.id(), duty);
        return duty;
    }

    public static Optional<GolemDuty> get(Identifier id) {
        return Optional.ofNullable(DUTIES.get(id));
    }

    public static Optional<GolemDuty> forAccessory(Identifier accessory) {
        return DUTIES.values().stream().filter(duty -> duty.accessory().equals(accessory)).findFirst();
    }
}
