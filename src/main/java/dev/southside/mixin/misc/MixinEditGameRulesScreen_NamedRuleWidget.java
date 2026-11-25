package dev.southside.mixin.misc;

import java.util.List;
import net.minecraft.client.gui.screen.world.EditGameRulesScreen.NamedRuleWidget;
import net.minecraft.text.OrderedText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NamedRuleWidget.class)
public interface MixinEditGameRulesScreen_NamedRuleWidget {
   @Accessor("name")
   List<OrderedText> name();
}
