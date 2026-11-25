package dev.southside.module.impl.misc;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.ClientWorldEvent;
import dev.southside.event.events.StateEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.NumberValue;
import dev.southside.utils.misc.AIWalkController;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import net.minecraft.block.Block;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;

public class AutoPixelParty extends Module {
   private static final int DEFAULT_INT = 10;
   private static final long DEFAULT_LONG_VALUE = 8L;
   private static final long DEFAULT_LONG_DATA = 3000L;
   @SerializedName("searchChunkRadius")
   public final NumberValue searchChunkRadius = new NumberValue("Search Chunk Radius", 6, 5, 10);
   @SerializedName("hotbarIndex")
   public final NumberValue hotbarIndex = new NumberValue("HotBar Index", 5, 1, 9, 1);
   @SerializedName("searchExecutor")
   private final ExecutorService searchExecutor = Executors.newSingleThreadExecutor(action -> {
      Thread thread = new Thread(action, "AutoPixelParty-Search");
      thread.setDaemon(true);
      thread.setPriority(1);
      return thread;
   });
   @SerializedName("activeSearch")
   public volatile AutoPixelParty.RunnableImpl activeSearch;
   @SerializedName("searchFuture")
   private Future<?> searchFuture;
   @SerializedName("targetBlock")
   private Block targetBlock;
   @SerializedName("hasTarget")
   private boolean hasTarget;
   @SerializedName("pathStopDeadline")
   private long pathStopDeadline = -1L;
   @SerializedName("pathStopGeneration")
   private int pathStopGeneration;

   public AutoPixelParty() {
      super(Category.Misc, "AutoPixelParty", "Automatically goes to pixel party block (hotbar slot 5)");
   }

   @Override
   public void onEnable() {
      this.pathStopGeneration++;
      this.pathStopDeadline = -1L;
      super.onEnable();
   }

   @Override
   public void onDisable() {
      short s = 10539;
      this.performActionAlt();
      long l = 3855070387770215448L;
      this.targetBlock = null;
      int n = ++this.pathStopGeneration;
      CompletableFuture.delayedExecutor(3000L, TimeUnit.MILLISECONDS).execute(() -> mc2.execute(() -> {
         if (this.pathStopGeneration == n) {
            AIWalkController.performActionSafe();
         }
      }));
      super.onDisable();
   }

   @EventTarget
   public void onClientWorld(ClientWorldEvent clientWorldEvent) {
      this.performActionAlt();
      this.targetBlock = null;
      this.pathStopDeadline = -1L;
      this.pathStopGeneration++;
   }

   private void performActionAlt() {
      this.activeSearch = null;
      if (this.searchFuture != null) {
         this.searchFuture.cancel(true);
         this.searchFuture = null;
      }

      this.hasTarget = false;
   }

   private void performActionImpl() {
      if (this.pathStopDeadline >= 0L) {
         this.pathStopDeadline = -1L;
         this.pathStopGeneration++;
      }
   }

   private void performActionDirect() {
      if (this.pathStopDeadline >= 0L && System.currentTimeMillis() >= this.pathStopDeadline) {
         this.pathStopDeadline = -1L;
         AIWalkController.performActionSafe();
      }
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      if (stateEvent.getState() == Stage.PRE) {
         this.performActionDirect();
         if (mc2.player != null && mc2.world != null) {
            int n = ((Number)this.hotbarIndex.getValue()).intValue() - 1;
            ItemStack stack = n >= 0 && n < 9 ? mc2.player.getInventory().getStack(n) : ItemStack.EMPTY;
            if (stack.isEmpty()) {
               if (this.pathStopDeadline < 0L) {
                  this.pathStopDeadline = System.currentTimeMillis() + 3000L;
               }

               this.performActionAlt();
               this.targetBlock = null;
            } else {
               this.performActionImpl();
               if (stack.getItem() instanceof BlockItem blockItem) {
                  Block block = blockItem.getBlock();
                  if (block != this.targetBlock) {
                     this.performActionAlt();
                     this.targetBlock = block;
                  }

                  if (!this.hasTarget && this.activeSearch == null) {
                     AutoPixelParty.RunnableImpl runnableImpl = new AutoPixelParty.RunnableImpl(
                        mc2.world, mc2.player.getPos(), block, ((Number)this.searchChunkRadius.getValue()).intValue()
                     );
                     this.activeSearch = runnableImpl;
                     this.searchFuture = this.searchExecutor.submit(runnableImpl);
                  }
               } else {
                  this.performActionAlt();
                  this.targetBlock = null;
               }
            }
         } else {
            this.performActionAlt();
            this.targetBlock = null;
         }
      }
   }

