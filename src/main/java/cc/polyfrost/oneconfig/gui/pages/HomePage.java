package cc.polyfrost.oneconfig.gui.pages;

import dev.southside.utils.InputHandler;
import dev.southside.utils.OneConfigColors;
import dev.southside.utils.OneConfigSvgs;
import dev.southside.utils.OneConfigFonts;
import dev.southside.utils.NanoVGHelper;
import dev.southside.utils.GuiPage;

public class HomePage extends GuiPage {
   public HomePage() {
      super("Home Dashboard");
   }

   @Override
   public void draw(long vg, int x, int y, InputHandler input) {
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;
      nvg.drawRoundedRect(vg, x, y, 184.0F, 36.0F, OneConfigColors.WHITE, 12.0F);
      nvg.drawTextEx(vg, "This is a cool string to test pages", x + 32, y + 72, OneConfigColors.WHITE, 36.0F, OneConfigFonts.BOLD);
      nvg.drawRoundedRect(vg, x + 350, y + 310, 300.0F, 200.0F, OneConfigColors.PRIMARY_500, 14.0F);
      nvg.draw(vg, OneConfigSvgs.INFO_CIRCLE, x + 20, y + 604, 24.0F, 24.0F);
      nvg.drawTextEx(vg, "Info", x + 52, y + 618, OneConfigColors.WHITE_90, 24.0F, OneConfigFonts.ONE_CONFIG_FONT);
      nvg.drawRoundedRect(vg, x + 16, y + 644, 1024.0F, 64.0F, OneConfigColors.GRAY_700, 20.0F);
      nvg.draw(vg, x + 100, y + 205, 24.0F, OneConfigFonts.ONE_CONFIG_FONT, input);
   }

   @Override
   public boolean isBase() {
      return true;
   }
}
