package dev.southside.gui.hud;

import cc.polyfrost.oneconfig.hud.Hud;
import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.module.impl.client.Theme;
import dev.southside.module.impl.client.Watermark;
import dev.southside.module.values.BooleanValue;
import dev.southside.nanovg.NanoFontRenderer;
import dev.southside.render.UIMatrixStack;
import dev.southside.render.NanoVGTransform;
import dev.southside.render.SquirclePath;
import dev.southside.render.NanoVGContext;
import dev.southside.utils.OneConfigSvgs;
import dev.southside.utils.OneConfigMultithreading;
import dev.southside.utils.NanoFontManager;
import dev.southside.utils.OneConfigFonts;
import dev.southside.utils.HttpClientUtils;
import dev.southside.utils.AssetHelper;
import dev.southside.utils.NanoVGHelper;
import dev.southside.utils.ModeValue;
import java.awt.Color;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;

public class WatermarkHud extends Hud {
   @SerializedName("subtitleContent")
   public final ModeValue subtitleContent = new ModeValue(
      "Subtitle Content", new String[]{"None", "FPS", "Player Role name", "Playtime", "Ping", "Hitokoto", "Auto Switch"}, "Choose the watermark subtitle", 6, 1
   );
   @SerializedName("backgroundGlow")
   public final BooleanValue backgroundGlow = new BooleanValue("Background Glow", false);
   @SerializedName("targetPanelWidth")
   private float targetPanelWidth;
   @SerializedName("animatedPanelWidth")
   private float animatedPanelWidth;
   @SerializedName("panelWidth")
   private float panelWidth;
   @SerializedName("panelHeight")
   private float panelHeight;
   @SerializedName("subtitleIndex")
   private int subtitleIndex = 1;
   @SerializedName("lastSubtitleSwitch")
   private long lastSubtitleSwitch;
   @SerializedName("subtitleFade")
   private float subtitleFade = 1.0F;
   @SerializedName("previousSubtitle")
   private String previousSubtitle = "";
   @SerializedName("fading")
   private boolean fading;
   @SerializedName("startTime")
   private final long startTime = System.currentTimeMillis();
   @SerializedName("hitokotoText")
   private volatile String hitokotoText = "曹家豪130185200910260034";
   @SerializedName("lastHitokotoFetch")
   private volatile long lastHitokotoFetch;
   @SerializedName("hitokotoFetching")
   private final AtomicBoolean hitokotoFetching = new AtomicBoolean();
   @SerializedName("watermarkModule")
   private Watermark watermarkModule;

   @Override
   public float setScale(float f, boolean flag) {
      if (this.watermarkModule == null) {
         return 50.0F * f;
      } else {
         String displayTextEx = this.watermarkModule.getStyle().getDisplayTextEx();
         if ("Old".equals(displayTextEx)) {
            return 28.0F * f + 16.0F * f;
         } else if ("SigmaJello".equals(displayTextEx)) {
            return 104.0F * f;
         } else if ("Southside".equals(displayTextEx)) {
            return (this.panelHeight > 0.0F ? this.panelHeight : 50.0F) * f;
         } else {
            return "Retro".equals(displayTextEx) ? 75.0F * f : this.panelHeight * f;
         }
      }
   }

   @Override
   public float computeFloat(float f, boolean flag) {
      if (this.watermarkModule == null) {
         return 200.0F * f;
      } else {
         String displayTextEx = this.watermarkModule.getStyle().getDisplayTextEx();
         if ("Old".equals(displayTextEx)) {
            return 350.0F * f + 16.0F * f;
         } else if ("Southside".equals(displayTextEx)) {
            float f2 = this.animatedPanelWidth > 0.0F ? this.animatedPanelWidth : this.targetPanelWidth;
            return (f2 <= 0.0F ? 200.0F : f2) * f;
         } else {
            return "Retro".equals(displayTextEx) ? Math.max(240.0F, this.panelWidth) * f : 200.0F * f;
         }
      }
   }

