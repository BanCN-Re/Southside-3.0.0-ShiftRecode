package dev.southside.mixin.render;

import dev.southside.utils.heypixel.VIPList;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.text.OrderedText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TranslationStorage.class)
public class MixinTranslationStorage {
   @Inject(
      method = "Lnet/minecraft/client/resource/language/TranslationStorage;reorder(Lnet/minecraft/text/StringVisitable;)Lnet/minecraft/text/OrderedText;",
      at = @At("RETURN"),
      cancellable = true
   )
   private void onReorderReturn(CallbackInfoReturnable<OrderedText> cir) {
      OrderedText returnValue = (OrderedText)cir.getReturnValue();
      if (returnValue != null) {
         cir.setReturnValue(new VIPList.Class5481Impl(returnValue));
      }
   }
}
