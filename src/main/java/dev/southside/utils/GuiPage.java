package dev.southside.utils;

import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.Objects;

public abstract class GuiPage {
   @SerializedName("title")
   protected final String title;
   @SerializedName("scrollAnimation")
   public UiAnimation scrollAnimation;
   @SerializedName("colorAnimation")
   private final ColorAnimation colorAnimation = new ColorAnimation(
      new ColorPalette(OneConfigColors.TRANSPARENT, OneConfigColors.GRAY_400_60, OneConfigColors.GRAY_400_60), 200
   );
   @SerializedName("scrollTarget")
   public float scrollTarget;
   @SerializedName("scrollTime")
   private long scrollTime;
   @SerializedName("mouseWasDown")
   private boolean mouseWasDown;
   @SerializedName("dragging")
   private boolean dragging;
   @SerializedName("yStart")
   private float yStart;
   @SerializedName("dragStartScroll")
   private float dragStartScroll;
   @SerializedName("dragScrollScale")
   private float dragScrollScale;
   @SerializedName("scroll")
   public float scroll;
   @SerializedName("parents")
   public final ArrayList<GuiPage> parents = new ArrayList<>();
   @SerializedName("previousSearch")
   private String previousSearch = "";
   private static final long g = 1000L;

   public GuiPage(String text) {
      this.title = text;
   }

   public abstract void draw(long vg, int x, int y, InputHandler input);

   public int drawAndGetHeight(long vg, int x, int y, InputHandler input) {
      return 0;
   }

   public void performAction() {
      this.scroll = 0.0F;
      this.scrollTarget = 0.0F;
      this.scrollTime = 0L;
      this.scrollAnimation = null;
      this.dragging = false;
      this.mouseWasDown = false;
   }

   public void drawEx(long vg, int x, int y, InputHandler input) {
      this.draw(vg, x, y, 1056, 728, input);
   }

   public void draw(long l, int n, int n2, int n3, int n4, InputHandler input) {
      ScissorHelper scissor = ScissorHelper.INSTANCE;
      int size = this.getSize();
      int n5 = Math.max(0, Math.min(n4 - 1, this.drawAndGetHeight(l, n, n2, input)));
      if (OneConfigGui.INSTANCE != null && !Objects.equals(this.previousSearch, OneConfigGui.INSTANCE.getInput())) {
         this.previousSearch = OneConfigGui.INSTANCE.getInput();
         this.performAction();
      }

      this.scroll = this.scrollAnimation == null ? this.scrollTarget : this.scrollAnimation.getProgress();
      float f = Math.max(0, size - n4);
      float f2 = n4 - n5;
      this.scrollTarget = Math.max(-f, Math.min(0.0F, this.scrollTarget));
      this.scroll = Math.max(-f, Math.min(0.0F, this.scroll));
      float f3 = Math.min(f2, Math.max(24.0F, f2 * f2 / Math.max(f2, size - n5)));
      float f4 = f2 - f3;
      float f5 = n2 + n5 + (f == 0.0F ? 0.0F : -this.scroll / f * f4);
      boolean isHovered = input.isHovered(n, n2 + n5, n3, f2);
      boolean flag = f > 0.0F && input.isHovered(n + n3 - 14, f5, 12.0F, f3);
      boolean isKeyDown = OneConfigPlatform.getMousePlatform().isKeyDown(0);
      boolean dragging2 = this.dragging;
      if (!isKeyDown || f == 0.0F) {
         this.dragging = false;
      }

      if (flag && isKeyDown && !this.mouseWasDown) {
         this.yStart = input.getProgressEx();
         this.dragStartScroll = this.scroll;
         this.dragScrollScale = f4 > 0.0F ? f / f4 : 0.0F;
         this.dragging = true;
      }

      this.mouseWasDown = isKeyDown;
      float f6 = f > 0.0F ? (float)input.getCurrentValue() : 0.0F;
      if (this.dragging) {
         float f7 = f4 > 0.0F ? f / f4 : 0.0F;
         if (f7 != this.dragScrollScale) {
            this.yStart = input.getProgressEx();
            this.dragStartScroll = this.scroll;
            this.dragScrollScale = f7;
         }

         this.scrollTarget = Math.max(-f, Math.min(0.0F, this.dragStartScroll - (input.getProgressEx() - this.yStart) * this.dragScrollScale));
         this.scroll = this.scrollTarget;
         this.scrollAnimation = null;
         this.scrollTime = System.currentTimeMillis();
      } else if (f > 0.0F && isHovered && f6 != 0.0F) {
         this.scrollTarget = Math.max(-f, Math.min(0.0F, this.scrollTarget + f6));
         this.scrollAnimation = new EaseOutQuadAnimation(150, this.scroll, this.scrollTarget, false);
         this.scrollTime = System.currentTimeMillis();
      }

      if (this.scrollAnimation != null && this.scrollAnimation.isValid()) {
         this.scrollAnimation = null;
      }

      ScissorRect clip = scissor.scissor(l, n, n2 + n5, n3, f2);
      ScissorRect clip2 = isHovered && !this.dragging && !dragging2
         ? input.getScissorRect(n + n3 - 14, n2, f > 0.0F ? 14.0F : 0.0F, n4)
         : input.getScissorRect();

      try {
         this.draw(l, n, (int)(n2 + this.scroll), input);
      } finally {
         input.remove(clip2);
         scissor.resetScissor(l, clip);
      }

      if (f == 0.0F) {
         input.getCurrentValue();
      }

      if (f > 0.0F) {
         f5 = n2 + n5 - this.scroll / f * f4;
         boolean flag2 = System.currentTimeMillis() - this.scrollTime < g;
         NanoVGHelper.INSTANCE.drawRoundedRect(l, n + n3 - 8, f5, 4.0F, f3, this.colorAnimation.computeInt(flag || flag2, this.dragging), 4.0F);
      }
   }

   public String getTitle() {
      return this.title;
   }

   public void onKeyTyped(int n) {
   }

   public void onKeyTypedEx(int n) {
   }

   public boolean isValid() {
      return false;
   }

   public void onMouseClick(double d, double d2, int n) {
   }

   public void onMouseClickEx(double d, double d2, int n) {
   }

   public boolean isBase() {
      return false;
   }

   public int getSize() {
      return 728;
   }
}
