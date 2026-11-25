package dev.southside.mixin.misc;

import dev.southside.utils.IMenuSliderAccessor;
import net.minecraft.client.gui.widget.SliderWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SliderWidget.class)
public abstract class MixinSliderWidget implements IMenuSliderAccessor {
   @Shadow
   protected double value;

   @Override
   public double menuValue() {
      return this.value;
   }
}
