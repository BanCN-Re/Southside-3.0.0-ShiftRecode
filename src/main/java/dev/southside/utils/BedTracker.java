package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.ClientWorldEvent;
import dev.southside.event.events.StateEvent;
import net.minecraft.block.BedBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

public final class BedTracker {
   public static final BedTracker INSTANCE = new BedTracker();
   private static final MinecraftClient mc = MinecraftClient.getInstance();
   private static final int DEFAULT_INT = 20;
   @SerializedName("bedPosition")
   private BlockPos bedPosition;

   private BedTracker() {
   }

   public boolean isValid() {
      return this.bedPosition != null;
   }

   public BlockPos getBedPosition() {
      return this.bedPosition;
   }

   @EventTarget
   public void onClientWorld(ClientWorldEvent clientWorldEvent) {
      this.bedPosition = null;
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      if (stateEvent.getState() == Stage.PRE && mc.player != null && mc.world != null && this.bedPosition == null) {
         if (this.isValidEx()) {
            BlockPos blockPos = mc.player.getBlockPos();

            for (int i = -20; i <= 20; i++) {
               for (int j = -20; j <= 20; j++) {
                  for (int k = -20; k <= 20; k++) {
                     BlockPos pos = blockPos.add(i, j, k);
                     if (mc.world.getBlockState(pos).getBlock() instanceof BedBlock) {
                        this.bedPosition = pos;
                        return;
                     }
                  }
               }
            }
         }
      }
   }

   private boolean isValidEx() {
      for (int i = 0; i < 9; i++) {
         if (mc.player.getInventory().getStack(i).getItem() == Items.WOODEN_SWORD) {
            return true;
         }
      }

      return false;
   }
}
