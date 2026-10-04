package com.leclowndu93150.thaumaturge.content.golem;

import com.leclowndu93150.thaumaturge.api.golems.GolemHelper;
import com.leclowndu93150.thaumaturge.api.golems.ProvisionRequest;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealType;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealHandler;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskBoard;
import com.leclowndu93150.thaumaturge.registry.TCSeals;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class GolemBindings implements GolemHelper.Bindings {
    @Override
    public Optional<SealType> sealType(Identifier id) {
        return TCSeals.registry().getOptional(id);
    }

    @Override
    public @Nullable ISealEntity getSealEntity(Level level, @Nullable SealPos pos) {
        return SealHandler.getSealEntity(level, pos);
    }

    @Override
    public void addGolemTask(Level level, Task task) {
        TaskBoard.of(level).post(task);
    }

    @Override
    public List<ProvisionRequest> getProvisionRequests(Level level) {
        return TaskBoard.of(level).wants();
    }
}
