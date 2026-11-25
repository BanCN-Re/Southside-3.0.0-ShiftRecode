package dev.southside.utils;

import cc.polyfrost.oneconfig.hud.Hud;
import dev.southside.render.UIMatrixStack;
import net.minecraft.client.gui.DrawContext;

public class RenderTestHud extends Hud {
   private static final long DEFAULT_LONG = 198742691490562303L;

   public RenderTestHud(float x, float y, float z) {
      super(x, y, 1, z);
   }

   @Override
   public void draw(DrawContext context, UIMatrixStack matrices, float f, float f2, float f3, boolean flag) {
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;
      nvg.performAction(true, l -> nvg.draw(l, f, f2, this.computeFloat(f3, flag), this.setScale(f3, flag), (int)DEFAULT_LONG, 40.0F));
   }

   @Override
   public float setScale(float f, boolean flag) {
      return 200.0F * f;
   }

   @Override
   public float computeFloat(float f, boolean flag) {
      return 400.0F * f;
   }
}
