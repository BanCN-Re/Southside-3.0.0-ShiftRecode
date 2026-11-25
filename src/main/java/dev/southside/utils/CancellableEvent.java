package dev.southside.utils;

import com.google.gson.annotations.SerializedName;

public abstract class CancellableEvent implements ICancellable {
   @SerializedName("cancelled")
   public boolean cancelled;

   @Override
   public boolean isCancelled() {
      return this.cancelled;
   }

   @Override
   public void setCancelled(boolean flag) {
      this.cancelled = flag;
   }
}
