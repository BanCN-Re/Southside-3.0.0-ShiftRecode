package dev.southside.manager;

import cc.polyfrost.oneconfig.utils.Notifications;
import com.google.gson.annotations.SerializedName;
import dev.southside.module.Module;
import dev.southside.module.impl.client.Notification;
import dev.southside.module.impl.client.Notification.Mode;
import dev.southside.utils.EaseInOutCubicAnimation;
import dev.southside.utils.OneConfigScreen;
import java.util.ArrayList;

public class FriendManager {
   @SerializedName("friends")
   public final ArrayList<String> friends = new ArrayList<>();

   public void applyText(String text) {
      if (this.friends.contains(text)) {
         this.friends.remove(text);
         this.applyText("Removed friend: " + text, Mode.ENABLED);
      } else {
         this.applyText("Friend not found: " + text, Mode.ENABLED);
      }
   }

   public void applyTextEx(String text) {
      if (!this.friends.contains(text)) {
         this.friends.add(text);
         this.applyText("Added friend: " + text, Mode.ENABLED);
      } else {
         this.applyText(text + " is already your friend!", Mode.WARN);
      }
   }

   private void applyText(String text, Mode mode) {
      if (this.getFriends()) {
         double d = ((Number)Notification.INSTANCE.keepTime.getValue()).doubleValue();
         if (this.getFriends()) {
            EaseInOutCubicAnimation easeInOutCubicAnimation = new EaseInOutCubicAnimation((int)(d * 1000.0), 0.0F, 1.0F, false);

            Notifications.INSTANCE.send("Friend Manager", text, switch (mode) {
               case INFO -> Notification.iconUtil3;
               case WARN -> Notification.iconUtil4;
               case ERROR -> Notification.iconUtil5;
               case ENABLED -> Notification.iconUtil;
               case DISABLED -> Notification.iconUtil2;
            }, (float)(d * 1000.0), () -> Notification.computeFloat(easeInOutCubicAnimation));
         }
      }
   }

   private boolean getFriends() {
      return Module.mc.world != null || Module.mc.currentScreen instanceof OneConfigScreen;
   }

}
