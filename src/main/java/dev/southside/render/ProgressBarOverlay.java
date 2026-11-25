package dev.southside.render;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.events.DrawContextEvent;
import dev.southside.module.impl.client.ClientSettings;
import dev.southside.nanovg.NanoFontRenderer;
import dev.southside.utils.NanoFontManager;
import dev.southside.utils.IMinecraft;
import dev.southside.utils.ModeValue;
import dev.southside.utils.SmoothFloat;
import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;

public class ProgressBarOverlay implements IMinecraft {
   private static final float DEFAULT_FLOAT = 200.0F;
   private static final float DEFAULT_FLOAT_VALUE = 10.0F;
   private static final float DEFAULT_FLOAT_DATA = 4.0F;
   private static final float DEFAULT_FLOAT_REF = 13.0F;
   private static final float DEFAULT_FLOAT_PRIMARY = 11.0F;
   private static final float DEFAULT_FLOAT_SECONDARY = 20.0F;
   private static final float DEFAULT_FLOAT_TERTIARY = 8.0F;
   private static final int DEFAULT_INT = 120;
   private static final int DEFAULT_INT_VALUE = 2;
   private static final int DEFAULT_INT_DATA = 20;
   private static final long DEFAULT_LONG = 300L;
   private static final long DEFAULT_LONG_VALUE = 800L;
   private static final long DEFAULT_LONG_DATA = 400L;
   private static final Color DEFAULT_COLOR = new Color(0, 0, 0, 180);
   private static final Color DEFAULT_COLOR_VALUE = new Color(255, 255, 255, 30);
   private static final Color DEFAULT_COLOR_DATA = new Color(255, 255, 255, 220);
   private static final Color DEFAULT_COLOR_REF = new Color(255, 255, 255, 240);
   private static final Color DEFAULT_COLOR_PRIMARY = new Color(180, 180, 180);
   @SerializedName("tasks")
   private final LinkedHashMap<Long, ProgressBarOverlay.Inner2> tasks = new LinkedHashMap<>();
   @SerializedName("listHeightAnimation")
   private final SmoothFloat listHeightAnimation = new SmoothFloat(4.0F, 0.0F);
   @SerializedName("globalAlphaAnimation")
   private final SmoothFloat globalAlphaAnimation = new SmoothFloat(5.0F, 0.0F);
   @SerializedName("nextTaskId")
   private long nextTaskId;
   @SerializedName("hadActiveTasks")
   private boolean hadActiveTasks;

   public synchronized ProgressBarOverlay.Inner getInner(int n, @Nullable String text) {
      ProgressBarOverlay.Inner2 inner2 = new ProgressBarOverlay.Inner2(++this.nextTaskId, Math.max(1, n), text);
      this.tasks.put(inner2.id, inner2);
      return new ProgressBarOverlay.Inner(inner2.id);
   }

   public ProgressBarOverlay.Inner getInner(int n) {
      return this.getInner(n, null);
   }

   public synchronized void clear() {
      this.tasks.clear();
   }

   public synchronized boolean isValid() {
      this.performAction();
      return !this.tasks.isEmpty();
   }

   private synchronized void performAction() {
      if (!this.tasks.isEmpty()) {
         long now = System.currentTimeMillis();
         this.tasks.values().removeIf(inner2 -> inner2.finishTime > 0L && now - inner2.finishTime >= 1200L);
      }
   }

   public synchronized void remove(long l) {
      this.tasks.remove(l);
   }

   public synchronized void performAction(long l) {
      ProgressBarOverlay.Inner2 inner2 = this.tasks.get(l);
      if (inner2 != null) {
         this.performAction(l, inner2.currentStep + 1);
      }
   }

   public synchronized void performAction(long l, int n) {
      ProgressBarOverlay.Inner2 inner2 = this.tasks.get(l);
      if (inner2 != null) {
         inner2.currentStep = MathHelper.clamp(n, 0, inner2.totalSteps);
         inner2.finishTime = inner2.currentStep >= inner2.totalSteps ? System.currentTimeMillis() : -1L;
      }
   }

   public synchronized void performActionEx(long l) {
      ProgressBarOverlay.Inner2 inner2 = this.tasks.get(l);
      if (inner2 != null) {
         inner2.currentStep = inner2.totalSteps;
         inner2.finishTime = System.currentTimeMillis();
      }
   }

