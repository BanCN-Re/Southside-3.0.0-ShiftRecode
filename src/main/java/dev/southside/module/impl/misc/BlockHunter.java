package dev.southside.module.impl.misc;

import cc.polyfrost.oneconfig.config.core.OneColor;
import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.TransferOrigin;
import dev.southside.event.events.CameraEvent;
import dev.southside.event.events.ClientWorldEvent;
import dev.southside.event.events.DirectionEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.ColorValue;
import dev.southside.utils.BoxMeshRenderer;
import dev.southside.utils.IText;
import java.awt.Color;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class BlockHunter extends Module {
   @SerializedName("sideColor")
   public final ColorValue sideColor = new ColorValue("Side Color", true, new OneColor(255, 120, 40, 60));
   @SerializedName("lineColor")
   public final ColorValue lineColor = new ColorValue("Line Color", true, new OneColor(255, 120, 40, 160));
   @SerializedName("hiddenBlocks")
   private final Set<BlockPos> hiddenBlocks = ConcurrentHashMap.newKeySet();

   public BlockHunter() {
      super(Category.Misc, "BlockHunter");
   }

   private void markIfAir(BlockPos pos, BlockState state) {
      if (pos != null && state != null && mc2.world != null) {
         if (state.isAir()) {
            this.hiddenBlocks.remove(pos);
         } else if (mc2.world.getBlockState(pos).isAir()) {
            BlockPos pos2 = pos.toImmutable();
            if (this.hiddenBlocks.add(pos2)) {
               mc2.execute(() -> {
                  short s = 25136;
                  int x = pos2.getX();
                  int z = pos2.getZ();
                  int y = pos2.getY();
                  int n = x;
                  short s2 = 345;
                  IText.applyText("Detected a hider! (" + n + ", " + y + ", " + z + ")");
               });
            }
         }
      }
   }

   @EventTarget
   public void onDirection(DirectionEvent directionEvent) {
      if (directionEvent.getDirection() == TransferOrigin.RECEIVE && mc2.world != null) {
         if (directionEvent.getPacket() instanceof BlockUpdateS2CPacket blockUpdateS2CPacket) {
            this.markIfAir(blockUpdateS2CPacket.getPos(), blockUpdateS2CPacket.getState());
         } else if (directionEvent.getPacket() instanceof ChunkDeltaUpdateS2CPacket chunkDeltaUpdateS2CPacket) {
            chunkDeltaUpdateS2CPacket.visitUpdates(this::markIfAir);
         }
      }
   }

   @EventTarget
   public void onCamera(CameraEvent cameraEvent) {
      if (mc2.world != null && !this.hiddenBlocks.isEmpty()) {
         Color color = ((OneColor)this.sideColor.getValue()).blendColor();
         Color color2 = ((OneColor)this.lineColor.getValue()).blendColor();

         for (BlockPos pos : this.hiddenBlocks) {
            Box box = new Box(pos);
            BoxMeshRenderer.renderBoxEx(box, color);
            BoxMeshRenderer.renderBox(box, color2);
         }
      }
   }

   @Override
   public void onDisable() {
      this.hiddenBlocks.clear();
      super.onDisable();
   }

   @EventTarget
   public void onClientWorld(ClientWorldEvent clientWorldEvent) {
      this.hiddenBlocks.clear();
   }
}