   @Override
   public void draw(DrawContext context, UIMatrixStack matrices, float f, float f2, float f3, boolean flag) {
      if (this.watermarkModule == null) {
         this.watermarkModule = Southside.INSTANCE.getModuleManager().getModule(Watermark.class);
      }

      if (mc.getNetworkHandler() != null) {
         NanoVGHelper nvg = NanoVGHelper.INSTANCE;
         nvg.performAction(true, l -> this.draw(nvg, l, f, f2, f3, flag));
      }
   }

   private void draw(NanoVGHelper nvg, long l, float f, float f2, float f3, boolean flag) {
      switch (this.watermarkModule.getStyle().getDisplayTextEx()) {
         case "Southside":
            this.performAction(nvg, l, f, f2, f3, flag);
            break;
         case "SigmaJello":
            this.performAction(nvg, l);
            break;
         case "Retro":
            this.performAction(nvg, l, f, f2, f3);
            break;
         default:
            this.leap(f, f2, f3);
      }
   }

   private String getDisplayText() {
      String text = (String)this.watermarkModule.clientName.getValue();
      String text2 = text != null && !text.isBlank() ? text.trim() : "Southside";
      String text3 = text2.replaceAll("[\\s_-]", "");
      return !text3.equalsIgnoreCase("Southside") && !text3.equalsIgnoreCase("SouthsideNextGen") && !text3.equalsIgnoreCase("SouthSideReborn")
         ? text2
         : "SouthSide";
   }

   private void performAction(NanoVGHelper nvg, long l, float f, float f2, float f3, boolean flag) {
      String displayText = this.getDisplayText();
      String displayTextEx = this.getDisplayTextEx();
      String text = displayTextEx.isEmpty() ? displayText + " Reborn" : displayText + " Reborn · " + displayTextEx;
      this.targetPanelWidth = nvg.getTextWidth(l, text, 10.0F, OneConfigFonts.SEMIBOLD) + 6.0F + 22.0F;
      this.panelHeight = nvg.getTextHeight(l, 10.0F, OneConfigFonts.SEMIBOLD) * 2.0F;
      this.animatedPanelWidth = this.animatedPanelWidth + (this.targetPanelWidth - this.animatedPanelWidth) * 0.15F;
      float f4 = this.computeFloat(f3, flag);
      float f5 = this.setScale(f3, flag);
      nvg.drawRoundedRect(l, f, f2, f4, f5, -1266581119, 5.0F * f3);
      if ((Boolean)this.backgroundGlow.getValue()) {
         nvg.drawRoundedRect(l, f, f2, f4, f5, 10.0F * f3, 0.0F, 5.0F * f3, new Color(167, 166, 166, 180));
      }

      nvg.draw(l, OneConfigSvgs.ICON_WHITE, f + 5.0F * f3, f2 + 2.0F * f3, 15.0F * f3, 15.0F * f3, -1, 3.0F);
      int rgb = new Color(255, 255, 255, (int)(255.0F * this.subtitleFade)).getRGB();
      nvg.drawTextEx(l, text, f + 22.0F * f3, f2 + 10.5F * f3, rgb, 10.0F * f3, OneConfigFonts.SEMIBOLD);
   }

   private void performAction(NanoVGHelper nvg, long l) {
      AssetHelper assetHelper = AssetHelper.INSTANCE;
      if (assetHelper.isValid(l, "/assets/southside/textures/gui/hud/watermark/watermark.png", Watermark.class)) {
         nvg.drawImage(l, "/assets/southside/textures/gui/hud/watermark/watermark.png", 0.0F, 0.0F, 170.0F, 104.0F, Watermark.class);
      }
   }

