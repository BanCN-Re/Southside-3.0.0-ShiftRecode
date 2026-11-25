package dev.southside.event.events;

import com.google.gson.annotations.SerializedName;
import dev.southside.utils.CancellableEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class HandEvent extends CancellableEvent {
   @SerializedName("hand")
   public Hand hand;
   @SerializedName("itemStack")
   public ItemStack itemStack;

   public HandEvent(Hand hand2, ItemStack stack) {
      this.hand = hand2;
      this.itemStack = stack;
   }

   public void setItemStack(ItemStack stack) {
      this.itemStack = stack;
   }

   public void setItemStack2(ItemStack stack) {
      this.itemStack = stack;
   }

   public Hand getHand() {
      return this.hand;
   }

   public ItemStack getItemStack() {
      return this.itemStack;
   }
}