   public void performAction(AutoPixelParty.RunnableImpl runnableImpl, BlockPos pos) {
      if (this.activeSearch == runnableImpl && this.isEnabled() && mc2.world == runnableImpl.world) {
         this.activeSearch = null;
         this.searchFuture = null;
         if (pos == null) {
            this.applyText("未在附近找到该方块");
            this.hasTarget = false;
         } else {
            this.applyText("找到方块, 前往: " + pos.toShortString());
            AIWalkController.walkTo(pos);
            this.hasTarget = true;
         }
      }
   }

   public void performAction(AutoPixelParty.RunnableImpl runnableImpl, Exception e) {
      if (this.activeSearch == runnableImpl && this.isEnabled()) {
         this.activeSearch = null;
         this.searchFuture = null;
         this.hasTarget = false;
         this.applyText("搜索时发生错误: " + e.getMessage());
      }
   }

   private void applyText(String text) {
      if (mc2.player != null) {
         mc2.player.sendMessage(Text.literal("[AutoPixelParty] " + text), false);
      }
   }

   private final class RunnableImpl implements Runnable {
      @SerializedName("world")
      public final ClientWorld world;
      @SerializedName("origin")
      private final Vec3d origin;
      @SerializedName("block")
      private final Block block;
      @SerializedName("radius")
      private final int radius;
      @SerializedName("minY")
      private final int minY;
      @SerializedName("maxY")
      private final int maxY;
      @SerializedName("cursor")
      private final Mutable cursor = new Mutable();
      @SerializedName("closest")
      private BlockPos closest;
      @SerializedName("closestDistanceSquared")
      private double closestDistanceSquared = Double.MAX_VALUE;
      private static final long DEFAULT_LONG = 8L;

      public RunnableImpl(ClientWorld world2, Vec3d vec, Block block2, int n) {
         this.world = world2;
         this.origin = vec;
         this.block = block2;
         this.radius = n;
         int y = BlockPos.ofFloored(vec).getY();
         this.minY = Math.max(world2.getBottomY(), y - 10);
         this.maxY = Math.min(world2.getBottomY() + world2.getHeight() - 1, y);
      }

      private boolean isValid() {
         return AutoPixelParty.this.activeSearch == this && !Thread.currentThread().isInterrupted() && AutoPixelParty.mc2.world == this.world;
      }

      @Override
      public void run() {
         try {
            BlockPos pos = BlockPos.ofFloored(this.origin);
            int n = pos.getX() >> 4;
            int n2 = pos.getZ() >> 4;

            for (int i = 0; i <= this.radius && this.isValid(); i++) {
               for (int j = n - i; j <= n + i && this.isValid(); j++) {
                  for (int k = n2 - i; k <= n2 + i && this.isValid(); k++) {
                     if (i <= 0 || Math.abs(j - n) >= i || Math.abs(k - n2) >= i) {
                        Chunk chunk = this.world.getChunk(j, k, ChunkStatus.FULL, false);
                        if (chunk != null) {
                           this.performAction(chunk, j << 4, k << 4);
                           Thread.sleep(DEFAULT_LONG);
                        }
                     }
                  }
               }
            }

            if (this.isValid()) {
               BlockPos closest2 = this.closest;
               AutoPixelParty.mc2.execute(() -> AutoPixelParty.this.performAction(this, closest2));
            }
         } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
         } catch (Exception e2) {
            AutoPixelParty.mc2.execute(() -> AutoPixelParty.this.performAction(this, e2));
         }
      }

      private void performAction(Chunk chunk, int n, int n2) {
         for (int i = 0; i < 16 && this.isValid(); i++) {
            for (int j = 0; j < 16 && this.isValid(); j++) {
               int n3 = n + i;
               int n4 = n2 + j;
               double d = n3 + 0.5 - this.origin.x;
               double d2 = n4 + 0.5 - this.origin.z;
               if (this.closest == null || !(d * d + d2 * d2 > this.closestDistanceSquared)) {
                  for (int k = this.minY; k <= this.maxY && this.isValid(); k++) {
                     this.cursor.set(n3, k, n4);
                     if (chunk.getBlockState(this.cursor).getBlock() == this.block) {
                        double dist = this.origin.squaredDistanceTo(this.cursor.toCenterPos());
                        if (this.closest == null || dist < this.closestDistanceSquared) {
                           this.closest = this.cursor.toImmutable();
                           this.closestDistanceSquared = dist;
                        }
                        break;
                     }
                  }
               }
            }
         }
      }
   }
}
