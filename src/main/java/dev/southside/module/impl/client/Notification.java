package dev.southside.module.impl.client;

import cc.polyfrost.oneconfig.utils.Notifications;
import com.google.gson.annotations.SerializedName;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.EaseInOutCubicAnimation;
import dev.southside.utils.OneConfigSvgs;
import dev.southside.utils.GuiUtils;
import dev.southside.utils.OneConfigIcon;
import dev.southside.utils.NumberValue;
import dev.southside.utils.UiAnimation;
import dev.southside.utils.OneConfigScreen;

public class Notification extends Module {
   public static Notification INSTANCE;
   public static final OneConfigIcon iconUtil = new OneConfigIcon(OneConfigSvgs.NOTIFICATION_ENABLED);
   public static final OneConfigIcon iconUtil2 = new OneConfigIcon(OneConfigSvgs.NOTIFICATION_DISABLED);
   public static final OneConfigIcon iconUtil3 = new OneConfigIcon(OneConfigSvgs.INFO_ARROW);
   public static final OneConfigIcon iconUtil4 = new OneConfigIcon(OneConfigSvgs.WARNING);
   public static final OneConfigIcon iconUtil5 = new OneConfigIcon(OneConfigSvgs.ERROR);
   @SerializedName("keepTime")
   public final NumberValue keepTime = new NumberValue("Keep Time", 1, 0, 5, 0.1);

   public Notification() {
      super(Category.Client, "Notification");
      INSTANCE = this;
   }

   @Override
   public void onEnable() {
      this.setEnabled(false);
   }

   public static float computeFloat(UiAnimation uiAnimation) {
      return uiAnimation.evaluate(GuiUtils.getProgress());
   }

   public static void applyText(String text, String text2, Notification.Mode mode) {
      mc2.execute(() -> {
         if (mc2.world != null || mc2.currentScreen instanceof OneConfigScreen) {
            float f = INSTANCE == null ? 1000.0F : ((Number)INSTANCE.keepTime.getValue()).floatValue() * 1000.0F;
            EaseInOutCubicAnimation easeInOutCubicAnimation = new EaseInOutCubicAnimation((int)f, 0.0F, 1.0F, false);

            OneConfigIcon oneConfigIcon = switch (mode) {
               case INFO -> iconUtil3;
               case WARN -> iconUtil4;
               case ERROR -> iconUtil5;
               case ENABLED -> iconUtil;
               case DISABLED -> iconUtil2;
            };
            Notifications.INSTANCE.send(text, text2, oneConfigIcon, f, () -> computeFloat(easeInOutCubicAnimation));
         }
      });
   }

   public enum Mode {
      INFO,
      WARN,
      ERROR,
      ENABLED,
      DISABLED;


      // $VF: synthetic method
      private static Notification.Mode[] createValues() {
         return new Notification.Mode[]{INFO, WARN, ERROR, ENABLED, DISABLED};
      }


   }
}