   private void performAction(NanoVGHelper nvg, long l, float f, float f2, float f3) {
      String displayText = this.getDisplayText();
      float textWidth = nvg.getTextWidth(l, displayText, 25.0F * f3, OneConfigFonts.SEMIBOLD);
      float f4 = Math.max(240.0F * f3, 89.0F * f3 + textWidth);
      float f5 = 75.0F * f3;
      this.panelWidth = f4 / f3;
      this.panelHeight = f5 / f3;
      float f6 = 12.0F * f3;
      float f7 = 65.0F * f3;
      nvg.drawRoundedRectVaried(l, f + f7, f2, f4 - f7, f5, 2130706432, 0.0F, f6, f6, 0.0F);
      nvg.drawRoundedRectVaried(l, f, f2, f7, f5, -1, f6, 0.0F, 0.0F, f6);
      nvg.drawTextEx(l, displayText, f + f7 + 12.0F * f3, f2 + 31.0F * f3, -1, 25.0F * f3, OneConfigFonts.SEMIBOLD);
      nvg.drawTextEx(l, "Reborn", f + f7 + 12.0F * f3, f2 + 53.0F * f3, -3618616, 18.0F * f3, OneConfigFonts.REGULAR);
      float f8 = 50.0F * f3;
      float f9 = f + (f7 - f8) / 2.0F;
      float f10 = f2 + (f5 - f8) / 2.0F;
      nvg.draw(l, OneConfigSvgs.ICON_WHITE, f9, f10, f8, f8, -16777216, 30.0F);
   }

   private void leap(float x, float y, float z) {
      String text = this.getDisplayText() + "   ";
      String text2 = "Reborn";
      float f = 28.0F * z;
      float f2 = 20.0F * z;
      NanoFontRenderer nanoFontRenderer = NanoFontManager.nanoFontRenderer;
      float f3 = nanoFontRenderer.getTextWidth(text, f) + nanoFontRenderer.getTextWidth(text2, f2);
      float textWidth = nanoFontRenderer.getTextWidth(text, f);
      float f4 = 8.0F * z;
      float f5 = f4 * 2.0F + f3;
      float f6 = nanoFontRenderer.getTextWidth(f) + f4 * 2.0F;
      drawDropShadow(x, y, f5, f6, 8.0F * z);
      nanoFontRenderer.applyTextEx(text, x + f4, y + f4, f, Theme.blendColor(0));
      float f7 = x + f4 + textWidth;
      float f8 = y + nanoFontRenderer.getTextWidth(f) / 2.0F + f4 - nanoFontRenderer.getTextWidth(f2) / 2.0F;
      nanoFontRenderer.applyText(text2, f7 + 0.5F, f8 + 0.5F, f2, new Color(63, 63, 63));
      nanoFontRenderer.applyText(text2, f7, f8, f2, Color.WHITE);
      this.panelWidth = f4 * 2.0F + f3;
      this.panelHeight = nanoFontRenderer.getTextWidth(f) + f4 * 2.0F;
   }

