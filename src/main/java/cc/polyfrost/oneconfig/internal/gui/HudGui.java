package cc.polyfrost.oneconfig.internal.gui;

import cc.polyfrost.oneconfig.hud.Hud;
import cc.polyfrost.oneconfig.libs.universal.UKeyboard;
import com.google.gson.annotations.SerializedName;
import dev.southside.gui.CustomScreen;
import dev.southside.render.UIMatrixStack;
import dev.southside.utils.AssetHelper;
import dev.southside.utils.ConfigCore;
import dev.southside.utils.GuiUtils;
import dev.southside.utils.HudCore;
import dev.southside.utils.HudDragOffset;
import dev.southside.utils.HudPosition;
import dev.southside.utils.HudSnapLine;
import dev.southside.utils.HudValue;
import dev.southside.utils.IPauseControlScreen;
import dev.southside.utils.MathClampUtils;
import dev.southside.utils.NanoVGHelper;
import dev.southside.utils.NanoVGImage;
import dev.southside.utils.OneConfigFonts;
import dev.southside.utils.OneConfigGui;
import dev.southside.utils.WindowMetrics;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.NotNull;

public class HudGui extends CustomScreen implements IPauseControlScreen {
   private static final int DEFAULT_INT = 10;
   @SerializedName("editingHuds")
   private final HashMap<Hud, HudDragOffset> editingHuds = new HashMap<>();
   @SerializedName("isDragging")
   private boolean isDragging;
   @SerializedName("isSelecting")
   private boolean isSelecting;
   @SerializedName("selectX")
   private float selectX;
   @SerializedName("selectY")
   private float selectY;
   @SerializedName("isScaling")
   public boolean isScaling;
   @SerializedName("superSecretString")
   private String superSecretString = "";

   @Override
   public void performAction(int n, int n2) {
      HudCore.active = true;
      super.performAction(n, n2);
   }

   @Override
   public void renderAll(@NotNull UIMatrixStack matrices, DrawContext context, int n, int n2, float f) {
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;
      int n3 = Math.max(1, Math.round(Math.min(WindowMetrics.getFramebufferWidth() / 1920.0F, WindowMetrics.getFramebufferHeight() / 1080.0F)));
      if (this.isDragging) {
         nvg.performAction(l -> this.draw(l, n, n2, true, true, n3));
      } else if (this.isSelecting) {
         this.getHudsInRegion(this.selectX, this.selectY, n, n2);
      } else if (this.isScaling && this.editingHuds.size() == 1) {
         nvg.performAction(l2 -> {
            Hud hud = (Hud)this.editingHuds.keySet().toArray()[0];
            HudPosition hudPosition = hud.position.getValue();
            float f2 = this.computeFloat(l2, n3, n, hudPosition.getWidth(), false);
            float f3 = (f2 - hudPosition.getProgress()) / (hudPosition.getWidth() / hud.getProgress());
            hud.performAction(MathClampUtils.clamp(f3, 0.2F, 10.0F), true);
         });
      }

      float scaleFactor = WindowMetrics.getScaleFactor();

      for (Hud hud2 : HudCore.hudMap.values()) {
         if (hud2.isValidEx()) {
            HudPosition hudPosition2 = hud2.position.getValue();
            hud2.drawAll(context, matrices, true);
            if (this.editingHuds.containsKey(hud2)) {
               nvg.performAction(
                  l3 -> nvg.drawTextEx(
                     l3,
                     hudPosition2.anchor.toString(),
                     (hudPosition2.getProgress() + 2.0F) * scaleFactor,
                     (hudPosition2.getProgressEx() - 6.0F) * scaleFactor,
                     -16777216,
                     10.0F * scaleFactor,
                     OneConfigFonts.MINECRAFT_REGULAR
                  )
               );
               nvg.performAction(
                  true,
                  l4 -> nvg.drawRect(
                     l4, hudPosition2.getProgress(), hudPosition2.getProgressEx(), hudPosition2.getWidth(), hudPosition2.getHeight(), new Color(0, 128, 128, 60).getRGB()
                  )
               );
            }

            if (hud2.isValidAlt()) {
               nvg.performAction(
                  true,
                  l5 -> nvg.drawRect(
                     l5, hudPosition2.getProgress(), hudPosition2.getProgressEx(), hudPosition2.getWidth(), hudPosition2.getHeight(), new Color(238, 36, 36, 60).getRGB()
                  )
               );
            }

            nvg.performAction(l6 -> performAction(nvg, hudPosition2, scaleFactor, n3, l6));
            if (this.editingHuds.containsKey(hud2) && this.editingHuds.size() == 1) {
               nvg.performAction(
                  true,
                  l7 -> nvg.drawRect(l7, hudPosition2.getProgressAlt() - 4.0F, hudPosition2.getProgressImpl() - 4.0F, 8.0F, 8.0F, new Color(0, 128, 128, 200).getRGB())
               );
            }
         }
      }

      if (this.isSelecting) {
         nvg.performAction(
            true, l8 -> nvg.drawRect(l8, this.selectX, this.selectY, n - this.selectX, n2 - this.selectY, new Color(0, 0, 255, 100).getRGB())
         );
      }
   }

