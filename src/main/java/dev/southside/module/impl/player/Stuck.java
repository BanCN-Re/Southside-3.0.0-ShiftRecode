package dev.southside.module.impl.player;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.events.ClientPlayerEntityEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.world.Scaffold;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.PacketUtils;
import dev.southside.utils.MovementFreezeManager;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;

public class Stuck extends Module {
   @SerializedName("disableScaffold")
   public final BooleanValue disableScaffold = new BooleanValue("Disable Scaffold", false);
   @SerializedName("infinite")
   public final BooleanValue infinite = new BooleanValue("Infnite", false);

   public Stuck() {
      super(Category.Player, "Stuck", "你会被定住");
   }

   @Override
   public void onEnable() {
      super.onEnable();
      if ((Boolean)this.disableScaffold.getValue() && Southside.INSTANCE != null) {
         Scaffold scaffold = Southside.INSTANCE.getModuleManager().getModule(Scaffold.class);
         if (scaffold.isEnabled()) {
            scaffold.setEnabled(false);
         }
      }
   }

   @EventTarget
   public void onClientPlayerEntity(ClientPlayerEntityEvent clientPlayerEntityEvent) {
      if (mc2.player != null && mc2.world != null) {
         int noPositionMoveTicks = PacketUtils.noPositionMoveTicks;
         if ((Boolean)this.infinite.getValue() && noPositionMoveTicks % 60 == 0) {
            mc2.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc2.player, Mode.START_FALL_FLYING));
         }

         if (((Boolean)this.infinite.getValue() || noPositionMoveTicks < 19) && !MovementFreezeManager.activeValue && mc2.player.getVelocity() != null) {
            MovementFreezeManager.performAction();
         }
      }
   }
}
