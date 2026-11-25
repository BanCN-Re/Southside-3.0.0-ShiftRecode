package dev.southside.mixin.render;

import dev.southside.utils.heypixel.VIPList;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.text.OrderedText;
import net.minecraft.util.Language;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Language.class)
public abstract class MixinLanguage {
   @Inject(method = "Lnet/minecraft/util/Language;reorder(Ljava/util/List;)Ljava/util/List;", at = @At("RETURN"), cancellable = true)
   private void onReorderReturn(CallbackInfoReturnable<List<OrderedText>> cir) {
      List returnValue = (List)cir.getReturnValue();
      ArrayList list = new ArrayList();

      for (Object obj : returnValue) {
         OrderedText orderedText = (OrderedText)obj;
         if (orderedText != null) {
            list.add(new VIPList.Class5481Impl(orderedText));
         } else {
            list.add(null);
         }
      }

      cir.setReturnValue(list);
   }
}
