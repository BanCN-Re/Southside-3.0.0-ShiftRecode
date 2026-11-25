package dev.southside.utils;

import cc.polyfrost.oneconfig.config.elements.BasicOption;
import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;

public class ModConfigPage extends GuiPage {
   @SerializedName("page")
   private final OptionPage page;
   @SerializedName("categories")
   private final List<BasicButton> categories = new ArrayList<>();
   @SerializedName("selectedCategory")
   private String selectedCategory;
   @SerializedName("totalSize")
   private int totalSize = 724;
   @SerializedName("base")
   private final boolean base;
   @SerializedName("categoryY")
   private int categoryY;

   public ModConfigPage(OptionPage optionPage) {
      this(optionPage, false);
   }

   public ModConfigPage(OptionPage optionPage, boolean flag) {
      super(optionPage.name);
      this.page = optionPage;
      this.base = flag;
      if (!optionPage.categories.isEmpty()) {
         this.selectedCategory = optionPage.categories.keySet().iterator().next();
         if (optionPage.categories.size() >= 2) {
            for (String text : optionPage.categories.keySet()) {
               BasicButton basicButton = new BasicButton(0, 32, text, ColorPalette.INSTANCE3);
               basicButton.setRunnable(() -> this.applyText(text));
               basicButton.setToggleable(true);
               if (text.equals(this.selectedCategory)) {
                  basicButton.setToggled(true);
               }

               this.categories.add(basicButton);
            }
         }
      }
   }

   public OptionPage getPage() {
      return this.page;
   }

   public String getSelectedCategory() {
      return this.selectedCategory;
   }

   @Override
   public int getSize() {
      return this.totalSize;
   }

   @Override
   public boolean isBase() {
      return this.base;
   }

   public void applyText(String text) {
      if (this.page.categories.containsKey(text)) {
         this.selectedCategory = text;

         for (BasicButton basicButton : this.categories) {
            if (!basicButton.getText().equals(text)) {
               basicButton.setToggled(false);
               super.scrollTarget = 0.0F;
               super.scrollAnimation = null;
            }
         }

         OneConfigGui.INSTANCE.getOneColor2();
      }
   }

   private List<OptionSubcategory> collectItems() {
      String input = OneConfigGui.INSTANCE.getInput();
      if (input.isEmpty()) {
         return this.page.categories.isEmpty() ? List.of() : this.page.categories.get(this.selectedCategory).subcategories;
      }

      ArrayList list = new ArrayList();

      for (Entry entry : this.page.categories.entrySet()) {
         if (FuzzySearchUtils.isValid((String)entry.getKey(), input)) {
            list.addAll(((OptionCategory)entry.getValue()).subcategories);
         } else {
            for (OptionSubcategory optionSubcategory : ((OptionCategory)entry.getValue()).subcategories) {
               if (FuzzySearchUtils.isValid(optionSubcategory.getName(), input)) {
                  list.add(optionSubcategory);
               } else {
                  for (BasicOption option : optionSubcategory.options) {
                     if (FuzzySearchUtils.isValid(option.name, input)) {
                        list.add(optionSubcategory);
                        break;
                     }
                  }
               }
            }
         }
      }

      return list;
   }

   @Override
   public void draw(long vg, int x, int y, InputHandler input) {
      if (!this.page.categories.isEmpty()) {
         ScissorHelper scissor = ScissorHelper.INSTANCE;
         int n = y + (this.page.categories.size() != 1 ? 64 : 16) + this.categoryY;
         List list = this.collectItems();

         for (Object obj : list) {
            OptionSubcategory optionSubcategory = (OptionSubcategory)obj;
            n += optionSubcategory.drawAndGetHeight(vg, x + 30, n, input);
         }

         scissor.save();
         scissor.clearScissors(vg);

         for (Object obj2 : list) {
            OptionSubcategory optionSubcategory2 = (OptionSubcategory)obj2;
            optionSubcategory2.draw(vg, x + 30, input);
         }

         scissor.restore(vg);
         this.totalSize = n - y;
      }
   }

   @Override
   public int drawAndGetHeight(long vg, int x, int y, InputHandler input) {
      this.categoryY = 0;
      if (this.categories.size() <= 1) {
         return 0;
      }

      int n = x + 16;
      byte b = 0;
      boolean flag = !OneConfigGui.INSTANCE.getInput().trim().isEmpty();

      for (BasicButton basicButton : this.categories) {
         if (basicButton.getWidth() == 0) {
            basicButton.setWidth((int)(Math.ceil(NanoVGHelper.INSTANCE.getTextWidth(vg, basicButton.getText(), 12.0F, OneConfigFonts.ONE_CONFIG_FONT) / 8.0F) * 8.0 + 16.0));
         }

         if (n + basicButton.getWidth() >= x + 1024 - 16) {
            n = x + 16;
            b += 48;
            this.categoryY += 48;
         }

         if (flag) {
            boolean isValid = FuzzySearchUtils.isValid(basicButton.getText(), OneConfigGui.INSTANCE.getInput());
            boolean isToggled = basicButton.isToggled();
            basicButton.setToggled(isValid);
            basicButton.draw(vg, n, y + 16 + b, input);
            n += basicButton.getWidth() + 16;
            basicButton.setToggled(isToggled);
         } else {
            basicButton.draw(vg, n, y + 16 + b, input);
            n += basicButton.getWidth() + 16;
         }
      }

      return 60 + b;
   }

   private void performAction(Consumer<BasicOption> consumer) {
      if (!this.page.categories.isEmpty()) {
         for (OptionSubcategory optionSubcategory : this.collectItems()) {
            for (BasicOption option : optionSubcategory.options) {
               consumer.accept(option);
            }
         }
      }
   }

   @Override
   public void onMouseClick(double d, double d2, int n) {
      this.performAction(option -> option.onMouseClick(d, d2, n));
   }

   @Override
   public void onMouseClickEx(double d, double d2, int n) {
      this.performAction(option -> option.onMouseClickEx(d, d2, n));
   }

   @Override
   public void onKeyTypedEx(int n) {
      this.performAction(option -> option.onKeyTypedEx(n));
   }

   @Override
   public void onKeyTyped(int n) {
      this.performAction(option -> option.onKeyTyped(n));
   }

   @Override
   public void performAction() {
      this.page.owner.config.saveToJson();
      OneConfigGui.INSTANCE.getOneColor2();

      for (OptionSubcategory optionSubcategory : this.collectItems()) {
         for (BasicOption option : optionSubcategory.options) {
            option.performActionEx();
         }
      }
   }
}