   public synchronized boolean isValid(long l) {
      this.performAction();
      return this.tasks.containsKey(l);
   }

   private synchronized List<ProgressBarOverlay.Entry> collectItems() {
      this.performAction();
      ArrayList list = new ArrayList();
      int n = 0;
      long now = System.currentTimeMillis();

      for (ProgressBarOverlay.Inner2 inner2 : this.tasks.values()) {
         float f = 1.0F;
         long l = now - inner2.createdTime;
         if (l < 300L) {
            f = MathHelper.clamp((float)l / 300.0F, 0.0F, 1.0F);
         }

         if (inner2.finishTime > 0L) {
            long l2 = now - inner2.finishTime;
            if (l2 > 800L) {
               f = MathHelper.clamp(1.0F - (float)(l2 - 800L) / 400.0F, 0.0F, 1.0F);
            }
         }

         list.add(new ProgressBarOverlay.Entry(inner2.id, inner2.totalSteps, inner2.currentStep, inner2.label, inner2.text, inner2.hasLabel, n, f));
         n++;
      }

      return list;
   }

   @EventTarget
   public void onDrawContext(DrawContextEvent drawContextEvent) {
      List list = this.collectItems();
      boolean flag = !list.isEmpty();
      if (flag && !this.hadActiveTasks) {
         this.globalAlphaAnimation.setTarget(1.0F);
      } else if (!flag && this.hadActiveTasks) {
         this.globalAlphaAnimation.setTarget(0.0F);
      }

      this.hadActiveTasks = flag;
      float f = this.globalAlphaAnimation.getValue();
      if (!(f <= 0.01F) || flag) {
         ModeValue progressBarStyle = ClientSettings.progressBarStyle;
         if (progressBarStyle.options[progressBarStyle.getValue()].equalsIgnoreCase("Classic")) {
            this.render(drawContextEvent.getDrawContext(), list, f);
         } else {
            this.performAction(list, f);
         }
      }
   }

   private void render(DrawContext context, List<ProgressBarOverlay.Entry> list, float f) {
      TextRenderer textRenderer = mc.textRenderer;
      int scaledWidth = mc.getWindow().getScaledWidth();
      int scaledHeight = mc.getWindow().getScaledHeight();

      for (ProgressBarOverlay.Entry entry : list) {
         float f2 = f * entry.alpha;
         if (!(f2 <= 0.01F)) {
            int n = entry.hasLabel ? textRenderer.getWidth(entry.text) : 0;
            int n2 = Math.max(120, n);
            int n3 = (scaledWidth - n2) / 2;
            int n4 = scaledHeight / 2 + ((Number)ClientSettings.progressBarYOffset.getValue()).intValue() + entry.index * 20;
            float progress = entry.getProgress();
            String text = (int)(progress * 100.0F) + "%";
            int width = textRenderer.getWidth(text);
            int n5 = n4 - 12;
            if (entry.hasLabel) {
               context.drawText(textRenderer, entry.text, n3, n5, computeInt(-1, f2), false);
            }

            context.drawText(textRenderer, Text.literal(text), n3 + n2 - width, n5, computeInt(-3355444, f2), false);
            render(context, progress, n3, n4, n2, f2);
         }
      }
   }

   private static void render(DrawContext context, float f, int n, int n2, int n3, float f2) {
      int n4 = (int)Math.ceil(n3 * f);
      int n5 = computeInt(1627389951, f2);
      int n6 = computeInt(-11552513, f2);
      context.fill(n - 1, n2 - 1, n + n3 + 1, n2, n5);
      context.fill(n - 1, n2 + 2, n + n3 + 1, n2 + 2 + 1, n5);
      context.fill(n - 1, n2, n, n2 + 2, n5);
      context.fill(n + n3, n2, n + n3 + 1, n2 + 2, n5);
      if (n4 > 0) {
         context.fill(n, n2, n + n4, n2 + 2, n6);
      }
   }

