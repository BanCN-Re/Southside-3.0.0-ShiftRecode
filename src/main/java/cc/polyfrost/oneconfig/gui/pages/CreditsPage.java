package cc.polyfrost.oneconfig.gui.pages;

import dev.southside.utils.InputHandler;
import dev.southside.utils.OneConfigColors;
import dev.southside.utils.OneConfigSvgs;
import dev.southside.utils.OneConfigFonts;
import dev.southside.utils.NanoVGHelper;
import dev.southside.utils.GuiPage;

public class CreditsPage extends GuiPage {
   public CreditsPage() {
      super("Credits");
   }

   @Override
   public void draw(long vg, int x, int y, InputHandler input) {
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;
      nvg.draw(vg, OneConfigSvgs.ICON_WHITE, x + 15.0F, y, 110.0F, 110.0F);
      y += 10;
      nvg.drawTextEx(vg, "OneConfig 兼容层编写团队", x + 20, y + 110, OneConfigColors.WHITE, 24.0F, OneConfigFonts.SEMIBOLD);
      nvg.drawTextEx(vg, " - 花花派对 & ...", x + 20, y + 135, OneConfigColors.WHITE, 12.0F, OneConfigFonts.REGULAR);
      nvg.drawTextEx(vg, "版权说明", x + 20, y + 175, OneConfigColors.WHITE, 24.0F, OneConfigFonts.SEMIBOLD);
      nvg.drawTextEx(
         vg,
         "   OneConfig 以 LGPL 协议开源于 https://github.com/Polyfrost/OneConfig。",
         x + 20,
         y + 200,
         OneConfigColors.WHITE,
         12.0F,
         OneConfigFonts.REGULAR
      );
      nvg.drawTextEx(
         vg, "   SouthsideNG 团队以编写 fabric 兼容层的方法给客户端导入了 OneConfig 作为配置管理系统。", x + 20, y + 215, OneConfigColors.WHITE, 12.0F, OneConfigFonts.REGULAR
      );
      nvg.drawTextEx(vg, "   符合 LGPL 的标准，仅仅用到了 OneConfig 的功能，故可保持本软件闭源。", x + 20, y + 230, OneConfigColors.WHITE, 12.0F, OneConfigFonts.REGULAR);
      y += 240;
      nvg.draw(vg, OneConfigSvgs.ONE_CONFIG_FULL_DARK, x + 15.0F, y + 20.0F, 474.0F, 102.0F);
      y -= 52;
      nvg.drawTextEx(
         vg,
         " - Wyvest - OG Team - Gradle, NanoVGHelper, VCAL, Utilities, GUI Frontend",
         x + 20,
         y + 205,
         OneConfigColors.WHITE,
         12.0F,
         OneConfigFonts.REGULAR
      );
      nvg.drawTextEx(vg, " - Caledonian - Designer", x + 20, y + 220, OneConfigColors.WHITE, 12.0F, OneConfigFonts.REGULAR);
      nvg.drawTextEx(
         vg,
         " - nextdaydelivery - OG Team - GUI Frontend, NanoVGHelper, Utilities",
         x + 20,
         y + 235,
         OneConfigColors.WHITE,
         12.0F,
         OneConfigFonts.REGULAR
      );
      nvg.drawTextEx(vg, " - Pauline - Utilities", x + 20, y + 250, OneConfigColors.WHITE, 12.0F, OneConfigFonts.REGULAR);
      nvg.drawTextEx(
         vg,
         " - DeDiamondPro - OG Team -  Config Backend, GUI Frontend, HUD",
         x + 20,
         y + 265,
         OneConfigColors.WHITE,
         12.0F,
         OneConfigFonts.REGULAR
      );
      nvg.drawTextEx(vg, " - xtrm - Multiversion support, GUI Frontend", x + 20, y + 280, OneConfigColors.WHITE, 12.0F, OneConfigFonts.REGULAR);
      nvg.drawTextEx(vg, " - MoonTidez - OG Team - Designer", x + 20, y + 295, OneConfigColors.WHITE, 12.0F, OneConfigFonts.REGULAR);
      nvg.drawTextEx(vg, "Libraries", x + 20, y + 333, OneConfigColors.WHITE, 24.0F, OneConfigFonts.SEMIBOLD);
      nvg.drawTextEx(
         vg, " - #getResourceAsStream (SpinyOwl) - IO Utility and shadow", x + 20, y + 355, OneConfigColors.WHITE, 12.0F, OneConfigFonts.REGULAR
      );
      nvg.drawTextEx(vg, " - NanoVG (memononen) - NanoVG Library", x + 20, y + 370, OneConfigColors.WHITE, 12.0F, OneConfigFonts.REGULAR);
      nvg.drawTextEx(vg, " - https://easings.net/ - Easing functions", x + 20, y + 385, OneConfigColors.WHITE, 12.0F, OneConfigFonts.REGULAR);
   }

   @Override
   public boolean isBase() {
      return true;
   }
}
