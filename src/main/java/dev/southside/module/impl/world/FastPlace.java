package dev.southside.module.impl.world;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.events.ClientPlayerEntityEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.NumberValue;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;

public class FastPlace extends Module {
   @SerializedName("placementDelay")
   public NumberValue placementDelay;
   private static final long DEFAULT_LONG_VALUE = 2717588763533377542L;

   public FastPlace() {
      super(Category.World, "FastPlace", "快速放方块");
      this.placementDelay = new NumberValue("Speed", 1, 0, (int)DEFAULT_LONG_VALUE, 1);
   }

   @EventTarget
   public void onClientPlayerEntity(ClientPlayerEntityEvent clientPlayerEntityEvent) {
      if (Module.mc.player != null) {
         Item item = Module.mc.player.getMainHandStack().getItem();
         Item item2 = Module.mc.player.getOffHandStack().getItem();
         if (item instanceof BlockItem || item2 instanceof BlockItem) {
            Module.mc.itemUseCooldown = Math.min(Module.mc.itemUseCooldown, ((Number)this.placementDelay.getValue()).intValue());
         }
      }
   }
}
