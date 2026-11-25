package dev.southside.module.impl.client;

import com.google.gson.annotations.SerializedName;
import dev.southside.gui.hud.ArrayListHud;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.HudValue;

public class ArrayList extends Module {
   @SerializedName("hudSetting")
   public final HudValue hudSetting = new HudValue("ArrayList", new ArrayListHud(0.0F, 0.0F, 1.0F));

   public ArrayList() {
      super(Category.Client, "ArrayList", true);
   }

   @Override
   public void postInit() {
      super.postInit();
      ArrayListHud arrayListHud = (ArrayListHud)this.hudSetting.getValue();
      this.hideIf(arrayListHud.customColorA.getLabel(), () -> !arrayListHud.color.matches("Custom"));
      this.hideIf(arrayListHud.customColorB.getLabel(), () -> !arrayListHud.color.matches("Custom"));
      this.hideIf(arrayListHud.customSpeed.getLabel(), () -> !arrayListHud.color.matches("Custom"));
      this.hideIf(ArrayListHud.removeShadow.getLabel(), ArrayList::isValidEx);
      this.hideIf(ArrayListHud.showSuffix.getLabel(), ArrayList::isValidEx);
      this.hideIf(ArrayListHud.background.getLabel(), ArrayList::isValidEx);
      this.hideIf(ArrayListHud.roundBackground.getLabel(), () -> !(Boolean)ArrayListHud.background.getValue() || isValidEx());
      this.addListener(arrayListHud.customColorA.getLabel(), (Runnable)arrayListHud::performAction);
      this.addListener(arrayListHud.customColorB.getLabel(), (Runnable)arrayListHud::performAction);
   }

   private static boolean isValidEx() {
      return ArrayListHud.style.matches("SigmaJello");
   }
}
