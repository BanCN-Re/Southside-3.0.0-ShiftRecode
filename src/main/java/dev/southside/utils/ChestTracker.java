package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.events.ClientWorldEvent;
import dev.southside.event.events.DirectionEvent;
import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockEventS2CPacket;
import net.minecraft.util.math.BlockPos;

public class ChestTracker implements IMinecraft, IChatFeedback {
   @SerializedName("pendingOpenChests")
   public Queue pendingOpenChests = new LinkedBlockingQueue();
   @SerializedName("openedChests")
   public Queue<BlockPos> openedChests = new LinkedBlockingQueue<>();
   @SerializedName("openedChestHistory")
   public Set openedChestHistory = new LinkedHashSet();
   @SerializedName("chestPositions")
   public Set chestPositions = new LinkedHashSet();

   public void remove(BlockPos pos) {
      if (this.pendingOpenChests.remove(pos)) {
         this.openedChests.offer(pos);
      } else if (!this.openedChests.contains(pos)) {
         this.chestPositions.add(pos);
      }
   }

   public BlockPos findBlockPos() {
      Screen currentScreen = mc.currentScreen;
      return currentScreen instanceof GenericContainerScreen && !this.openedChests.isEmpty() ? this.openedChests.peek() : null;
   }

   public Set getChestPositions() {
      return this.chestPositions;
   }

   @EventTarget
   public void onClientWorld(ClientWorldEvent clientWorldEvent) {
      this.pendingOpenChests.clear();
      this.openedChestHistory.clear();
      this.chestPositions.clear();
      this.openedChests.clear();
   }

   public void performAction() {
      while (!this.openedChests.isEmpty()) {
         BlockPos pos = this.openedChests.poll();
         if (pos != null) {
            this.openedChestHistory.add(pos);
            if (this.openedChests.remove(pos)) {
               this.openedChestHistory.add(pos);
            }

            this.chestPositions.add(pos);
         }
      }
   }

   public Queue getPendingOpenChests() {
      return this.pendingOpenChests;
   }

   public Queue<BlockPos> getOpenedChests() {
      return this.openedChests;
   }

   public Set getOpenedChestHistory() {
      return this.openedChestHistory;
   }

   @EventTarget
   public void onDirection(DirectionEvent directionEvent) {
      if (directionEvent.getPacket() instanceof BlockEventS2CPacket blockEventS2CPacket) {
         if (mc.world != null) {
            BlockState blockState = mc.world.getBlockState(blockEventS2CPacket.getPos());
            if (blockState.getBlock() instanceof ChestBlock && blockEventS2CPacket.getType() == 1 && blockEventS2CPacket.getData() == 1) {
               this.remove(blockEventS2CPacket.getPos());
            }
         }
      } else if (directionEvent.getPacket() instanceof PlayerInteractBlockC2SPacket playerInteractBlockC2SPacket) {
         BlockPos blockPos = playerInteractBlockC2SPacket.getBlockHitResult().getBlockPos();
         if (mc.world != null) {
            BlockState blockState2 = mc.world.getBlockState(blockPos);
            if (blockState2.getBlock() instanceof ChestBlock && !this.openedChests.contains(blockPos) && !this.pendingOpenChests.contains(blockPos)) {
               this.pendingOpenChests.offer(blockPos);
            }
         }
      }
   }
}