   private void performAction(List<ProgressBarOverlay.Entry> list, float f) {
      int scaledWidth = mc.getWindow().getScaledWidth();
      int scaledHeight = mc.getWindow().getScaledHeight();
      int size = list.size();
      float f2 = size > 0 ? 10.0F + size * 20.0F + (size - 1) * 8.0F + 10.0F : 0.0F;
      this.listHeightAnimation.setTarget(f2);
      float f3 = this.listHeightAnimation.getValue();
      if (!(f3 < 1.0F)) {
         float f4 = (scaledWidth - 200.0F) / 2.0F;
         float f5 = ((Number)ClientSettings.progressBarYOffset.getValue()).floatValue();
         float f6 = scaledHeight / 2.0F + f5;
         GlStateStack.pushState();
         NanoVG.nvgSave(NanoVGContext.timestamp);
         NanoVG.nvgBeginFrame(
            NanoVGContext.timestamp, MinecraftClient.getInstance().getWindow().getWidth(), MinecraftClient.getInstance().getWindow().getHeight(), 1.0F
         );
         NanoVGTransform.scale(0.0F, 0.0F, (float)MinecraftClient.getInstance().getWindow().getScaleFactor());
         f6 += (1.0F - f) * 8.0F;
         drawDropShadow(f4, f6, f3, f);

         for (ProgressBarOverlay.Entry entry : list) {
            float f7 = f * entry.alpha;
            if (!(f7 <= 0.01F)) {
               float f8 = f6 + 10.0F + entry.index * 28.0F;
               performAction(entry, f4, f8, f7);
            }
         }

         NanoVGTransform.restore();
         NanoVG.nvgEndFrame(NanoVGContext.timestamp);
         NanoVG.nvgRestore(NanoVGContext.timestamp);
         GlStateStack.popState();
      }
   }

   private static void drawDropShadow(float x, float y, float width, float height) {
      float f = 8.0F;
      long timestamp = NanoVGContext.timestamp;
      NVGPaint paint = NVGPaint.calloc();

      try {
         NVGColor nvgColor = NVGColor.calloc();

         try {
            NVGColor nvgColor2 = NVGColor.calloc();

            try {
               performAction(nvgColor, new Color(0, 0, 0, (int)(40.0F * height)));
               performAction(nvgColor2, new Color(0, 0, 0, 0));
               NanoVG.nvgBoxGradient(timestamp, x, y, 200.0F, width, 0.0F, f, nvgColor, nvgColor2, paint);
               NanoVG.nvgBeginPath(timestamp);
               NanoVG.nvgRect(timestamp, x - f, y - f, 200.0F + f * 2.0F, width + f * 2.0F);
               NanoVG.nvgRect(timestamp, x, y, 200.0F, width);
               NanoVG.nvgPathWinding(timestamp, 2);
               NanoVG.nvgFillPaint(timestamp, paint);
               NanoVG.nvgFill(timestamp);
            } catch (Throwable e) {
               if (nvgColor2 != null) {
                  try {
                     nvgColor2.close();
                  } catch (Throwable e2) {
                     e.addSuppressed(e2);
                  }
               }

               throw e;
            }

            if (nvgColor2 != null) {
               nvgColor2.close();
            }
         } catch (Throwable e3) {
            if (nvgColor != null) {
               try {
                  nvgColor.close();
               } catch (Throwable e4) {
                  e3.addSuppressed(e4);
               }
            }

            throw e3;
         }

         if (nvgColor != null) {
            nvgColor.close();
         }
      } catch (Throwable e5) {
         if (paint != null) {
            try {
               paint.close();
            } catch (Throwable e6) {
               e5.addSuppressed(e6);
            }
         }

         throw e5;
      }

      if (paint != null) {
         paint.close();
      }

      drawRect(x, y, 200.0F, width, blendColor(DEFAULT_COLOR, height));
   }

   private static void performAction(ProgressBarOverlay.Entry entry, float f, float f2, float f3) {
      NanoFontRenderer nanoFontRenderer = NanoFontManager.nanoFontRenderer;
      float progress = entry.getProgress();
      int n = (int)(progress * 100.0F);
      float f4 = f + 10.0F;
      float f5 = 180.0F;
      if (entry.hasLabel) {
         nanoFontRenderer.applyTextEx(entry.label, f4, f2, 13.0F, blendColor(DEFAULT_COLOR_REF, f3));
      }

      String text = n + "%";
      float textWidth = nanoFontRenderer.getTextWidth(text, 11.0F);
      Color color = n >= 100 ? Color.WHITE : DEFAULT_COLOR_PRIMARY;
      nanoFontRenderer.applyTextEx(text, f4 + f5 - textWidth, f2 + 1.0F, 11.0F, blendColor(color, f3));
      float f6 = f2 + 20.0F - 4.0F;
      drawRect(f4, f6, f5, 4.0F, blendColor(DEFAULT_COLOR_VALUE, f3));
      float f7 = f5 * progress;
      if (f7 > 0.5F) {
         drawRect(f4, f6, f7, 4.0F, blendColor(DEFAULT_COLOR_DATA, f3));
      }
   }

