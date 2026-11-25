package dev.southside.utils;

import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import com.google.gson.annotations.SerializedName;
import java.awt.Color;
import java.util.function.Supplier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public final class MenuButtonWidget extends PressableWidget {
   public static final MenuButtonWidget.Inner INNER = Supplier::get;
   @SerializedName("palette")
   public final ColorAnimation palette = new ColorAnimation(
      new ColorPalette(new Color(32, 32, 32, 150).getRGB(), new Color(255, 255, 255, 100).getRGB(), new Color(21, 21, 21, 205).getRGB()), 200
   );
   @SerializedName("pressAction")
   public final MenuButtonWidget.Inner3 pressAction;
   @SerializedName("narration")
   public final MenuButtonWidget.Inner narration;
   @SerializedName("alignment")
   public final MenuButtonWidget.Mode alignment;

   public MenuButtonWidget(
      int n, int n2, int n3, int n4, MenuButtonWidget.Mode mode, Text text, MenuButtonWidget.Inner3 inner3, MenuButtonWidget.Inner inner
   ) {
      super(n, n2, n3, n4, text);
      this.pressAction = inner3;
      this.narration = inner;
      this.alignment = mode;
   }

   public static MenuButtonWidget.Inner2 getInner2(Text text, MenuButtonWidget.Inner3 inner3, MenuButtonWidget.Mode mode) {
      return new MenuButtonWidget.Inner2(text, inner3, mode);
   }

   public static boolean isValid(int n, int n2, int n3, int n4, int n5, int n6) {
      return n5 >= n && n5 <= n + n3 && n6 >= n2 && n6 <= n2 + n4;
   }

   protected void renderWidget(DrawContext context, int n, int n2, float f) {
      if (this.visible) {
         NanoVGHelper nvg = NanoVGHelper.INSTANCE;
         short s = 31641;
         ScissorHelper scissor = ScissorHelper.INSTANCE;
         long l = 5635719289123696454L;
         int n3 = Math.max(10, Math.round(this.height * 0.4F));
         s = 24841;
         float f2 = this.height * 0.22F;
         l = 9015531273676944859L;
         int n4 = Math.max(6, Math.round(this.height * 0.3F));
         boolean isValid = isValid(this.getX(), this.getY(), this.width, this.height, n, n2);
         int n5 = this.computeInt(isValid);
         int n6 = this.active ? new Color(255, 255, 255, 190).getRGB() : new Color(255, 255, 255, 90).getRGB();
         nvg.performAction(
            true,
            l2 -> {
               float textWidth = nvg.getTextWidth(l2, this.getMessage().getString(), n3, OneConfigFonts.REGULAR);
               nvg.drawRoundedRect(l2, this.getX(), this.getY(), this.width, this.height, n5, f2);
               switch (this.alignment) {
                  case LEFT:
                     nvg.drawTextEx(
                        l2,
                        this.getMessage().getString(),
                        this.getX() + this.height / 2.0F,
                        this.getY() + this.height / 2.0F,
                        n6,
                        n3,
                        OneConfigFonts.REGULAR
                     );
                     scissor.scissor(l2, this.getX(), this.getY(), n4, this.height);
                     nvg.drawRoundedRect(l2, this.getX(), this.getY(), this.width, this.height, n5, f2);
                     scissor.clearScissors(l2);
                     break;
                  case CENTER:
                     nvg.drawTextAlt(
                        l2,
                        this.getMessage().getString(),
                        this.getX() + this.width / 2.0F,
                        this.getY() + this.height / 2.0F,
                        n6,
                        n3,
                        OneConfigFonts.REGULAR
                     );
                     break;
                  case RIGHT:
                     nvg.drawTextEx(
                        l2,
                        this.getMessage().getString(),
                        this.getX() + this.width - this.height / 2.0F - textWidth,
                        this.getY() + this.height / 2.0F,
                        n6,
                        n3,
                        OneConfigFonts.REGULAR
                     );
                     scissor.scissor(l2, this.getX() + this.width - n4, this.getY(), n4, this.height);
                     nvg.drawRoundedRect(l2, this.getX(), this.getY(), this.width, this.height, n5, f2);
                     scissor.clearScissors(l2);
               }
            }
         );
      }
   }

   private int computeInt(boolean flag) {
      int n = this.palette.computeInt(flag, this.isFocused());
      if (this.active) {
         return n;
      }

      Color color = new Color(n, true);
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.min(120, color.getAlpha() / 2)).getRGB();
   }

   public void onPress() {
      this.pressAction.performAction(this);
   }

   public boolean mouseClicked(double d, double d2, int n) {
      if (this.active && this.visible && isValid(this.getX(), this.getY(), this.width, this.height, (int)d, (int)d2)) {
         this.onClick(d, d2);
         return true;
      } else {
         return false;
      }
   }

   protected MutableText getNarrationMessage() {
      return this.narration.getMutableText(() -> super.getNarrationMessage());
   }

   protected void appendClickableNarrations(NarrationMessageBuilder narrationMessageBuilder) {
      this.appendDefaultNarrations(narrationMessageBuilder);
   }

   public interface Inner {
      MutableText getMutableText(Supplier<MutableText> supplier);
   }

   public static class Inner2 {
      @SerializedName("text")
      public final Text text;
      @SerializedName("pressAction")
      public final MenuButtonWidget.Inner3 pressAction;
      @SerializedName("tooltip")
      @Nullable
      public Tooltip tooltip;
      @SerializedName("posX")
      public int posX;
      @SerializedName("posY")
      public int posY;
      @SerializedName("width")
      public int width = 150;
      @SerializedName("height")
      public int height = 20;
      @SerializedName("alignment")
      public MenuButtonWidget.Mode alignment;
      @SerializedName("narration")
      public MenuButtonWidget.Inner narration = MenuButtonWidget.INNER;

      public Inner2(Text text2, MenuButtonWidget.Inner3 inner3, MenuButtonWidget.Mode mode) {
         this.text = text2;
         this.pressAction = inner3;
         this.alignment = mode;
      }

      public MenuButtonWidget.Inner2 getInner2(Tooltip tooltip2) {
         this.tooltip = tooltip2;
         return this;
      }

      public MenuButtonWidget.Inner2 getInner2(MenuButtonWidget.Inner inner) {
         this.narration = inner;
         return this;
      }

      public MenuButtonWidget.Inner2 getInner2(int n) {
         this.width = n;
         return this;
      }

      public MenuButtonWidget.Inner2 getInner2(int n, int n2) {
         this.posX = n;
         this.posY = n2;
         return this;
      }

      public MenuButtonWidget.Inner2 getInner2Ex(int n, int n2) {
         this.width = n;
         this.height = n2;
         return this;
      }

      public MenuButtonWidget.Inner2 getInner2Ex(int n, int n2, int n3, int n4) {
         return this.getInner2(n, n2).getInner2Ex(n3, n4);
      }

      public MenuButtonWidget.Inner2 getInner2(MenuButtonWidget.Mode mode) {
         this.alignment = mode;
         return this;
      }

      public MenuButtonWidget getPaletteUtil2() {
         MenuButtonWidget menuButtonWidget = new MenuButtonWidget(
            this.posX, this.posY, this.width, this.height, this.alignment, this.text, this.pressAction, this.narration
         );
         menuButtonWidget.setTooltip(this.tooltip);
         return menuButtonWidget;
      }
   }

   public interface Inner3 {
      void performAction(MenuButtonWidget menuButtonWidget);
   }

   public enum Mode {
      LEFT,
      CENTER,
      RIGHT;
   }
}