   private static void performAction(NanoVGHelper nvg, HudPosition hudPosition, float f, int n, long l) {
      nvg.drawLine(
         l,
         hudPosition.getProgress() * f - n / 2.0F,
         hudPosition.getProgressEx() * f - n / 2.0F,
         hudPosition.getProgressAlt() * f + n / 2.0F,
         hudPosition.getProgressEx() * f - n / 2.0F,
         n,
         new Color(255, 255, 255).getRGB()
      );
      nvg.drawLine(
         l,
         hudPosition.getProgress() * f - n / 2.0F,
         hudPosition.getProgressImpl() * f + n / 2.0F,
         hudPosition.getProgressAlt() * f + n / 2.0F,
         hudPosition.getProgressImpl() * f + n / 2.0F,
         n,
         new Color(255, 255, 255).getRGB()
      );
      nvg.drawLine(
         l,
         hudPosition.getProgress() * f - n / 2.0F,
         hudPosition.getProgressEx() * f - n / 2.0F,
         hudPosition.getProgress() * f - n / 2.0F,
         hudPosition.getProgressImpl() * f + n / 2.0F,
         n,
         new Color(255, 255, 255).getRGB()
      );
      nvg.drawLine(
         l,
         hudPosition.getProgressAlt() * f + n / 2.0F,
         hudPosition.getProgressEx() * f - n / 2.0F,
         hudPosition.getProgressAlt() * f + n / 2.0F,
         hudPosition.getProgressImpl() * f + n / 2.0F,
         n,
         new Color(255, 255, 255).getRGB()
      );
   }

   @Override
   public void onMouseClick(double d, double d2, int n) {
      super.onMouseClick(d, d2, n);
      if (n == 1) {
         for (Hud hud : this.editingHuds.keySet()) {
            if (this.isValid(hud, (float)d, (float)d2) && !hud.isValidAlt()) {
               float progress = hud.position.getValue().getProgress();
               float progressEx = hud.position.getValue().getProgressEx();
               hud.position.getValue().anchor = HudPosition.getMode((hud.position.getValue().anchor.ordinal() + 1) % 9);
               hud.position.getValue().setProgress(progress);
               hud.position.getValue().setProgressEx(progressEx);
               return;
            }
         }
      }

      if (n == 0) {
         this.isDragging = false;
         this.isSelecting = false;
         this.isScaling = false;
         if (this.editingHuds.size() == 1) {
            HudPosition hudPosition = ((Hud)this.editingHuds.keySet().toArray()[0]).position.getValue();
            if (d >= hudPosition.getProgressAlt() - 7.0F
               && d <= hudPosition.getProgressAlt() + 7.0F
               && d2 >= hudPosition.getProgressImpl() - 7.0F
               && d2 <= hudPosition.getProgressImpl() + 7.0F) {
               this.isScaling = true;
               return;
            }
         }

         for (Hud hud2 : HudCore.hudMap.values()) {
            if (hud2.isValidEx() && this.isValid(hud2, (float)d, (float)d2) && !hud2.isValidAlt()) {
               if (!this.editingHuds.containsKey(hud2)) {
                  if (!UKeyboard.hasControlDown()) {
                     this.editingHuds.clear();
                  }

                  this.editingHuds.put(hud2, new HudDragOffset());
               }

               this.isDragging = true;
               this.editingHuds
                  .forEach(
                     (hud3, hudDragOffset) -> hudDragOffset.performAction(
                        (float)(d - hud3.position.getValue().getProgress()), (float)(d2 - hud3.position.getValue().getProgressEx())
                     )
                  );
               return;
            }
         }

         this.isSelecting = true;
         this.selectX = (float)d;
         this.selectY = (float)d2;
      }
   }

   @Override
   public void onMouseClickEx(double d, double d2, int n) {
      this.isDragging = false;
      this.isSelecting = false;
      this.isScaling = false;
      super.onMouseClickEx(d, d2, n);
   }

