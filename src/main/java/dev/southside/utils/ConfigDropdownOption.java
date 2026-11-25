package dev.southside.utils;

import cc.polyfrost.oneconfig.config.elements.BasicOption;
import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import com.google.gson.annotations.SerializedName;
import dev.southside.utils.freecookie.FreeCookieAPI;
import java.awt.Color;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ConfigDropdownOption extends BasicOption {
   @SerializedName("originalOptions")
   private final String[] originalOptions;
   @SerializedName("options")
   private final List<String> options;
   @SerializedName("backgroundColor")
   private final ColorAnimation backgroundColor = new ColorAnimation(ColorPalette.INSTANCE3);
   @SerializedName("atomColor")
   private final ColorAnimation atomColor = new ColorAnimation(new ColorPalette(OneConfigColors.PRIMARY_500, OneConfigColors.PRIMARY_400, OneConfigColors.PRIMARY_400));
   @SerializedName("colorAnimation")
   private final ColorAnimation colorAnimation = new ColorAnimation(new ColorPalette(OneConfigColors.GRAY_400_80, OneConfigColors.GRAY_400, OneConfigColors.GRAY_400), 200);
   @SerializedName("textInputField")
   private final FreeCookieAPI textInputField;
   @SerializedName("opened")
   private boolean opened;
   @SerializedName("inputScissor")
   private ScissorRect inputScissor;
   @SerializedName("scrollAnimation")
   private UiAnimation scrollAnimation;
   @SerializedName("scrollTarget")
   private float scrollTarget;
   @SerializedName("scrollTime")
   private long scrollTime;
   @SerializedName("mouseWasDown")
   private boolean mouseWasDown;
   @SerializedName("dragging")
   private boolean dragging;
   @SerializedName("yStart")
   private float yStart;
   @SerializedName("scroll")
   private float scroll;
   @SerializedName("inputHandler")
   private InputHandler inputHandler;
   private static final long DEFAULT_LONG = 1000L;

   public ConfigDropdownOption(Field field, Object obj, String text, String text2, String text3, String text4, int n, String[] textArray) {
      super(field, obj, text, text2, text3, text4, n);
      this.originalOptions = textArray;
      this.options = new ArrayList<>(textArray.length);
      this.options.addAll(Arrays.asList(textArray));
      this.textInputField = new FreeCookieAPI(n != 1 ? 640 : 256, 32, "Search...", false, false, OneConfigSvgs.SEARCH_SM, 12.0F);
   }

   @Override
   public void drawEx(long vg, int x, int y, InputHandler input) {
      super.drawEx(vg, x, y, input);
      if (!this.opened) {
         this.setActive(false);
      } else {
         if (this.dragging) {
            input.getScissorRect();
         }

         NanoVGHelper nvg = NanoVGHelper.INSTANCE;
         boolean flag = this.size == 1;
         int n = x + (flag ? 224 : 352);
         int n2 = flag ? 256 : 640;
         boolean isHovered = input.isHovered(n, y, n2, 32.0F, true);
         this.textInputField.setErrored(this.options.isEmpty());
         if (isHovered && OneConfigPlatform.getMousePlatform().isKeyDown(0)) {
            nvg.setAlpha(vg, 0.8F);
         }

         this.textInputField.draw(vg, n, y, input);
         if (!this.options.isEmpty()) {
            nvg.setAlpha(vg, 1.0F);
            nvg.drawRoundedRect(vg, n, y + 40, n2, Math.min(this.options.size(), 10) * 32 + 8, OneConfigColors.GRAY_700, 12.0F);
            nvg.drawRoundedRect(vg, n - 1, y + 39, n2 + 2, Math.min(this.options.size(), 10) * 32 + 10, new Color(204, 204, 204, 77).getRGB(), 12.0F, 1.0F);
            ScissorHelper scissor = ScissorHelper.INSTANCE;
            ScissorRect clip = null;
            if (this.options.size() > 10) {
               clip = scissor.scissor(vg, n, y + 40, n2, 328.0F);
            }

            float f = Math.min(this.options.size(), 10) * 32 + 8;
            boolean isHovered2 = input.isHovered(n, y + 40, n2, f, true);
            if (this.opened && isHovered2) {
               input.performActionEx();
            }

            this.scroll = this.scrollAnimation != null ? this.scrollAnimation.getProgress() : this.scrollTarget;
            float f2 = this.options.size() * 32 + 8;
            float f3 = f / f2 * f;
            this.performAction(input, f, f2);
            this.draw(vg, n, y, n2, flag, input);
            if (clip != null) {
               scissor.resetScissor(vg, clip);
            }

            if (this.dragging && input.isValid(true)) {
               this.dragging = false;
               input.clear();
            }

            this.draw(vg, n + n2 - 4, y, f3, f, f2, input);
            if (isHovered2 && this.opened) {
               input.performActionEx();
            }
         }
      }
   }

   private void performAction(InputHandler input, float f, float f2) {
      float f3 = (float)input.computeDouble(true);
      if (f3 != 0.0F) {
         this.scrollTarget += f3;
         this.performAction(f, f2);
         this.scrollTime = System.currentTimeMillis();
      } else if (this.scrollAnimation != null && this.scrollAnimation.isValid()) {
         this.scrollAnimation = null;
      }
   }

   private void performAction(float x, float y) {
      if (this.scrollTarget > 0.0F) {
         this.scrollTarget = 0.0F;
      } else if (this.scrollTarget < -y + x) {
         this.scrollTarget = -y + x;
      }

      this.scrollAnimation = new EaseOutQuadAnimation(150, this.scroll, this.scrollTarget, false);
   }

   private void draw(long l, int n, int n2, int n3, boolean flag, InputHandler input) {
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;
      float f = n2 + 44 + this.scroll;

      for (String text : this.options) {
         int white_80 = OneConfigColors.WHITE_80;
         boolean isHovered = input.isHovered(n, f, flag ? 252.0F : 640.0F, flag ? 32.0F : 36.0F, true);
         if (isHovered && OneConfigPlatform.getMousePlatform().isKeyDown(0)) {
            nvg.drawRoundedRect(l, n + 4, f + 2.0F, n3 - 8, 28.0F, OneConfigColors.PRIMARY_600_80, 8.0F);
         } else if (isHovered) {
            nvg.drawRoundedRect(l, n + 4, f + 2.0F, n3 - 8, 28.0F, OneConfigColors.PRIMARY_600, 8.0F);
            white_80 = OneConfigColors.WHITE;
         }

         if (flag) {
            this.applyText(text, isHovered, input);
         }

         nvg.drawTextEx(l, text, n + 16, f + 18.0F, white_80, 14.0F, OneConfigFonts.ONE_CONFIG_FONT);
         if (!flag) {
            this.applyText(text, isHovered, input);
         }

         f += 32.0F;
      }
   }

   private void applyText(String text, boolean flag, InputHandler input) {
      if (flag && input.isValid(!this.dragging)) {
         try {
            this.performAction(Integer.valueOf(Arrays.asList(this.originalOptions).indexOf(text)));
         } catch (IllegalAccessException e) {
         }

         this.opened = false;
         this.backgroundColor.performAction(ColorPalette.INSTANCE3);
         input.performActionAlt();
         if (this.inputScissor != null && !this.dragging) {
            input.remove(this.inputScissor);
         }
      }
   }

   private void draw(long l, int n, int n2, float f, float f2, float f3, InputHandler input) {
      if (!(f >= f2)) {
         float f4 = this.scroll / f3 * (f2 - 8.0F) - 45.0F;
         boolean isKeyDown = OneConfigPlatform.getMousePlatform().isKeyDown(0);
         boolean isHovered = input.isHovered(n, n2 - f4, 12.0F, (int)f - 5, true);
         boolean flag = System.currentTimeMillis() - this.scrollTime < DEFAULT_LONG;
         if (isHovered && isKeyDown && !this.mouseWasDown) {
            this.yStart = input.getProgressEx();
            this.dragging = true;
         }

         this.mouseWasDown = isKeyDown;
         if (this.dragging) {
            this.scrollTarget = -(input.getProgressEx() - this.yStart) * f3 / f2;
            this.performAction(f2, f3);
         }

         int n3 = this.colorAnimation.computeInt(isHovered || flag, this.dragging);
         NanoVGHelper.INSTANCE.drawRoundedRect(l, n, n2 - f4, 4.0F, f - 5.0F, n3, 4.0F);
      }
   }

   @Override
   public void draw(long vg, int x, int y, InputHandler input) {
      this.inputHandler = input;
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;
      if (!this.isValidEx()) {
         nvg.setAlpha(vg, 0.5F);
      }

      nvg.drawTextEx(vg, this.name, x, y + 16, super.nameColor, 14.0F, OneConfigFonts.ONE_CONFIG_FONT);
      int n = x + (this.size == 1 ? 224 : 352);
      int n2 = this.size == 1 ? 256 : 640;
      boolean flag = input.isHovered(n, y, n2, 32.0F) && this.isValidEx();
      if (flag && input.isValid() || this.isValid(x, y, input)) {
         this.opened = !this.opened;
         if (!this.opened) {
            input.performActionAlt();
            input.clear();
            if (this.inputScissor != null) {
               input.remove(this.inputScissor);
            }
         } else {
            this.inputScissor = input.getScissorRect();
            this.textInputField.performAction();
            this.performActionAlt();
         }

         this.backgroundColor.performAction(this.opened ? ColorPalette.INSTANCE2 : ColorPalette.INSTANCE3);
      }

      if (!this.opened) {
         int n3 = 0;

         try {
            n3 = (Integer)this.resolveValue();
         } catch (IllegalAccessException e) {
         }

         if (flag && OneConfigPlatform.getMousePlatform().isKeyDown(0)) {
            nvg.setAlpha(vg, 0.8F);
         }

         int n4 = this.backgroundColor.computeInt(flag, flag && OneConfigPlatform.getMousePlatform().isKeyDown(0));
         nvg.drawRoundedRect(vg, n, y, n2, 32.0F, n4, 12.0F);
         nvg.drawTextEx(vg, this.originalOptions[n3], n + 12, y + 16, OneConfigColors.WHITE_80, 14.0F, OneConfigFonts.ONE_CONFIG_FONT);
         nvg.drawRoundedRect(vg, n + n2 - 28, y + 4, 24.0F, 24.0F, this.atomColor.computeInt(flag, false), 8.0F);
         nvg.draw(vg, OneConfigSvgs.DROPDOWN_LIST, n + n2 - 28, y + 4, 24.0F, 24.0F);
         nvg.setAlpha(vg, 1.0F);
      }
   }

   private boolean isValid(int n, int n2, InputHandler input) {
      return this.opened
         && input.isValid(!this.dragging)
         && (
            this.size == 1 && !input.isHovered(n + 224, n2 + 40, 256.0F, this.options.size() * 32, true)
               || this.size == 2 && !input.isHovered(n + 352, n2 + 40, 640.0F, this.options.size() * 32, true)
         );
   }

   private void performActionAlt() {
      this.options.clear();
      String text = this.textInputField.getInput().toLowerCase(Locale.ENGLISH);

      for (String text2 : this.originalOptions) {
         if (text2.toLowerCase(Locale.ENGLISH).contains(text)) {
            this.options.add(text2);
         }
      }
   }

   @Override
   public boolean isValid() {
      return super.isValid() && !this.opened;
   }

   @Override
   public void onMouseClick(double d, double d2, int n) {
      if (this.isValidEx() && this.opened) {
         this.textInputField.onKeyTypedAlt(n);
      }
   }

   @Override
   public void onMouseClickEx(double d, double d2, int n) {
      if (this.isValidEx() && this.opened) {
         this.textInputField.onKeyTypedImpl(n);
      }
   }

   @Override
   public void onKeyTyped(int n) {
      super.onKeyTyped(n);
      this.textInputField.onKeyTyped(n);
      if (this.opened) {
         this.performActionAlt();
      }
   }

   @Override
   public void onKeyTypedEx(int n) {
      super.onKeyTypedEx(n);
      this.textInputField.onKeyTypedEx(n);
      if (this.opened) {
         this.performActionAlt();
      }
   }

   private void setActive(boolean flag) {
      super.performActionEx();
      this.textInputField.setInput("");
      this.scroll = 0.0F;
      this.scrollTarget = 0.0F;
      this.scrollTime = 0L;
      this.scrollAnimation = null;
      if (flag && this.inputHandler != null) {
         this.inputHandler.performActionAlt();
         this.inputHandler.clear();
      }
   }

   @Override
   public void performActionEx() {
      super.performActionEx();
      this.setActive(true);
   }

   @Override
   public int getHeight() {
      return 32;
   }
}
