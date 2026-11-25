package dev.southside.utils;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.elements.BasicOption;
import cc.polyfrost.oneconfig.gui.elements.ColorSelector;
import com.google.gson.annotations.SerializedName;
import java.lang.reflect.Field;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class ConfigColorOption extends BasicOption {
   @SerializedName("element")
   private final BasicElement element = new BasicElement(64, 32, false);
   @SerializedName("allowAlpha")
   private final boolean allowAlpha;
   @SerializedName("colorSelector")
   private ColorSelector colorSelector;
   @SerializedName("open")
   private boolean open;

   public ConfigColorOption(Field field, Object obj, String text, String text2, String text3, String text4, int n, boolean flag) {
      super(field, obj, text, text2, text3, text4, n);
      this.allowAlpha = flag;
   }

   @Override
   public int getHeight() {
      return 32;
   }

   @Override
   public void draw(long vg, int x, int y, InputHandler input) {
      if (OneConfigGui.INSTANCE != null) {
         NanoVGHelper nvg = NanoVGHelper.INSTANCE;
         if (!this.isValidEx()) {
            nvg.setAlpha(vg, 0.5F);
         }

         this.element.setDisabled(!this.isValidEx());
         int n = this.size == 1 ? x : x + 512;

         OneColor color;
         try {
            color = (OneColor)this.resolveValue();
         } catch (IllegalAccessException e) {
            return;
         }

         nvg.drawTextEx(vg, this.name, x, y + 16, super.nameColor, 14.0F, OneConfigFonts.ONE_CONFIG_FONT);
         this.element.performAction(n + 416, y, input);
         nvg.drawRoundedRect(vg, n + 415, y - 1, 64.0F, 32.0F, OneConfigColors.GRAY_300, 12.0F, 2.0F);
         nvg.drawRoundedRect(vg, OneConfigImages.ALPHA_GRID.filePath, n + 420, y + 4, 56.0F, 24.0F, 8.0F, this.getClass());
         nvg.drawRoundedRect(vg, n + 420, y + 4, 56.0F, 24.0F, color.fromHsb(), 8.0F);
         if (this.element.isClicked() && !this.open) {
            OneColor color2 = new OneColor(color.fromHsb());
            new FrameDelayTask(() -> this.performAction(color2, input));
         }

         if (OneConfigGui.INSTANCE.currentColorSelector != this.colorSelector) {
            this.open = false;
         } else if (this.open) {
            try {
               if (!OneConfigGui.INSTANCE.getOneColor().blendColor().equals(((OneColor)this.field.get(super.parent)).blendColor())) {
                  ((OneColor)this.field.get(super.parent)).performAction(OneConfigGui.INSTANCE.getOneColor());
                  this.performAction();
               }
            } catch (IllegalAccessException e2) {
            }
         }

         nvg.setAlpha(vg, 1.0F);
      }
   }

   protected void performAction(OneColor color) {
      try {
         super.performAction((Object)color);
      } catch (IllegalAccessException e) {
         if (MinecraftClient.getInstance().inGameHud != null) {
            ChatHud chatHud = MinecraftClient.getInstance().inGameHud.getChatHud();
            MutableText text = Text.empty().append(IText.TEXT);
            chatHud.addMessage(text.append(Text.literal("?")));
         }
      }
   }

   private void performAction(OneColor color, InputHandler input) {
      this.open = true;
      this.colorSelector = new ColorSelector(color, input.getProgress(), input.getProgressEx(), this.allowAlpha, input);
      OneConfigGui.INSTANCE.performAction(this.colorSelector);
   }
}
