package dev.southside.utils;

public interface ICancellable {
   boolean isCancelled();

   void setCancelled(boolean flag);

   default void cancel() {
      this.setCancelled(true);
   }
}
