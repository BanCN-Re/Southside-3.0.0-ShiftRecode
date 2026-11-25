package dev.southside.utils;

import cc.polyfrost.oneconfig.config.elements.BasicOption;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OptionSubcategory {
   @SerializedName("name")
   private final String name;
   @SerializedName("category")
   private final String category;
   @SerializedName("options")
   public ArrayList<BasicOption> options = new ArrayList<>();
   @SerializedName("topButtons")
   public ArrayList<ConfigPageOption> topButtons = new ArrayList<>();
   @SerializedName("bottomButtons")
   public ArrayList<ConfigPageOption> bottomButtons = new ArrayList<>();
   @SerializedName("filteredOptions")
   private List<BasicOption> filteredOptions = new ArrayList<>();
   @SerializedName("drawLastY")
   private int drawLastY;

   public OptionSubcategory(String text, String text2) {
      this.name = text;
      this.category = text2;
   }

   public String getName() {
      return this.name;
   }

   public int drawAndGetHeight(long vg, int x, int y, InputHandler input) {
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;
      String text = OneConfigGui.INSTANCE == null ? "" : OneConfigGui.INSTANCE.getInput().toLowerCase().trim();
      boolean flag = text.isEmpty() || FuzzySearchUtils.isValid(this.category, text) || FuzzySearchUtils.isValid(this.name, text);
      this.filteredOptions = this.options
         .stream()
         .filter(option -> !option.isValidAlt() && (flag || FuzzySearchUtils.isValid(option.name, text)))
         .collect(Collectors.toList());
      List list = this.topButtons.stream().filter(configPageOption -> isValid(flag, text, configPageOption)).collect(Collectors.toList());
      List list2 = this.bottomButtons.stream().filter(configPageOption2 -> isValid(flag, text, configPageOption2)).collect(Collectors.toList());
      if (this.filteredOptions.isEmpty() && list.isEmpty() && list2.isEmpty()) {
         return 0;
      }

      int n = y;
      if (!this.name.isEmpty()) {
         nvg.drawTextEx(vg, this.name, x, y + 12, OneConfigColors.WHITE_90, 24.0F, OneConfigFonts.ONE_CONFIG_FONT);
         n = y + 36;
      }

      for (Object obj : list) {
         ConfigPageOption configPageOption3 = (ConfigPageOption)obj;
         configPageOption3.draw(vg, x, n, input);
         n += configPageOption3.getHeight() + 16;
      }

      if (!this.filteredOptions.isEmpty()) {
         int n2 = this.computeInt();
         nvg.drawRoundedRect(vg, x - 16, n, 1024.0F, n2, OneConfigColors.GRAY_900, 20.0F);
         n += 16;
      }

      this.drawLastY = n;
      if (!this.filteredOptions.isEmpty()) {
         for (int i = 0; i < this.filteredOptions.size(); i++) {
            BasicOption option2 = this.filteredOptions.get(i);
            option2.draw(vg, x, n, input);
            if (i + 1 < this.filteredOptions.size()) {
               BasicOption option3 = this.filteredOptions.get(i + 1);
               if (option2.size == 1 && option3.size == 1) {
                  option3.draw(vg, x + 512, n, input);
                  n += Math.max(option2.getHeight(), option3.getHeight()) + 16;
                  i++;
                  continue;
               }
            }

            n += option2.getHeight() + 16;
         }

         n += 16;
      }

      for (Object obj2 : list2) {
         ConfigPageOption configPageOption4 = (ConfigPageOption)obj2;
         configPageOption4.draw(vg, x, n, input);
         n += configPageOption4.getHeight() + 16;
      }

      return n - y;
   }

   private int computeInt() {
      int n = 16;

      for (int i = 0; i < this.filteredOptions.size(); i++) {
         BasicOption option = this.filteredOptions.get(i);
         if (i + 1 < this.filteredOptions.size()) {
            BasicOption option2 = this.filteredOptions.get(i + 1);
            if (option.size == 1 && option2.size == 1) {
               n += Math.max(option.getHeight(), option2.getHeight()) + 16;
               i++;
               continue;
            }
         }

         n += option.getHeight() + 16;
      }

      return n;
   }

   public void draw(long l, int n, InputHandler input) {
      for (int i = 0; i < this.filteredOptions.size(); i++) {
         BasicOption option = this.filteredOptions.get(i);
         option.drawEx(l, n, this.drawLastY, input);
         if (i + 1 < this.filteredOptions.size()) {
            BasicOption option2 = this.filteredOptions.get(i + 1);
            if (option.size == 1 && option2.size == 1) {
               option2.drawEx(l, n + 512, this.drawLastY, input);
               this.drawLastY = this.drawLastY + Math.max(option.getHeight(), option2.getHeight()) + 16;
               i++;
               continue;
            }
         }

         this.drawLastY = this.drawLastY + option.getHeight() + 16;
      }
   }

   private static boolean isValid(boolean flag, String text, ConfigPageOption configPageOption) {
      return !configPageOption.isValidAlt() && (flag || FuzzySearchUtils.isValid(configPageOption.name, text) || FuzzySearchUtils.isValid(configPageOption.description, text));
   }
}
