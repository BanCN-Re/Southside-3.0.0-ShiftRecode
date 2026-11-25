package cc.polyfrost.oneconfig.gui;

import cc.polyfrost.oneconfig.gui.pages.CreditsPage;
import cc.polyfrost.oneconfig.internal.config.Preferences;
import cc.polyfrost.oneconfig.internal.gui.HudGui;
import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import com.google.gson.annotations.SerializedName;
import dev.southside.utils.InputHandler;
import dev.southside.utils.MusicPlayerPage;
import dev.southside.utils.OneConfigColors;
import dev.southside.utils.OneConfigSvgs;
import dev.southside.utils.GuiUtils;
import dev.southside.utils.OneConfigFonts;
import dev.southside.utils.NanoVGHelper;
import dev.southside.utils.ModsPage;
import dev.southside.utils.ModConfigPage;
import dev.southside.utils.UiAnimation;
import dev.southside.utils.LinearAnimation;
import dev.southside.utils.BasicButton;
import dev.southside.utils.DummyAnimation;
import java.util.ArrayList;
import java.util.List;

public class SideBar {
   @SerializedName("buttons")
   private final List<BasicButton> buttons = new ArrayList<BasicButton>() {
      {
         short s = 22098;
         long l = 8686172312255477878L;
         short s2 = 192;
         this.add(new BasicButton(s2, 36, "Credits", OneConfigSvgs.COPYRIGHT_FILL, null, 0, ColorPalette.INSTANCE4));
         this.add(new BasicButton(s2, 36, "Client", OneConfigSvgs.CLIENT, null, 0, ColorPalette.INSTANCE2));
         this.add(new BasicButton(s2, 36, "Combat", OneConfigSvgs.COMBAT, null, 0, ColorPalette.INSTANCE4));
         this.add(new BasicButton(s2, 36, "Misc", OneConfigSvgs.MISC, null, 0, ColorPalette.INSTANCE4));
         this.add(new BasicButton(s2, 36, "Movement", OneConfigSvgs.MOVEMENT, null, 0, ColorPalette.INSTANCE4));
         this.add(new BasicButton(s2, 36, "Player", OneConfigSvgs.PLAYER, null, 0, ColorPalette.INSTANCE4));
         this.add(new BasicButton(s2, 36, "Render", OneConfigSvgs.RENDER, null, 0, ColorPalette.INSTANCE4));
         this.add(new BasicButton(s2, 36, "World", OneConfigSvgs.WORLD, null, 0, ColorPalette.INSTANCE4));
         this.add(new BasicButton(s2, 36, "Preferences", OneConfigSvgs.SETTINGS_02, null, 0, ColorPalette.INSTANCE4));
         this.add(new BasicButton(s2, 36, "Music", OneConfigSvgs.Inner.MUSIC, null, 0, ColorPalette.INSTANCE4));
      }
   };
   @SerializedName("hudButton")
   private final BasicButton hudButton = new BasicButton(192, 36, "Edit HUD", OneConfigSvgs.LAYOUT_ALT, null, 0, ColorPalette.INSTANCE4);
   @SerializedName("closeButton")
   private final BasicButton closeButton = new BasicButton(192, 36, "Close", OneConfigSvgs.X_CLOSE, null, 0, ColorPalette.INSTANCE7);
   @SerializedName("selected")
   private int selected = 1;
   @SerializedName("moveAnimation")
   private UiAnimation moveAnimation = null;
   @SerializedName("sizeAnimation")
   private UiAnimation sizeAnimation = null;
   @SerializedName("y")
   private int y;
   @SerializedName("sidebarY")
   private int sidebarY;

   public SideBar() {
      this.buttons.get(0).setPage(new CreditsPage());

      for (int i = 1; i <= 7; i++) {
         int n = i - 1;
         this.buttons.get(i).setPage(new ModsPage(n));
      }

      this.buttons.get(8).setPage(new ModConfigPage(Preferences.getPreferences().modData.defaultPage, true));
      this.buttons.get(9).setPage(new MusicPlayerPage());
      this.hudButton.setRunnable(() -> GuiUtils.openScreen(new HudGui()));
      this.closeButton.setRunnable(GuiUtils::performAction);

      for (BasicButton basicButton : this.buttons) {
         if (!basicButton.isValid()) {
            basicButton.setDisabled(true);
         }
      }
   }

