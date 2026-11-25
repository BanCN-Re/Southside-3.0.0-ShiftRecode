package dev.southside.module.impl.client;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.events.ButtonEvent;
import dev.southside.event.events.ClientWorldEvent;
import dev.southside.event.events.StateEvent;
import dev.southside.manager.FriendManager;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.combat.KillAura;
import dev.southside.utils.NanoTimer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.EntityHitResult;

public class MCF extends Module {
   @SerializedName("clickTimer")
   private final NanoTimer clickTimer = new NanoTimer();
   @SerializedName("middleClickPending")
   private boolean middleClickPending;

   public MCF() {
      super(Category.Client, "MCF", "鼠标中键添加Friend");
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      if (this.middleClickPending) {
         this.middleClickPending = false;
         if (mc2.isWindowFocused() && mc2.currentScreen == null && mc2.player != null && mc2.world != null && this.clickTimer.hasTimePassedDouble(200.0)) {
            this.clickTimer.reset();
            KillAura killAura = Southside.INSTANCE.getModuleManager().getModule(KillAura.class);
            if (!killAura.isEnabled() || KillAura.target == null) {
               if (mc2.crosshairTarget instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof PlayerEntity player) {
                  String text = Formatting.strip(player.getName().getString());
                  if (text == null || text.isBlank()) {
                     return;
                  }

                  FriendManager friendManager = Southside.INSTANCE.getFriendManager();
                  if (friendManager.friends.contains(text)) {
                     friendManager.applyText(text);
                     this.applyTextImpl("Removed " + text + " from friends");
                  } else {
                     friendManager.applyTextEx(text);
                     this.applyTextImpl("Added " + text + " to friends");
                  }
               }
            }
         }
      }
   }

   @EventTarget
   public void onButton(ButtonEvent buttonEvent) {
      if (buttonEvent.getButton() == 2 && buttonEvent.getAction() == 0) {
         this.middleClickPending = true;
      }
   }

   @EventTarget
   public void onClientWorld(ClientWorldEvent clientWorldEvent) {
      this.middleClickPending = false;
   }

   @Override
   public void onDisable() {
      this.middleClickPending = false;
      super.onDisable();
   }
}