   private static void drawDropShadow(float f, float f2, float f3, float f4, float f5) {
      long timestamp = NanoVGContext.timestamp;
      NVGColor nvgColor = NVGColor.calloc();

      try {
         nvgColor.r(0.0F).g(0.0F).b(0.0F).a(0.39215687F);
         NanoVG.nvgBeginPath(timestamp);
         SquirclePath.draw(timestamp, f, f2, f3, f4, f5);
         NanoVG.nvgFillColor(timestamp, nvgColor);
         NanoVG.nvgFill(timestamp);
         NanoVGTransform.closePath();
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

      float f6 = 10.0F;
      NVGPaint paint = NVGPaint.calloc();

      try {
         NVGColor nvgColor2 = NVGColor.calloc();

         try {
            NVGColor nvgColor3 = NVGColor.calloc();

            try {
               nvgColor2.r(0.0F).g(0.0F).b(0.0F).a(0.3137255F);
               nvgColor3.r(0.0F).g(0.0F).b(0.0F).a(0.0F);
               NanoVG.nvgBoxGradient(timestamp, f, f2, f3, f4, f5, f6, nvgColor2, nvgColor3, paint);
               NanoVG.nvgBeginPath(timestamp);
               NanoVG.nvgRect(timestamp, f - f6, f2 - f6, f3 + f6 * 2.0F, f4 + f6 * 2.0F);
               SquirclePath.draw(timestamp, f, f2, f3, f4, f5);
               NanoVG.nvgPathWinding(timestamp, 2);
               NanoVG.nvgFillPaint(timestamp, paint);
               NanoVG.nvgFill(timestamp);
            } catch (Throwable e3) {
               if (nvgColor3 != null) {
                  try {
                     nvgColor3.close();
                  } catch (Throwable e4) {
                     e3.addSuppressed(e4);
                  }
               }

               throw e3;
            }

            if (nvgColor3 != null) {
               nvgColor3.close();
            }
         } catch (Throwable e5) {
            if (nvgColor2 != null) {
               try {
                  nvgColor2.close();
               } catch (Throwable e6) {
                  e5.addSuppressed(e6);
               }
            }

            throw e5;
         }

         if (nvgColor2 != null) {
            nvgColor2.close();
         }
      } catch (Throwable e7) {
         if (paint != null) {
            try {
               paint.close();
            } catch (Throwable e8) {
               e7.addSuppressed(e8);
            }
         }

         throw e7;
      }

      if (paint != null) {
         paint.close();
      }
   }

   private String getDisplayTextEx() {
      if (this.subtitleContent.getValue() != 6) {
         this.subtitleFade = 1.0F;
         return this.formatText(this.subtitleContent.getValue());
      }

      long now = System.currentTimeMillis();
      if (now - this.lastSubtitleSwitch > 3000L && !this.fading) {
         this.previousSubtitle = this.formatText(this.subtitleIndex);
         this.subtitleIndex = this.subtitleIndex < 5 ? this.subtitleIndex + 1 : 1;
         if (!this.previousSubtitle.equals(this.formatText(this.subtitleIndex))) {
            this.fading = true;
            this.lastSubtitleSwitch = now;
         }
      }

      if (!this.fading) {
         return this.formatText(this.subtitleIndex);
      } else {
         long l = now - this.lastSubtitleSwitch;
         if (l < 300L) {
            this.subtitleFade = 1.0F - evaluate((float)l / 300.0F);
            return this.previousSubtitle;
         } else if (l < 600L) {
            this.subtitleFade = evaluate((float)(l - 300L) / 300.0F);
            return this.formatText(this.subtitleIndex);
         } else {
            this.fading = false;
            this.subtitleFade = 1.0F;
            String text = this.formatText(this.subtitleIndex);
            this.lastSubtitleSwitch = now;
            return text;
         }
      }
   }

   private String formatText(int n) {
      return switch (n) {
         case 1 -> mc.getCurrentFps() + " FPS";
         case 2 -> mc.player != null ? mc.player.getName().getString() : "Player";
         case 3 -> {
            long l = (System.currentTimeMillis() - this.startTime) / 1000L;
            yield String.format("%02d:%02d:%02d", l / 3600L, l % 3600L / 60L, l % 60L);
         }
         case 4 -> mc.player != null ? mc.player.getPlayerListEntry().getLatency() + " ms" : "Ping";
         case 5, 6 -> this.getDisplayTextAlt();
         default -> "";
      };
   }

   private static float evaluate(float f) {
      return f < 0.5F ? 4.0F * f * f * f : 1.0F - (float)Math.pow(-2.0F * f + 2.0F, 3.0) / 2.0F;
   }

   private String getDisplayTextAlt() {
      this.performAction();
      return this.hitokotoText != null && !this.hitokotoText.isBlank() ? this.hitokotoText : "曹家豪130185200910260034";
   }

   private void performAction() {
      long now = System.currentTimeMillis();
      boolean flag = now - this.lastHitokotoFetch >= 20000L || "曹家豪130185200910260034".equals(this.hitokotoText);
      if (flag && this.hitokotoFetching.compareAndSet(false, true)) {
         OneConfigMultithreading.getFutureEx(this::performActionEx);
      }
   }

   private void performActionEx() {
      try {
         String displayText = HttpClientUtils.getInner2("https://v1.hitokoto.cn/?encode=text&charset=utf-8").getInner2AltEx().getDisplayText();
         if (displayText != null) {
            displayText = displayText.trim();
         }

         this.hitokotoText = displayText != null && !displayText.isEmpty() ? displayText : "曹家豪130185200910260034";
         this.lastHitokotoFetch = System.currentTimeMillis();
      } catch (Exception e) {
         this.hitokotoText = "曹家豪130185200910260034";
         this.lastHitokotoFetch = System.currentTimeMillis();
      } finally {
         this.hitokotoFetching.set(false);
      }
   }
}
