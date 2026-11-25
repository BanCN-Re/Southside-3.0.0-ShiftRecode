package dev.southside.utils;

import cc.polyfrost.oneconfig.internal.config.OneConfigConfig;
import com.google.gson.annotations.SerializedName;
import dev.southside.module.Category;
import java.util.ArrayList;

public class ModsPage extends GuiPage {
   @SerializedName("modCards")
   private final ArrayList<ModCard> modCards = new ArrayList<>();
   @SerializedName("size")
   private int size;
   @SerializedName("selected")
   private int selected;

   public ModsPage(int n) {
      super(getCategory(n).name());
      this.performActionEx();
      this.selected = n;
   }

   @Override
   public void draw(long vg, int x, int y, InputHandler input) {
      String text = OneConfigGui.INSTANCE == null ? "" : OneConfigGui.INSTANCE.getInput().toLowerCase().trim();
      int n = x + 16;
      int n2 = y + 16;

      for (ModCard modCard : new ArrayList<>(this.modCards)) {
         if (this.isValid(modCard) && (text.equals("") || FuzzySearchUtils.isValid(modCard.getModData().name, text))) {
            if (n2 + 135 >= y - super.scroll && n2 <= y + 728 - super.scroll) {
               modCard.draw(vg, n, n2, input);
            }

            n += 260;
            if (n > x + 796) {
               n = x + 16;
               n2 += 135;
            }
         }
      }

      this.size = n2 - y + 135;
      if (n == x + 16 && n2 == y + 72) {
         NanoVGHelper.INSTANCE
            .drawTextEx(
               vg, "Looks like there is nothing here. Try another category?", x + 16, y + 72, OneConfigColors.WHITE_60, 14.0F, OneConfigFonts.ONE_CONFIG_FONT
            );
      }
   }

   @Override
   public int drawAndGetHeight(long vg, int x, int y, InputHandler input) {
      return 0;
   }

   private boolean isValid(ModCard modCard) {
      return this.selected >= 0 && this.selected <= 6 && modCard.getModData().modType == getCategory(this.selected);
   }

   private static Category getCategory(int n) {
      return switch (n) {
         case 1 -> Category.Combat;
         case 2 -> Category.Misc;
         case 3 -> Category.Movement;
         case 4 -> Category.Player;
         case 5 -> Category.Render;
         case 6 -> Category.World;
         default -> Category.Client;
      };
   }

   public void performActionEx() {
      this.modCards.clear();

      for (ModData modData : ConfigCore.modDatas) {
         ModCard modCard = new ModCard(
            modData, modData.config == null || modData.config.isEnabled(), false, OneConfigConfig.favoriteMods.contains(modData.name), this
         );
         modData.config.addToggleListener(modCard::setToggled);
         this.modCards.add(modCard);
      }
   }

   @Override
   public int getSize() {
      return this.size;
   }

   @Override
   public boolean isBase() {
      return true;
   }
}
