package com.talhanation.smallships.world.entity.ship.abilities;

import com.talhanation.smallships.world.entity.ship.Ship;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Stack;

public interface Shieldable extends Ability {

    ShieldPosition getShieldPosition(int index);
    byte getMaxShieldsPerSide();

    default void tickShieldShip() {
    }

    default void defineShieldShipSynchedData() {
        self().getEntityData().define(Ship.SHIELD_DATA, new CompoundTag());
    }

    default void readShieldShipSaveData(CompoundTag compoundTag) {
        this.loadSaveData(compoundTag);
        self().setData(Ship.SHIELD_DATA, this.getSaveData());
    }

    default void addShieldShipSaveData(CompoundTag compoundTag) {
        this.loadSaveData(self().getData(Ship.SHIELD_DATA));
        this.addSaveData(compoundTag);
    }

    private void addSaveData(CompoundTag tag) {
        ListTag listTag = new ListTag();

        for (int i = 0; i < this.getShields().size(); ++i) {
            ItemStack itemStack = this.getShields().get(i);
            if (!itemStack.isEmpty()) {
                CompoundTag compoundTag = new CompoundTag();
                itemStack.save(compoundTag);
                listTag.add(compoundTag);
            }
        }
        tag.put("Shields", listTag);
    }

    private CompoundTag getSaveData() {
        CompoundTag compoundTag = new CompoundTag();
        this.addSaveData(compoundTag);
        return compoundTag;
    }

    private void loadSaveData(CompoundTag tag) {
        self().SHIELDS.clear();
        if (tag.contains("Shields", 9)) {
            ListTag listTag = tag.getList("Shields", 10);

            for (int i = 0; i < listTag.size(); ++i) {
                CompoundTag compoundTag = listTag.getCompound(i);
                ItemStack itemStack = ItemStack.of(compoundTag);
                self().SHIELDS.push(itemStack);
            }
        }
    }

    default float getDamageModifier() {
        return 1.0F - ((float) getShields().size() / (this.getMaxShieldsPerSide() * 2)) * 0.3F; //TODO: make maximum of 30% damage reduction configurable
    }

   default boolean interactShield(Player player, InteractionHand interactionHand) {
       ItemStack itemStack = player.getItemInHand(interactionHand);
       int shieldCount = this.getShields().size();
       if (itemStack.is(Items.SHIELD)) {
           if (shieldCount >= this.getMaxShieldsPerSide() * 2) {
               return false;
           } else {
               this.getShields().push(itemStack.copy());
               if (!player.isCreative()) itemStack.shrink(1);
               self().getLevel().playSound(player, self().getX(), self().getY() + 4, self().getZ(), SoundEvents.WOOD_HIT, self().getSoundSource(), 15.0F, 1.5F);
               return true;
           }
       } else if (itemStack.getItem() instanceof AxeItem && shieldCount > 0) {
           self().spawnAtLocation(this.getShields().pop());
           self().getLevel().playSound(player, self().getX(), self().getY() + 4, self().getZ(), SoundEvents.WOOD_HIT, self().getSoundSource(), 15.0F, 1.0F);
           return true;
       }
       return false;
   }

   default Stack<ItemStack> getShields() {
        if (self().SHIELDS.isEmpty()) this.loadSaveData(self().getData(Ship.SHIELD_DATA));
        return self().SHIELDS;
   }

    @SuppressWarnings("ClassCanBeRecord")
    class ShieldPosition {
        public final double x;
        public final double y;
        public final double z;
        public final boolean isRightSided;

        public ShieldPosition(double x, double y, double z, boolean isRightSided) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.isRightSided = isRightSided;
        }
    }
}