   @Override
   public void performAction(int n, int n2, int n3) {
      if (!this.isDragging) {
         this.editingHuds.forEach((hud, hudDragOffset) -> hudDragOffset.performAction(-hud.position.getValue().getProgress(), -hud.position.getValue().getProgressEx()));
         if (n == 265) {
            this.performAction(0.0F, -1.0F, false);
         } else if (n == 264) {
            this.performAction(0.0F, 1.0F, false);
         } else if (n == 263) {
            this.performAction(-1.0F, 0.0F, false);
         } else if (n == 262) {
            this.performAction(1.0F, 0.0F, false);
         }

         super.performAction(n, n2, n3);
      }
   }

   @Override
   public void performActionEx(int n, int n2) {
      if (!this.isDragging) {
         this.superSecretMethod((char)n);
         super.performActionEx(n, n2);
      }
   }

   @Override
   public void performAction() {
      super.performAction();
      HudCore.active = false;
      ConfigCore.performAction();
      GuiUtils.openScreen(OneConfigGui.getSideBarUtil());
   }

   @Override
   public boolean shouldPause() {
      return false;
   }

   private void getHudsInRegion(float x, float y, float width, float height) {
      if (x > width) {
         float f = x;
         x = width;
         width = f;
      }

      if (y > height) {
         float f2 = y;
         y = height;
         height = f2;
      }

      this.editingHuds.clear();

      for (Hud hud : HudCore.hudMap.values()) {
         if (hud.isValidEx() && !hud.isValidAlt()) {
            HudPosition hudPosition = hud.position.getValue();
            if ((x <= hudPosition.getProgress() && width >= hudPosition.getProgress() || x <= hudPosition.getProgressAlt() && width >= hudPosition.getProgressAlt())
               && (y <= hudPosition.getProgressEx() && height >= hudPosition.getProgressEx() || y <= hudPosition.getProgressImpl() && height >= hudPosition.getProgressImpl())) {
               this.editingHuds.put(hud, new HudDragOffset());
            }
         }
      }
   }

   private void draw(long l, float f, float f2, boolean flag, boolean flag2, int n) {
      for (Hud hud : this.editingHuds.keySet()) {
         HudDragOffset hudDragOffset = this.editingHuds.get(hud);
         HudPosition hudPosition = hud.position.getValue();
         float f3 = f - hudDragOffset.getOffsetX();
         float f4 = f2 - hudDragOffset.getOffsetY();
         if (this.editingHuds.size() == 1 && flag) {
            f3 = this.computeFloat(l, n, f3, hudPosition.getWidth(), true);
            f4 = this.computeFloatEx(l, n, f4, hudPosition.getHeight(), true);
         }

         if (flag2) {
            if (f3 < 0.0F) {
               f3 = 0.0F;
            } else if (f3 + hudPosition.getWidth() > WindowMetrics.getScaledWidth()) {
               f3 = WindowMetrics.getScaledWidth() - hudPosition.getWidth();
            }

            if (f4 < 0.0F) {
               f4 = 0.0F;
            } else if (f4 + hudPosition.getHeight() > WindowMetrics.getScaledHeight()) {
               f4 = WindowMetrics.getScaledHeight() - hudPosition.getHeight();
            }
         }

         hudPosition.performAction(f3, f4);
      }
   }

   private void performAction(float f, float f2, boolean flag) {
      this.draw(0L, f, f2, false, flag, 0);
   }

   private boolean isValid(Hud hud, float f, float f2) {
      HudPosition hudPosition = hud.position.getValue();
      return f >= hudPosition.getProgress() && f <= hudPosition.getProgressAlt() && f2 >= hudPosition.getProgressEx() && f2 <= hudPosition.getProgressImpl();
   }

   private float computeFloat(long l, float f, float f2, float f3, boolean flag) {
      ArrayList<Float> list = this.collectItems();
      ArrayList<HudSnapLine> list2 = new ArrayList<>();
      float f4 = 10.0F / WindowMetrics.getScaleFactor();

      for (Float val : list) {
         HudSnapLine hudSnapLine = new HudSnapLine(val, f2, f3, flag);
         if (Math.round(hudSnapLine.getDistance()) == Math.round(f4)) {
            list2.add(hudSnapLine);
         } else if (hudSnapLine.getDistance() < f4) {
            f4 = hudSnapLine.getDistance();
            list2.clear();
            list2.add(hudSnapLine);
         }
      }

      if (list2.isEmpty()) {
         return f2;
      }

      for (HudSnapLine hudSnapLine2 : list2) {
         hudSnapLine2.draw(l, f, true);
      }

      return ((HudSnapLine)list2.get(0)).getPosition();
   }

