package dev.southside.mixin.entity;

import dev.southside.Southside;
import dev.southside.event.events.FovMultiplierEvent;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class MixinAbstractClientPlayerEntity {
   @Inject(method = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;getFovMultiplier(ZF)F", at = @At("RETURN"), cancellable = true)
   private void onGetFovMultiplierReturn(CallbackInfoReturnable<Float> cir) {
      Float returnValue = (Float)cir.getReturnValue();
      FovMultiplierEvent fovMultiplierEvent = new FovMultiplierEvent(returnValue);
      Southside.INSTANCE.getEventBus().postCancellable(fovMultiplierEvent);
      cir.setReturnValue(fovMultiplierEvent.getFovMultiplier());
   }
}
