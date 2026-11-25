package dev.southside.utils.misc;

import dev.southside.utils.IMinecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public record SlotIndexRecord(int slotIndex, int count) {
   @Override
   public int hashCode() {
      return this.slotIndex;
   }

   public boolean isValid() {
      return this.slotIndex >= 0 && this.slotIndex <= 8;
   }

   public boolean isValidEx() {
      return this.slotIndex != -1;
   }

   public boolean isValidAlt() {
      return this.getHand() == Hand.MAIN_HAND;
   }

   @Override
   public boolean equals(Object obj) {
      return obj instanceof SlotIndexRecord slotIndexRecord && this.slotIndex == slotIndexRecord.slotIndex;
   }

   public ItemStack getItemStack() {
      return IMinecraft.mc.player.getInventory().getStack(this.slotIndex);
   }

   public boolean isValidImpl() {
      return this.slotIndex >= 36 && this.slotIndex <= 39;
   }

   public boolean isValidDirect() {
      return this.slotIndex >= 9 && this.slotIndex <= 35;
   }

   public boolean isValidSafe() {
      return this.getHand() == Hand.OFF_HAND;
   }

   public Hand getHand() {
      if (this.slotIndex == 45) {
         return Hand.OFF_HAND;
      } else {
         return IMinecraft.mc.player != null && this.slotIndex == IMinecraft.mc.player.getInventory().selectedSlot ? Hand.MAIN_HAND : null;
      }
   }

   public int getSlotIndex() {
      return this.slotIndex;
   }

   public int getCount() {
      return this.count;
   }
}