   private ArrayList<Float> collectItems() {
      ArrayList list = new ArrayList();
      list.add(WindowMetrics.getScaledWidth() / 2.0F);

      for (Hud hud : HudCore.hudMap.values()) {
         if (hud.isValidEx() && !this.editingHuds.containsKey(hud)) {
            list.add(hud.position.getValue().getProgress());
            list.add(hud.position.getValue().getProgressDirect());
            list.add(hud.position.getValue().getProgressAlt());
         }
      }

      return list;
   }

   private float computeFloatEx(long l, float f, float f2, float f3, boolean flag) {
      ArrayList<Float> list = this.collectItemsEx();
      ArrayList<HudSnapLine> list2 = new ArrayList<>();
      float f4 = 10.0F / WindowMetrics.getScaleFactor();

      for (Float val : list) {
         HudSnapLine hudSnapLine = new HudSnapLine(val, f2, f3, flag);
         if (Math.round(hudSnapLine.getDistance()) == Math.round(f4)) {
            list2.add(hudSnapLine);
         } else if (hudSnapLine.getDistance() < f4) {
            f4 = hudSnapLine.getDistance();
            list2.clear();
            list2.add(hudSnapLine);
         }
      }

      if (list2.isEmpty()) {
         return f2;
      }

      for (HudSnapLine hudSnapLine2 : list2) {
         hudSnapLine2.draw(l, f, false);
      }

      return ((HudSnapLine)list2.get(0)).getPosition();
   }

   private ArrayList<Float> collectItemsEx() {
      ArrayList list = new ArrayList();
      list.add(WindowMetrics.getScaledHeight() / 2.0F);

      for (Hud hud : HudCore.hudMap.values()) {
         if (hud.isValidEx() && !this.editingHuds.containsKey(hud)) {
            list.add(hud.position.getValue().getProgressEx());
            list.add(hud.position.getValue().getProgressSafe());
            list.add(hud.position.getValue().getProgressImpl());
         }
      }

      return list;
   }

   private void superSecretMethod(char ch) {
      this.superSecretString = this.superSecretString + ch;
      this.superSecretString = this.superSecretString.toLowerCase();
      if (!"blahaj".substring(0, this.superSecretString.length()).equals(this.superSecretString)
         && !"blåhaj".substring(0, this.superSecretString.length()).equals(this.superSecretString)
         && !"bigrat".substring(0, this.superSecretString.length()).equals(this.superSecretString)) {
         this.superSecretString = "";
      } else if ("blahaj".equals(this.superSecretString) || "blåhaj".equals(this.superSecretString) || "bigrat".equals(this.superSecretString)) {
         String text;
         switch (this.superSecretString) {
            case "blahaj":
            case "blåhaj":
               text = "https://blahaj.shop/api/random/image?" + UUID.randomUUID();
               break;
            case "bigrat":
               text = "https://bigrat.monster/media/bigrat.png";
               break;
            default:
               return;
         }

         this.superSecretString = "";
         AtomicBoolean atomicBoolean = new AtomicBoolean();
         AssetHelper assetHelper = AssetHelper.INSTANCE;
         NanoVGHelper.INSTANCE.performAction(l -> atomicBoolean.set(assetHelper.isValid(l, text, HudGui.class)));
         if (atomicBoolean.get()) {
            NanoVGImage nanoVGImage = assetHelper.get(text);
            int width = nanoVGImage.getWidth();
            int height = nanoVGImage.getHeight();
            float f = Math.min(300.0F / width, 300.0F / height);
            HudCore.hudMap.put(new HudValue("bigrat", null), new HudGui.Inner(text, width * f, height * f));
         }
      }
   }

   private static class Inner extends Hud {
      @SerializedName("url")
      private final String url;
      @SerializedName("width")
      private final float width;
      @SerializedName("height")
      private final float height;

      public Inner(String text, float f, float f2) {
         this.url = text;
         this.width = f;
         this.height = f2;
      }

      @Override
      public void draw(DrawContext context, UIMatrixStack matrices, float f, float f2, float f3, boolean flag) {
         NanoVGHelper.INSTANCE.performAction(true, l -> NanoVGHelper.INSTANCE.draw(l, this.url, f, f2, this.width * f3, this.height * f3));
      }

      @Override
      public float computeFloat(float f, boolean flag) {
         return this.width * f;
      }

      @Override
      public float setScale(float f, boolean flag) {
         return this.height * f;
      }
   }
}