   private static void drawRect(float f, float f2, float f3, float f4, Color color) {
      long timestamp = NanoVGContext.timestamp;
      NanoVG.nvgBeginPath(timestamp);
      NanoVG.nvgRect(timestamp, f, f2, f3, f4);
      NVGColor nvgColor = NVGColor.calloc();

      try {
         performAction(nvgColor, color);
         NanoVG.nvgFillColor(timestamp, nvgColor);
      } catch (Throwable e) {
         if (nvgColor != null) {
            try {
               nvgColor.close();
            } catch (Throwable e2) {
               e.addSuppressed(e2);
            }
         }

         throw e;
      }

      if (nvgColor != null) {
         nvgColor.close();
      }

      NanoVG.nvgFill(timestamp);
      NanoVGTransform.closePath();
   }

   private static void performAction(NVGColor nvgColor, Color color) {
      nvgColor.r(color.getRed() / 255.0F).g(color.getGreen() / 255.0F).b(color.getBlue() / 255.0F).a(color.getAlpha() / 255.0F);
   }

   private static Color blendColor(Color color, float f) {
      int clamped = MathHelper.clamp((int)(color.getAlpha() * f), 0, 255);
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), clamped);
   }

   private static int computeInt(int n, float f) {
      int clamped = MathHelper.clamp((int)((n >> 24 & 0xFF) * f), 0, 255);
      return clamped << 24 | n & 16777215;
   }

   private record Entry(long id, int totalSteps, int currentStep, String label, Text text, boolean hasLabel, int index, float alpha) {

      public float getProgress() {
         float f = this.totalSteps <= 0 ? 0.0F : (float)this.currentStep / this.totalSteps;
         return MathHelper.clamp(f, 0.0F, 1.0F);
      }

      public long getId() {
         return this.id;
      }

      public int getTotalSteps() {
         return this.totalSteps;
      }

      public int getCurrentStep() {
         return this.currentStep;
      }

      public String getLabel() {
         return this.label;
      }

      public Text getText() {
         return this.text;
      }

      public boolean isHasLabel() {
         return this.hasLabel;
      }

      public int getIndex() {
         return this.index;
      }

      public float getAlpha() {
         return this.alpha;
      }
   }

   public final class Inner {
      @SerializedName("id")
      private final long id;

      public Inner(long l) {
         this.id = l;
      }

      public void performAction() {
         ProgressBarOverlay.this.performAction(this.id);
      }

      public void onKeyTyped(int n) {
         ProgressBarOverlay.this.performAction(this.id, n);
      }

      public void performActionEx() {
         ProgressBarOverlay.this.performActionEx(this.id);
      }

      public boolean isValid() {
         return ProgressBarOverlay.this.isValid(this.id);
      }

      public void performActionAlt() {
         ProgressBarOverlay.this.remove(this.id);
      }
   }

   private static final class Inner2 {
      @SerializedName("id")
      public final long id;
      @SerializedName("totalSteps")
      public final int totalSteps;
      @SerializedName("currentStep")
      public int currentStep;
      @SerializedName("label")
      public final String label;
      @SerializedName("text")
      public final Text text;
      @SerializedName("hasLabel")
      public final boolean hasLabel;
      @SerializedName("createdTime")
      public final long createdTime;
      @SerializedName("finishTime")
      public long finishTime;
      private static final long DEFAULT_LONG = -1L;

      public Inner2(long l, int n, @Nullable String text2) {
         this.id = l;
         this.totalSteps = n;
         this.currentStep = 0;
         this.createdTime = System.currentTimeMillis();
         this.finishTime = DEFAULT_LONG;
         if (text2 != null && !text2.isBlank()) {
            this.label = text2.trim();
            this.text = Text.literal(this.label);
            this.hasLabel = true;
         } else {
            this.label = "";
            this.text = Text.empty();
            this.hasLabel = false;
         }
      }
   }
}
