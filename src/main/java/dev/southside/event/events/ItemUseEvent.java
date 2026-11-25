package dev.southside.event.events;

import com.google.gson.annotations.SerializedName;
import dev.southside.utils.CancellableEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class ItemUseEvent extends CancellableEvent {
   @SerializedName("hand")
   public Hand hand;
   @SerializedName("itemStack")
   public ItemStack itemStack;

   public ItemStack getItemStack() {
      return this.itemStack;
   }

   public Hand getHand() {
      return this.hand;
   }

   public ItemUseEvent(Hand hand2, ItemStack stack) {
      this.hand = hand2;
      this.itemStack = stack;
   }
}
