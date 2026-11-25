package dev.southside.module.impl.render;

import cc.polyfrost.oneconfig.config.core.OneColor;
import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.events.CameraEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.ColorValue;
import dev.southside.utils.BoxMeshRenderer;
import dev.southside.utils.NumberValue;
import dev.southside.utils.ModeValue;
import java.awt.Color;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos.Mutable;

public class ItemHighlight extends Module {
   @SerializedName("mode")
   public final ModeValue mode = new ModeValue("Mode", "Both", "Sides", "Lines", "Both");
   @SerializedName("colorSides")
   public final ColorValue colorSides = new ColorValue("Color Sides", new OneColor(43, 248, 54, 45));
   @SerializedName("colorLine")
   public final ColorValue colorLine = new ColorValue("Color Line", new OneColor(43, 248, 54, 90));
   @SerializedName("radius")
   public final NumberValue radius = new NumberValue("Radius", 32, 8, 64, 1);

   public ItemHighlight() {
      super(Category.Render, "ItemHighlight");
   }

   @EventTarget
   public void onCamera(CameraEvent cameraEvent) {
      if (mc2.player != null && mc2.world != null && mc2.player.getMainHandStack().getItem() instanceof BlockItem blockItem) {
         Block block = blockItem.getBlock();
         BlockPos blockPos = mc2.player.getBlockPos();
         int n = ((Number)this.radius.getValue()).intValue();
         String displayTextAlt = this.mode.getDisplayTextAlt();
         Color color = "Lines".equals(displayTextAlt) ? null : ((OneColor)this.colorSides.getValue()).blendColor();
         Color color2 = "Sides".equals(displayTextAlt) ? null : ((OneColor)this.colorLine.getValue()).blendColor();
         Mutable mutablePos = new Mutable();

         for (int i = blockPos.getX() - n; i <= blockPos.getX() + n; i++) {
            for (int j = blockPos.getY() - n; j <= blockPos.getY() + n; j++) {
               for (int k = blockPos.getZ() - n; k <= blockPos.getZ() + n; k++) {
                  mutablePos.set(i, j, k);
                  if (mc2.world.getBlockState(mutablePos).getBlock() == block) {
                     Box box = new Box(mutablePos);
                     if (color != null) {
                        BoxMeshRenderer.renderBoxEx(box, color);
                     }

                     if (color2 != null) {
                        BoxMeshRenderer.renderBox(box, color2);
                     }
                  }
               }
            }
         }
      }
   }
}
