package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class GuiNotificationManager {
   private static final float DEFAULT_FLOAT = 4000.0F;
   public static final GuiNotificationManager INSTANCE = new GuiNotificationManager();
   @SerializedName("notifications")
   private final CopyOnWriteArrayList<ToastNotification> notifications = new CopyOnWriteArrayList<>();
   @SerializedName("toRemove")
   private final ArrayList<ToastNotification> toRemove = new ArrayList<>();
   private static final long DEFAULT_LONG = -2331510601357983734L;

   private GuiNotificationManager() {
   }

   public void draw(long vg, int x, int y, InputHandler input) {
      for (ToastNotification toastNotification : this.toRemove) {
         this.notifications.remove(toastNotification);
      }

      this.toRemove.clear();
      int n = y;

      for (ToastNotification toastNotification2 : this.notifications) {
         int n2 = toastNotification2.drawAndGetHeight(vg, x, n, input);
         if (n2 == -1) {
            this.toRemove.add(toastNotification2);
         }

         n -= n2 + (int)DEFAULT_LONG;
      }
   }

   public void applyText(String text, float f, SvgAsset svg) {
      this.notifications.add(new ToastNotification(text, f, svg));
   }

   public void applyText(String text, float f) {
      this.applyText(text, f, null);
   }

   public void applyText(String text, SvgAsset svg) {
      this.applyText(text, 4000.0F, svg);
   }

   public void applyText(String text) {
      this.applyText(text, 4000.0F);
   }
}
