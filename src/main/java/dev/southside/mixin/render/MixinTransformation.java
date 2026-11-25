package dev.southside.mixin.render;

import dev.southside.Southside;
import dev.southside.event.events.TransformationEvent;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Transformation.class)
public abstract class MixinTransformation {
   @Inject(
      method = "Lnet/minecraft/client/render/model/json/Transformation;apply(ZLnet/minecraft/client/util/math/MatrixStack;)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private void onApplyHead(boolean flag, MatrixStack matrices, CallbackInfo ci) {
      TransformationEvent transformationEvent = (TransformationEvent)Southside.INSTANCE
         .getEventBus()
         .postCancellableEvent(TransformationEvent.getTransformationEvent((Transformation)(Object)this, flag, matrices));
      if (transformationEvent.isCancelled()) {
         ci.cancel();
      }
   }
}