   public void draw(long vg, int x, int y2, InputHandler input) {
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;
      this.y = y2;

      for (BasicButton basicButton : this.buttons) {
         if (basicButton.isClicked()) {
            this.performAction(basicButton, false);
            break;
         }
      }

      if (this.moveAnimation != null) {
         nvg.drawRoundedRect(
            vg,
            x + 16,
            y2 + this.moveAnimation.getProgress() - (this.sizeAnimation.getProgress() - 36.0F) / 2.0F,
            192.0F,
            this.sizeAnimation.evaluate(0.0F),
            OneConfigColors.PRIMARY_500,
            12.0F
         );
         if (this.moveAnimation.isValid() && this.sizeAnimation.isValid()) {
            this.moveAnimation = null;
            this.sizeAnimation = null;
            this.buttons.get(this.selected).performAction(ColorPalette.INSTANCE2);
         }
      }

      this.sidebarY = y2 + 44;
      this.buttons.get(0).draw(vg, x + 16, this.computeInt(this.sidebarY), input);
      nvg.drawTextEx(vg, "MOD CONFIG", x + 16, this.computeInt(this.sidebarY + 26), OneConfigColors.WHITE_50, 12.0F, OneConfigFonts.SEMIBOLD);
      this.sidebarY -= 26;
      this.buttons.get(1).draw(vg, x + 16, this.computeInt(this.sidebarY), input);
      this.buttons.get(2).draw(vg, x + 16, this.computeInt(this.sidebarY), input);
      this.buttons.get(3).draw(vg, x + 16, this.computeInt(this.sidebarY), input);
      this.buttons.get(4).draw(vg, x + 16, this.computeInt(this.sidebarY), input);
      this.buttons.get(5).draw(vg, x + 16, this.computeInt(this.sidebarY), input);
      this.buttons.get(6).draw(vg, x + 16, this.computeInt(this.sidebarY), input);
      this.buttons.get(7).draw(vg, x + 16, this.computeInt(this.sidebarY), input);
      nvg.drawTextEx(vg, "PERSONALIZATION", x + 16, this.computeInt(this.sidebarY + 26), OneConfigColors.WHITE_50, 12.0F, OneConfigFonts.SEMIBOLD);
      this.sidebarY -= 26;
      this.buttons.get(8).draw(vg, x + 16, this.computeInt(this.sidebarY), input);
      this.buttons.get(9).draw(vg, x + 16, this.computeInt(this.sidebarY), input);
      this.sidebarY = 0;
      this.hudButton.draw(vg, x + 16, y2 + 704, input);
      this.closeButton.draw(vg, x + 16, y2 + 748, input);
   }

   public void applyText(String text) {
      this.applyText(text, false);
   }

   public void applyText(String text, boolean flag) {
      for (BasicButton basicButton : this.buttons) {
         if (basicButton.getText().equalsIgnoreCase(text)) {
            this.performAction(basicButton, flag);
            return;
         }
      }
   }

   private void performAction(BasicButton basicButton) {
      this.performAction(basicButton, false);
   }

   private void performAction(BasicButton basicButton, boolean flag) {
      if (!basicButton.equals(this.buttons.get(this.selected))) {
         this.buttons.get(this.selected).performAction(ColorPalette.INSTANCE4);
         if (flag) {
            this.moveAnimation = new DummyAnimation(basicButton.y - this.y);
         } else {
            this.moveAnimation = new LinearAnimation(300, this.buttons.get(this.selected).y - this.y, basicButton.y - this.y, false);
         }

         this.sizeAnimation = new DummyAnimation(36.0F);
         this.selected = this.buttons.indexOf(basicButton);
      }
   }

   private int computeInt(int n) {
      this.sidebarY = n + 36;
      return this.sidebarY;
   }
}
