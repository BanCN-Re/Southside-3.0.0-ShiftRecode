package dev.southside.utils;

import cc.polyfrost.oneconfig.internal.config.OneConfigConfig;
import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

public class ModCard extends BasicElement {
   @SerializedName("modData")
   private final ModData modData;
   @SerializedName("favoriteButton")
   private final BasicButton favoriteButton = new BasicButton(32, 32, OneConfigSvgs.HEART_OUTLINE, 2, ColorPalette.INSTANCE4);
   @SerializedName("colorFrame")
   private final ColorAnimation colorFrame = new ColorAnimation(ColorPalette.INSTANCE3);
   @SerializedName("colorToggle")
   private final ColorAnimation colorToggle;
   @SerializedName("active")
   private boolean active;
   @SerializedName("disabled")
   private boolean disabled;
   @SerializedName("favorite")
   private boolean favorite;
   @SerializedName("isHoveredMain")
   private boolean isHoveredMain = false;
   @SerializedName("page")
   private final ModsPage page;

   public ModCard(@NotNull ModData modData2, boolean flag, boolean flag2, boolean flag3, ModsPage modsPage) {
      super(244, 119, false);
      this.modData = modData2;
      this.active = flag;
      super.toggled = flag;
      this.colorToggle = new ColorAnimation(flag ? ColorPalette.INSTANCE2 : ColorPalette.INSTANCE3);
      this.disabled = flag2;
      this.favorite = flag3;
      this.favoriteButton.setToggled(flag3);
      this.page = modsPage;
   }

   @Override
   public void draw(long l, float f, float f2, InputHandler input) {
      super.performAction(f, f2, input);
      ScissorHelper scissor = ScissorHelper.INSTANCE;
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;
      String text = this.modData.name.replaceAll("§.", "");
      ScissorRect clip = scissor.scissor(l, f, f2, super.width, super.height);
      this.isHoveredMain = input.isHovered(f, f2, super.width, 87.0F);
      boolean flag = input.isHovered(f, f2 + 87.0F, super.width - 32, 32.0F) && !this.disabled;
      if (this.disabled) {
         nvg.setAlpha(l, 0.5F);
      }

      nvg.drawRoundedRectVaried(
         l,
         f,
         f2,
         super.width,
         87.0F,
         this.colorFrame.computeInt(this.isHoveredMain, this.isHoveredMain && OneConfigPlatform.getMousePlatform().isKeyDown(0)),
         12.0F,
         12.0F,
         0.0F,
         0.0F
      );
      nvg.drawRoundedRectVaried(
         l,
         f,
         f2 + 87.0F,
         super.width,
         32.0F,
         this.colorToggle.computeInt(flag, flag && OneConfigPlatform.getMousePlatform().isKeyDown(0)),
         0.0F,
         0.0F,
         12.0F,
         12.0F
      );
      nvg.drawLine(l, f, f2 + 86.0F, f + super.width, f2 + 86.0F, 2.0F, OneConfigColors.GRAY_300);
      this.performAction(nvg, l, f, f2, text);
      this.favoriteButton.draw(l, f + 212.0F, f2 + 87.0F, input);
      this.favorite = this.favoriteButton.isToggled();
      if (this.favoriteButton.isClicked()) {
         if (this.favorite) {
            OneConfigConfig.favoriteMods.add(this.modData.name);
         } else {
            OneConfigConfig.favoriteMods.remove(this.modData.name);
         }

         ConfigCore.performActionImpl();
         this.page.performActionEx();
      }

      ScissorRect clip2 = scissor.scissor(l, f, f2 + 87.0F, super.width - 32, 32.0F);
      nvg.drawTextEx(
         l,
         text,
         f + 12.0F,
         f2 + 103.0F,
         ArgbColorUtils.withAlpha(OneConfigColors.WHITE, (int)(this.colorToggle.getProgress() * 255.0F)),
         14.0F,
         OneConfigFonts.ONE_CONFIG_FONT
      );
      scissor.resetScissor(l, clip2);
      if (this.favorite) {
         this.favoriteButton.setIcon1(OneConfigSvgs.HEART_FILL);
      } else {
         this.favoriteButton.setIcon1(OneConfigSvgs.HEART_OUTLINE);
      }

      if (super.clicked && this.isHoveredMain && !this.active) {
         super.toggled = false;
      }

      if (super.clicked && this.favoriteButton.hovered) {
         super.toggled = false;
      }

      if (super.clicked && !flag && this.active) {
         super.toggled = true;
      }

      if (!this.active && this.disabled) {
         super.toggled = false;
      }

      if (this.active != super.toggled) {
         this.active = super.toggled;
         if (this.modData.config.canToggle) {
            this.colorToggle.performAction(this.active ? ColorPalette.INSTANCE2 : ColorPalette.INSTANCE3);
            this.modData.config.setConfigEnabled(this.active);
            this.modData.config.saveToJson();
         } else {
            super.toggled = this.active = true;
         }
      }

      nvg.setAlpha(l, 1.0F);
      scissor.resetScissor(l, clip);
   }

   protected void performAction(NanoVGHelper nvg, long l, float f, float f2, String text) {
      int n = this.modData.iconWidth == -1 ? 56 : this.modData.iconWidth;
      int n2 = this.modData.iconHeight == -1 ? n : this.modData.iconHeight;
      if (this.modData.modIcon != null) {
         if (this.modData.modIcon.toLowerCase().endsWith(".svg")) {
            nvg.drawEx(l, this.modData.modIcon, f + 122.0F - n / 2.0F, f2 + 43.0F - n2 / 2.0F, n, n2);
         } else {
            nvg.draw(l, this.modData.modIcon, f + 122.0F - n / 2.0F, f2 + 43.0F - n2 / 2.0F, n, n2, 12.0F);
         }
      } else {
         nvg.drawTextEx(
            l,
            text,
            f + Math.max(0.0F, 244.0F - nvg.getTextWidth(l, text, 16.0F, OneConfigFonts.MINECRAFT_BOLD)) / 2.0F,
            f2 + 44.0F,
            ArgbColorUtils.withAlpha(OneConfigColors.WHITE, (int)(this.colorFrame.getProgress() * 255.0F)),
            16.0F,
            OneConfigFonts.MINECRAFT_BOLD
         );
      }
   }

   @Override
   public void performAction() {
      if (this.isHoveredMain) {
         if (this.modData instanceof ConfigLoader.Inner) {
            Runnable action = ConfigLoader.runnableMap.get(this.modData);
            if (action != null) {
               action.run();
               return;
            }
         }

         OneConfigGui.INSTANCE.performAction(new ModConfigPage(this.modData.defaultPage));
      }
   }

   public ModData getModData() {
      return this.modData;
   }

   @Override
   public boolean isDisabled() {
      return this.disabled;
   }

   public void setDisabled2(boolean flag) {
      this.disabled = flag;
   }

   public boolean isActive() {
      return this.active;
   }

   public boolean isFavorite() {
      return this.favorite;
   }
}
