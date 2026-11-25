package dev.southside.render;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.SmoothEasing;
import dev.southside.utils.OneConfigFont;
import dev.southside.utils.OneConfigFonts;
import dev.southside.utils.AssetHelper;
import dev.southside.utils.NanoVGHelper;
import dev.southside.utils.IMinecraft;
import dev.southside.utils.NanoVGImage;
import dev.southside.utils.render.jello.animation.Easing;
import java.awt.Color;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;

public class ArrayListRenderer implements IMinecraft {
   private static final String SHADOW = "/assets/southside/textures/gui/hud/arraylist/shadow.png";
   public static OneConfigFont oneConfigFont = OneConfigFonts.REGULAR;
   @SerializedName("entries")
   private final List<ArrayListRenderer.Inner> entries = new ArrayList<>();

   public ArrayListRenderer() {
      for (Module module : Southside.INSTANCE.getModuleManager().getModules()) {
         if (module.getCategory() != Category.Render) {
            this.entries.add(new ArrayListRenderer.Inner(module, this));
         }
      }
   }

   public float getProgress() {
      return this.entries.isEmpty() ? 0.0F : this.entries.getFirst().getTextWidth() / (float)mc.getWindow().getScaleFactor();
   }

   public float getProgressEx() {
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;
      float[] floats = new float[1];
      nvg.performAction(true, l -> floats[0] = nvg.getTextHeight(l, 20.0F, oneConfigFont));
      return ((float)this.entries.stream().filter(inner -> inner.module.isEnabled()).count() * (floats[0] + 5.0F) + 10.0F)
         / (float)mc.getWindow().getScaleFactor();
   }

   public void performAction(OneConfigFont font) {
      oneConfigFont = font;
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;
      float[] floats = new float[1];
      nvg.performAction(true, l -> {
         for (ArrayListRenderer.Inner inner : this.entries) {
            inner.textWidth = nvg.getTextWidth(l, inner.module.getName(), 20.0F, font);
         }

         this.entries.sort((inner2, inner3) -> Float.compare(inner3.textWidth, inner2.textWidth));
         floats[0] = nvg.getTextHeight(l, 20.0F, font);
      });
      float f = (float)((mc.getWindow().getScaledWidth() - this.getProgress()) * mc.getWindow().getScaleFactor() - 10.0);
      float f2 = 0.0F;

      for (ArrayListRenderer.Inner inner4 : this.entries) {
         boolean isEnabled = inner4.module.isEnabled();
         inner4.performAction(isEnabled, f, 20.0F, f2);
         if (isEnabled) {
            f2 += floats[0] + 5.0F;
         }
      }

      for (ArrayListRenderer.Inner inner5 : this.entries) {
         if (inner5.module.isEnabled() || !inner5.fadeAnim.isFinished()) {
            nvg.performAction(true, l2 -> inner5.performAction(nvg, l2, floats[0]));
         }
      }
   }

   public static void draw(long vg, float x, float y, float width, float height, float f) {
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;
      AssetHelper assetHelper = AssetHelper.INSTANCE;
      AtomicBoolean atomicBoolean = new AtomicBoolean();
      nvg.performAction(l -> atomicBoolean.set(assetHelper.isValid(l, "/assets/southside/textures/gui/hud/arraylist/shadow.png", ArrayListRenderer.class)));
      if (atomicBoolean.get()) {
         NanoVGImage nanoVGImage = assetHelper.get("/assets/southside/textures/gui/hud/arraylist/shadow.png");
         drawImage(vg, nanoVGImage, x, y, 0.0F, 0.0F, width, height, nanoVGImage.getWidth(), nanoVGImage.getHeight(), f);
      }
   }

   private static void drawImage(
      long l, NanoVGImage nanoVGImage, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9
   ) {
      float width = nanoVGImage.getWidth();
      float height = nanoVGImage.getHeight();
      float f10 = f3 / width;
      float f11 = f4 / height;
      float f12 = (f3 + f7) / width - f10;
      float f13 = (f4 + f8) / height - f11;
      NVGPaint paint = NVGPaint.calloc();

      try {
         NanoVG.nvgImagePattern(l, f, f2, f5, f6, 0.0F, nanoVGImage.getImage(), f9, paint);
         FloatBuffer buffer = paint.xform();
         buffer.put(0, f12 * buffer.get(0));
         buffer.put(1, f13 * buffer.get(1));
         buffer.put(2, f12 * buffer.get(2));
         buffer.put(3, f13 * buffer.get(3));
         buffer.put(4, f12 * buffer.get(4) + f10);
         buffer.put(5, f13 * buffer.get(5) + f11);
         NanoVG.nvgBeginPath(l);
         NanoVG.nvgRect(l, f, f2, f5, f6);
         NanoVG.nvgFillPaint(l, paint);
         NanoVG.nvgFill(l);
      } catch (Throwable e) {
         if (paint != null) {
            try {
               paint.close();
            } catch (Throwable e2) {
               e.addSuppressed(e2);
            }
         }

         throw e;
      }

      if (paint != null) {
         paint.close();
      }
   }

   public static class Inner {
      @SerializedName("module")
      public final Module module;
      @SerializedName("owner")
      private final ArrayListRenderer owner;
      @SerializedName("fadeAnim")
      public final SmoothEasing fadeAnim = new SmoothEasing(Easing.EASE_OUT_EXPO, 500L);
      @SerializedName("slideAnim")
      private final SmoothEasing slideAnim = new SmoothEasing(Easing.EASE_OUT_EXPO, 500L);
      @SerializedName("entryX")
      private float entryX;
      @SerializedName("entryY")
      private float entryY;
      @SerializedName("textWidth")
      public float textWidth;

      public Inner(Module module2, ArrayListRenderer arrayListRenderer) {
         this.module = module2;
         this.owner = arrayListRenderer;
      }

      public Module getModule() {
         return this.module;
      }

      public float getTextWidth() {
         return this.textWidth;
      }

      public void performAction(boolean flag, float f, float f2, float f3) {
         this.entryX = f;
         this.entryY = f2;
         this.fadeAnim.computeDouble(flag ? 1.0 : 0.0);
         this.slideAnim.computeDouble(f3);
      }

      public void performAction(NanoVGHelper nvg, long l, float f) {
         float f2 = this.entryX + this.owner.getProgress() * (float)IMinecraft.mc.getWindow().getScaleFactor() - this.textWidth;
         float f3 = f2 + this.textWidth / 2.0F;
         float f4 = this.entryY + (float)this.slideAnim.getCurrentValue();
         float f5 = f4 + f / 2.0F;
         if (this.module.isEnabled()) {
            ArrayListRenderer.draw(l, f2 - 24.0F, f4 - 24.0F, this.textWidth + 64.0F, f + 48.0F, 0.57F);
         }

         nvg.translate(l, f3, f5);
         nvg.scale(l, (float)this.fadeAnim.getCurrentValue(), (float)this.fadeAnim.getCurrentValue());
         nvg.translate(l, -f3, -f5);
         int rgb = new Color(1.0F, 1.0F, 1.0F, (float)this.fadeAnim.getCurrentValue() * 0.95F).getRGB();
         nvg.drawTextEx(l, this.module.getName(), f2, f4, rgb, 20.0F, ArrayListRenderer.oneConfigFont);
      }
   }
}
