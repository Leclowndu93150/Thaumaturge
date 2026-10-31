package com.leclowndu93150.thaumaturge.content.warding;

import java.util.List;
import java.util.UUID;

public record WardAccess(UUID owner, List<UUID> iron, List<UUID> gold) {}
