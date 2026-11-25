package dev.southside.module.impl.player;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.ForwardEvent;
import dev.southside.event.events.XEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.world.Scaffold;
import dev.southside.utils.ModeValue;
import net.minecraft.block.AirBlock;

public class Eagle extends Module {
   @SerializedName("sneakMode")
   public final ModeValue sneakMode = new ModeValue("Sneak Mode", "Always", "Always", "Hold");
   @SerializedName("shouldSneak")
   private boolean shouldSneak;

   public Eagle() {
      super(Category.Player, "Eagle", "适合蹲搭");
   }

   @EventTarget
   public void onX(XEvent xevent) {
      if (xevent.getPhase() == Stage.PRE && mc2.world != null && mc2.player != null) {
         this.shouldSneak = mc2.player.isOnGround()
            && mc2.player.getPitch() > 70.0F
            && mc2.world.getBlockState(mc2.player.getBlockPos().down()).getBlock() instanceof AirBlock;
         if ("Hold".equals(this.sneakMode.getDisplayTextAlt()) && mc2.options.sneakKey.isPressed()) {
            this.shouldSneak = false;
         }
      }
   }

   @EventTarget
   public void onForward(ForwardEvent forwardEvent) {
      Scaffold scaffold = Southside.INSTANCE.getModuleManager().getModule(Scaffold.class);
      if (this.shouldSneak && (!scaffold.isEnabled() || !(Boolean)scaffold.eagle.getValue())) {
         forwardEvent.setSneak(true);
      }
   }
}
