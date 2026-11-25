package dev.southside.mixin.misc;

import net.minecraft.client.gui.screen.world.EditGameRulesScreen.RuleCategoryWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RuleCategoryWidget.class)
public interface MixinEditGameRulesScreen_RuleCategoryWidget {
   @Accessor("name")
   Text name();
}
