package dev.southside.module.impl.combat;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.events.ClientPlayerEntityEvent;
import dev.southside.event.events.ClientWorldEvent;
import dev.southside.event.events.EntityEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;

public class SuperKnockBack extends Module {
   @SerializedName("forwardKeyState")
   private SuperKnockBack.Mode forwardKeyState = SuperKnockBack.Mode.IDLE;

   public SuperKnockBack() {
      super(Category.Combat, "SuperKnockBack", "More KnockBack");
   }

   @Override
   public void onEnable() {
      this.forwardKeyState = SuperKnockBack.Mode.IDLE;
   }

   @Override
   public void onDisable() {
      this.forwardKeyState = SuperKnockBack.Mode.IDLE;
   }

   @EventTarget
   public void onClientWorld(ClientWorldEvent clientWorldEvent) {
      this.forwardKeyState = SuperKnockBack.Mode.IDLE;
   }

   @EventTarget
   public void onEntity(EntityEvent entityEvent) {
      if (entityEvent.entity instanceof LivingEntity) {
         this.forwardKeyState = SuperKnockBack.Mode.RELEASE;
      }
   }

   @EventTarget
   public void onClientPlayerEntity(ClientPlayerEntityEvent clientPlayerEntityEvent) {
      switch (this.forwardKeyState) {
         case IDLE:
         default:
            break;
         case RELEASE:
            Module.mc.options.forwardKey.setPressed(false);
            this.forwardKeyState = SuperKnockBack.Mode.RESTORE;
            break;
         case RESTORE:
            Module.mc
               .options
               .forwardKey
               .setPressed(InputUtil.isKeyPressed(Module.mc.getWindow().getHandle(), Module.mc.options.forwardKey.getDefaultKey().getCode()));
            this.forwardKeyState = SuperKnockBack.Mode.IDLE;
      }
   }

   private enum Mode {
      IDLE,
      RELEASE,
      RESTORE;


      // $VF: synthetic method
      private static SuperKnockBack.Mode[] createValues() {
         return new SuperKnockBack.Mode[]{IDLE, RELEASE, RESTORE};
      }


   }
}
