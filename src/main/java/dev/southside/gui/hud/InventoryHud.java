package dev.southside.gui.hud;

import cc.polyfrost.oneconfig.hud.Hud;
import com.google.gson.annotations.SerializedName;
import dev.southside.module.impl.render.InventoryHUD;
import dev.southside.module.values.BooleanValue;
import dev.southside.render.UIMatrixStack;
import dev.southside.utils.NumberValue;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

public class InventoryHud extends Hud {
   private static final int DEFAULT_INT = 16;
   private static final int DEFAULT_INT_VALUE = 18;
   private static final int DEFAULT_INT_DATA = 4;
   private static final int DEFAULT_INT_REF = 12;
   private static final int DEFAULT_INT_PRIMARY = 9;
   private static final int DEFAULT_INT_SECONDARY = 3;
   private static final int DEFAULT_INT_TERTIARY = 168;
   private static final int DEFAULT_INT_EXTRA = 60;
   @SerializedName("background")
   public final BooleanValue background = new BooleanValue("Background", true);
   @SerializedName("backgroundAlpha")
   public final NumberValue backgroundAlpha = new NumberValue("Background Alpha", 120, 0, 255, 1);
   @SerializedName("showTitle")
   public final BooleanValue showTitle = new BooleanValue("Show Title", true);
   @SerializedName("bindToTab")
   public final BooleanValue bindToTab = new BooleanValue("Bind to Tab", false);

   @Override
   public void draw(DrawContext context, UIMatrixStack matrices, float f, float f2, float f3, boolean flag) {
      if (mc.player != null) {
         if (!flag && this.bindToTab != null && (Boolean)this.bindToTab.getValue()) {
            if (!mc.options.playerListKey.isPressed()) {
               return;
            }

            int n = (int)(168.0F * f3);
            f = (mc.getWindow().getScaledWidth() - n) / 2.0F;
            f2 = mc.getWindow().getScaledHeight() / 2.0F + 10.0F;
         }

         this.drawInventory(context, f, f2, f3);
      }
   }

   @Override
   public float computeFloat(float f, boolean flag) {
      return 168.0F * f;
   }

   @Override
   public float setScale(float f, boolean flag) {
      int n = !InventoryHUD.style.matches("Simple") && this.isValidImpl() ? 12 : 0;
      return (60 + n) * f;
   }

   private boolean isValidImpl() {
      return this.showTitle != null && (Boolean)this.showTitle.getValue();
   }

   private void drawInventory(DrawContext context, float f, float f2, float f3) {
      PlayerInventory inventory = mc.player.getInventory();
      boolean flag = InventoryHUD.style.matches("Simple");
      int n = !flag && this.isValidImpl() ? 12 : 0;
      int n2 = 60 + n;
      MatrixStack matrices = context.getMatrices();
      matrices.push();
      matrices.translate(f, f2, 0.0F);
      matrices.scale(f3, f3, 1.0F);
      if (flag) {
         context.fill(0, 0, 168, n2, -1275068416);
      } else {
         this.render(context, n2);
      }

      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 9; j++) {
            int n3 = 9 + i * 9 + j;
            ItemStack stack = inventory.getStack(n3);
            int n4 = 4 + j * 18;
            int n5 = 4 + n + i * 18;
            context.fill(n4, n5, n4 + 16, n5 + 16, 1073741824);
            if (!stack.isEmpty()) {
               context.drawItem(stack, n4, n5);
               context.drawStackOverlay(mc.textRenderer, stack, n4, n5);
            }
         }
      }

      matrices.pop();
   }

   private void render(DrawContext context, int n) {
      if (this.background != null && (Boolean)this.background.getValue()) {
         int n2 = this.backgroundAlpha != null ? ((Number)this.backgroundAlpha.getValue()).intValue() : 120;
         context.fill(0, 0, 168, n, n2 << 24 | 1315860);
         if (this.isValidImpl()) {
            int n3 = Math.min(n2 + 30, 255);
            context.fill(0, 0, 168, 12, n3 << 24 | 1973790);
         }
      }

      if (this.isValidImpl()) {
         context.drawText(mc.textRenderer, "Inventory", 4, 2, -1, false);
      }
   }
}
