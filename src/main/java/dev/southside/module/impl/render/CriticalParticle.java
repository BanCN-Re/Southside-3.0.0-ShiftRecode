package dev.southside.module.impl.render;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.EntityEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.NumberValue;
import dev.southside.utils.ModeValue;

public class CriticalParticle extends Module {
   @SerializedName("mode")
   public final ModeValue mode = new ModeValue("Mode", "Both", "Crit", "Enchant", "Both");
   @SerializedName("multiplier")
   public final NumberValue multiplier;
   private static final long DEFAULT_LONG_VALUE = -4210329789996728310L;

   public CriticalParticle() {
      super(Category.Render, "CriticalParticle", "每次攻击显示暴击/击打粒子");
      this.multiplier = new NumberValue("Multiplier", 1, 1, (int)DEFAULT_LONG_VALUE, 1);
   }

   @EventTarget
   public void onEntity(EntityEvent entityEvent) {
      if (entityEvent.phase == Stage.POST && entityEvent.entity != null && mc2.player != null && mc2.world != null) {
         boolean flag = this.mode.matches("Crit") || this.mode.matches("Both");
         boolean flag2 = this.mode.matches("Enchant") || this.mode.matches("Both");

         for (int i = 0; i < ((Number)this.multiplier.getValue()).intValue(); i++) {
            if (flag) {
               mc2.player.addCritParticles(entityEvent.entity);
            }

            if (flag2) {
               mc2.player.addEnchantedHitParticles(entityEvent.entity);
            }
         }
      }
   }
}
