package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.client.effect.ClientEffects;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;

public class EntityFollowingItem extends EntitySpecialItem implements IEntityWithComplexSpawn {
    private static final int BUBBLE_TYPE = 10;
    private static final double DEFAULT_GRAVITY = 0.04;

    private double targetX;
    private double targetY;
    private double targetZ;
    private int type = 3;
    public Entity target = null;
    private int age = 20;
    private double gravity = DEFAULT_GRAVITY;

    public EntityFollowingItem(EntityType<? extends EntityFollowingItem> type, Level level) {
        super(type, level);
    }

    public EntityFollowingItem(Level level, double x, double y, double z, ItemStack stack) {
        super(TCEntities.FOLLOWING_ITEM.get(), level);
        this.setPos(x, y, z);
        this.setItem(stack);
        this.setYRot((float) (Math.random() * 360.0));
        this.lifespan = stack.getItem() == null ? 6000 : stack.getEntityLifespan(level);
    }

    public EntityFollowingItem(Level level, double x, double y, double z, ItemStack stack, Entity target, int type) {
        this(level, x, y, z, stack);
        this.target = target;
        this.targetX = target.getX();
        this.targetY = target.getBoundingBox().minY + target.getBbHeight() / 2.0F;
        this.targetZ = target.getZ();
        this.type = type;
        this.noPhysics = true;
    }

    public EntityFollowingItem(Level level, double x, double y, double z, ItemStack stack, double tx, double ty, double tz) {
        this(level, x, y, z, stack);
        this.targetX = tx;
        this.targetY = ty;
        this.targetZ = tz;
    }

    @Override
    public void tick() {
        if (this.target != null) {
            this.targetX = this.target.getX();
            this.targetY = this.target.getBoundingBox().minY + this.target.getBbHeight() / 2.0F;
            this.targetZ = this.target.getZ();
        }

        if (this.targetX == 0.0 && this.targetY == 0.0 && this.targetZ == 0.0) {
            this.setDeltaMovement(this.getDeltaMovement().subtract(0.0, this.gravity, 0.0));
        } else {
            float xd = (float) (this.targetX - this.getX());
            float yd = (float) (this.targetY - this.getY());
            float zd = (float) (this.targetZ - this.getZ());
            if (this.age > 1) {
                this.age--;
            }

            double distance = Mth.sqrt(xd * xd + yd * yd + zd * zd);
            if (distance > 0.5) {
                distance *= this.age;
                this.setDeltaMovement(xd / distance, yd / distance, zd / distance);
            } else {
                this.setDeltaMovement(this.getDeltaMovement().scale(0.1));
                this.targetX = 0.0;
                this.targetY = 0.0;
                this.targetZ = 0.0;
                this.target = null;
                this.noPhysics = false;
            }

            if (this.level().isClientSide()) {
                float h = (float) ((this.getBoundingBox().maxY - this.getBoundingBox().minY) / 2.0) + Mth.sin(this.getAge() / 10.0F + this.bobOffs) * 0.1F + 0.1F;
                double fx = this.xo + (this.random.nextFloat() - this.random.nextFloat()) * 0.125F;
                double fy = this.yo + h + (this.random.nextFloat() - this.random.nextFloat()) * 0.125F;
                double fz = this.zo + (this.random.nextFloat() - this.random.nextFloat()) * 0.125F;
                if (this.type != BUBBLE_TYPE) {
                    ClientEffects.nitorCore(this.level(), fx, fy, fz, this.random.nextGaussian() * 0.01, this.random.nextGaussian() * 0.01, this.random.nextGaussian() * 0.01, 0xFFFFFF);
                } else {
                    ClientEffects.followingBubble(this.level(), fx, fy, fz);
                }
            }
        }

        super.tick();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putShort("type", (short) this.type);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.type = input.getShortOr("type", (short) 3);
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        buffer.writeInt(this.target == null ? -1 : this.target.getId());
        buffer.writeDouble(this.targetX);
        buffer.writeDouble(this.targetY);
        buffer.writeDouble(this.targetZ);
        buffer.writeByte(this.type);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf buffer) {
        int id = buffer.readInt();
        if (id > -1) {
            this.target = this.level().getEntity(id);
        }
        this.targetX = buffer.readDouble();
        this.targetY = buffer.readDouble();
        this.targetZ = buffer.readDouble();
        this.type = buffer.readByte();
    }
}
