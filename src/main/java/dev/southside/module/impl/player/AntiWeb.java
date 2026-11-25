package dev.southside.module.impl.player;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.StateEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.NumberValue;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.world.World;

public class AntiWeb extends Module {
   @SerializedName("recycleTick")
   public final NumberValue recycleTick = new NumberValue("Recycle Tick", 10, 5, 20, 1);
   @SerializedName("waterPlacement")
   private BlockPos waterPlacement;
   @SerializedName("recycleTicks")
   private int recycleTicks;

   public AntiWeb() {
      super(Category.Player, "AntiWeb", "Anti Webs");
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.waterPlacement = null;
      this.recycleTicks = 0;
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      if (stateEvent.getState() == Stage.PRE && mc2.player != null && mc2.world != null) {
         BlockPos blockPos = mc2.player.getBlockPos();
         if (this.waterPlacement == null && mc2.world.getBlockState(blockPos).isOf(Blocks.COBWEB)) {
            this.updateBlock(blockPos);
         } else if (this.waterPlacement != null) {
            this.recycleTicks++;
            if (this.recycleTicks >= ((Number)this.recycleTick.getValue()).intValue() || !isValid(mc2.world, this.waterPlacement)) {
               this.performActionAlt();
            }
         }
      }
   }

   private void updateBlock(BlockPos pos) {
      int n = this.computeInt(Items.WATER_BUCKET);
      if (n != -1) {
         BlockPos pos2 = findBlockPos(mc2.world, pos);
         if (pos2 != null && this.isKeyDown(n)) {
            this.waterPlacement = pos2;
            this.recycleTicks = 0;
         }
      }
   }

   private void performActionAlt() {
      int n = this.computeInt(Items.BUCKET);
      if (n != -1 && this.isKeyDown(n)) {
         this.waterPlacement = null;
         this.recycleTicks = 0;
      }
   }

   private boolean isKeyDown(int n) {
      if (mc2.player != null && mc2.interactionManager != null) {
         int selectedSlot = mc2.player.getInventory().selectedSlot;
         float pitch = mc2.player.getPitch();

         try {
            mc2.player.getInventory().selectedSlot = n;
            mc2.player.setPitch(90.0F);
            mc2.doItemUse();
            return true;
         } finally {
            mc2.player.setPitch(pitch);
            mc2.player.getInventory().selectedSlot = selectedSlot;
         }
      } else {
         return false;
      }
   }

   private int computeInt(Item item) {
      for (int i = 0; i < 9; i++) {
         if (mc2.player.getInventory().getStack(i).getItem() == item) {
            return i;
         }
      }

      return -1;
   }

   private static BlockPos findBlockPos(World world, BlockPos pos) {
      BlockPos[] posArray = new BlockPos[]{
         pos.up(), pos.north(), pos.south(), pos.east(), pos.west(), pos.up().north(), pos.up().south(), pos.up().east(), pos.up().west()
      };

      for (BlockPos pos2 : posArray) {
         if (world.getBlockState(pos2).isAir() && !world.getBlockState(pos2.down()).isAir()) {
            return pos2;
         }
      }

      return null;
   }

   private static boolean isValid(World world, BlockPos pos) {
      Mutable mutablePos = new Mutable();

      for (int i = -1; i <= 1; i++) {
         for (int j = -1; j <= 1; j++) {
            for (int k = -1; k <= 1; k++) {
               mutablePos.set(pos.getX() + i, pos.getY() + j, pos.getZ() + k);
               if (world.getBlockState(mutablePos).isOf(Blocks.COBWEB)) {
                  return true;
               }
            }
         }
      }

      return false;
   }
}
