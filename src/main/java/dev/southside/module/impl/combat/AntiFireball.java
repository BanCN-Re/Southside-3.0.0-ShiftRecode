package dev.southside.module.impl.combat;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.XEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.movement.LongJump;
import dev.southside.module.values.BooleanValue;
import java.util.Optional;
import java.util.stream.StreamSupport;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.util.Hand;

public class AntiFireball extends Module {
   @SerializedName("antiVelocity")
   public final BooleanValue antiVelocity = new BooleanValue("Anti Velocity", false);
   @SerializedName("ignoreSelf")
   public final BooleanValue ignoreSelf = new BooleanValue("Ignore Self", true);
   @SerializedName("velocityEnabledByModule")
   private boolean velocityEnabledByModule;

   public AntiFireball() {
      super(Category.Combat, "AntiFireball", "Prevents fireballs from damaging you");
   }

   private boolean onMotion(Entity entity) {
      return entity instanceof FireballEntity fireballEntity
         && Module.mc.player.distanceTo(entity) < 6.0F
         && (!(Boolean)this.ignoreSelf.getValue() || fireballEntity.getOwner() != Module.mc.player);
   }

   @EventTarget
   public void onX(XEvent xevent) {
      if (Module.mc.player != null && Module.mc.world != null) {
         if (!Southside.INSTANCE.getModuleManager().getModule(LongJump.class).isEnabled() && xevent.getPhase() == Stage.PRE) {
            Optional optional = StreamSupport.<Entity>stream(Module.mc.world.getEntities().spliterator(), true)
               .filter(this::onMotion)
               .map(FireballEntity.class::cast)
               .findFirst();
            Velocity velocity = Southside.INSTANCE.getModuleManager().getModule(Velocity.class);
            boolean isEnabled = velocity.isEnabled();
            if (optional.isEmpty()) {
               if (isEnabled && this.velocityEnabledByModule) {
                  velocity.setEnabled(false);
                  this.velocityEnabledByModule = false;
               }
            } else {
               if ((Boolean)this.antiVelocity.getValue()) {
                  if (!isEnabled) {
                     velocity.setEnabled(true);
                     this.velocityEnabledByModule = true;
                  }
               } else {
                  FireballEntity fireballEntity = (FireballEntity)optional.get();
                  if (Module.mc.interactionManager != null) {
                     Module.mc.interactionManager.attackEntity(Module.mc.player, fireballEntity);
                     Module.mc.player.swingHand(Hand.MAIN_HAND);
                  }
               }
            }
         }
      }
   }
}
