package dev.southside.module.impl.render;

import cc.polyfrost.oneconfig.config.core.OneColor;
import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.events.CameraEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.ColorValue;
import dev.southside.utils.WorldBlockEntityUtils;
import dev.southside.utils.BoxMeshRenderer;
import dev.southside.utils.NumberValue;
import dev.southside.utils.ModeValue;
import dev.southside.utils.ChestTracker;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class ChestESP extends Module {
   @SerializedName("mode")
   public final ModeValue mode = new ModeValue("Mode", "Both", "Sides", "Lines", "Both");
   @SerializedName("range")
   public final NumberValue range = new NumberValue("Range", 64.0, 30.0, 256.0, 1.0);
   @SerializedName("unOpenedColorSides")
   public final ColorValue unOpenedColorSides = new ColorValue("UnOpened Color Sides", new OneColor(43, 248, 54, 45));
   @SerializedName("openedColorSides")
   public final ColorValue openedColorSides = new ColorValue("Opened Color Sides", new OneColor(248, 48, 34, 45));
   @SerializedName("unOpenedColorLine")
   public final ColorValue unOpenedColorLine = new ColorValue("UnOpened Color Line", new OneColor(43, 248, 54, 90));
   @SerializedName("openedColorLine")
   public final ColorValue openedColorLine = new ColorValue("Opened Color Line", new OneColor(248, 48, 34, 90));
   @SerializedName("selfStolenColorSides")
   public final ColorValue selfStolenColorSides = new ColorValue("Self Stolen Color Sides", new OneColor(210, 80, 100, 45));
   @SerializedName("selfStolenColorLine")
   public final ColorValue selfStolenColorLine = new ColorValue("Self Stolen Color Line", new OneColor(210, 80, 100, 90));

   public ChestESP() {
      super(Category.Render, "ChestESP", "视奸附近的箱子");
   }

   @Override
   public void postInit() {
      super.postInit();
      this.hideIf(this.unOpenedColorLine.getLabel(), () -> this.mode.matches("Sides"));
      this.hideIf(this.openedColorLine.getLabel(), () -> this.mode.matches("Sides"));
   }

   @EventTarget
   public void onCamera(CameraEvent cameraEvent) {
      if (mc2.world != null && mc2.player != null) {
         double d = ((Number)this.range.getValue()).doubleValue();
         double d2 = d * d;
         boolean flag = this.mode.matches("Lines") || this.mode.matches("Both");
         boolean flag2 = this.mode.matches("Sides") || this.mode.matches("Both");
         ChestTracker chestTracker = Southside.INSTANCE.getChestTracker();

         for (BlockEntity blockEntity : WorldBlockEntityUtils.getIterable()) {
            if (blockEntity instanceof ChestBlockEntity) {
               BlockPos pos = blockEntity.getPos();
               if (!(mc2.player.getPos().squaredDistanceTo(pos.toCenterPos()) > d2)) {
                  ColorValue colorValue;
                  ColorValue colorValue2;
                  if (!chestTracker.chestPositions.contains(pos)) {
                     colorValue = this.unOpenedColorLine;
                     colorValue2 = this.unOpenedColorSides;
                  } else if (chestTracker.openedChestHistory.contains(pos)) {
                     colorValue = this.selfStolenColorLine;
                     colorValue2 = this.selfStolenColorSides;
                  } else {
                     colorValue = this.openedColorLine;
                     colorValue2 = this.openedColorSides;
                  }

                  Box box = new Box(pos);
                  if (flag) {
                     BoxMeshRenderer.renderBox(box, ((OneColor)colorValue.getValue()).blendColor());
                  }

                  if (flag2) {
                     BoxMeshRenderer.renderBoxEx(box, ((OneColor)colorValue2.getValue()).blendColor());
                  }
               }
            }
         }
      }
   }
}
