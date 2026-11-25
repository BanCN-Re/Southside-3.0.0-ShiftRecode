package cc.polyfrost.oneconfig.internal.renderer;

import dev.southside.utils.OneConfigFont;
import dev.southside.utils.OneConfigFonts;
import dev.southside.utils.FontHelper;
import org.lwjgl.nanovg.NanoVG;

public class FontHelperImpl implements FontHelper {
   @Override
   public void initialize(long l) {
      for (OneConfigFont font : OneConfigFonts.oneConfigFonts) {
         try {
            this.loadFont(l, font);
         } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Could not initialize fonts");
         }
      }

      if (OneConfigFonts.WQY_MICROHEI.isLoaded()) {
         for (OneConfigFont font2 : OneConfigFonts.oneConfigFonts) {
            if (font2 != OneConfigFonts.WQY_MICROHEI && font2.isLoaded()) {
               NanoVG.nvgAddFallbackFontId(l, font2.getHandle(), OneConfigFonts.WQY_MICROHEI.getHandle());
            }
         }
      }
   }

   @Override
   public void loadFont(long l, OneConfigFont font) {
      if (!font.isLoaded()) {
         font.setHandle(NanoVG.nvgCreateFontMem(l, font.getName(), font.getBuffer(), false));
         if (font.getHandle() == -1) {
            throw new RuntimeException("Failed to initialize font " + font.getName());
         }

         font.setLoaded(true);
      }
   }
}
