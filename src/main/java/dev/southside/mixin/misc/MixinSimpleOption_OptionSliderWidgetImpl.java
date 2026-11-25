package dev.southside.mixin.misc;

import net.minecraft.client.option.SimpleOption.OptionSliderWidgetImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(OptionSliderWidgetImpl.class)
public interface MixinSimpleOption_OptionSliderWidgetImpl {
   @Invoker("applyPendingValue")
   void southside$applyPendingValue();
}
