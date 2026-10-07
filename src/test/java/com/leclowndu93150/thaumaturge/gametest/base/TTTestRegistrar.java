package com.leclowndu93150.thaumaturge.gametest.base;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.Thaumaturge;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

public final class TTTestRegistrar {
    private static final Identifier STRUCTURE = TTIds.rl("empty_5x5x7");

    private final RegisterGameTestsEvent event;
    private final Holder<TestEnvironmentDefinition<?>> environment;

    public TTTestRegistrar(RegisterGameTestsEvent event) {
        this.event = event;
        this.environment = event.registerEnvironment(TTIds.rl("default"), new TestEnvironmentDefinition.AllOf(List.of()));
    }

    public void add(String name, int maxTicks, Consumer<GameTestHelper> body) {
        add(name, maxTicks, 0, body);
    }

    public void add(String name, int maxTicks, int setupTicks, Consumer<GameTestHelper> body) {
        TestData<Holder<TestEnvironmentDefinition<?>>> info = new TestData<>(environment, STRUCTURE, maxTicks, setupTicks, true, Rotation.NONE);
        try {
            event.registerTest(TTIds.rl(name), new TTInlineTest(info, body));
        } catch (Throwable t) {
            Thaumaturge.LOGGER.error("Failed to register gametest {}", name, t);
        }
    }
}
