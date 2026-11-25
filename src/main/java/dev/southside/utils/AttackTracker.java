package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.events.ClientWorldEvent;
import java.util.ArrayList;
import java.util.List;

public class AttackTracker implements IMinecraft {
   public static AttackTracker INSTANCE = new AttackTracker();
   @SerializedName("attackTicks")
   public List<Integer> attackTicks = new ArrayList<>();
   private static final long a = 8681266886865846292L;

   public static boolean isValid(Integer integer) {
      return mc.player.age < integer || mc.player.age - integer > (int)a;
   }

   @EventTarget
   public void onClientWorld(ClientWorldEvent clientWorldEvent) {
      this.attackTicks.clear();
   }

   public void performAction() {
      if (mc.player == null) {
         this.attackTicks.clear();
      } else {
         this.attackTicks.add(mc.player.age);
      }
   }

   public int getHeight() {
      if (mc.player == null) {
         this.attackTicks.clear();
      }

      this.attackTicks.removeIf(AttackTracker::isValid);
      return this.attackTicks.size();
   }
}
