package dev.southside.utils;

import cc.polyfrost.oneconfig.config.data.OptionType;
import cc.polyfrost.oneconfig.hud.Hud;
import dev.southside.module.values.Value;
import org.jetbrains.annotations.NotNull;

public class HudValue extends Value<Hud> {
   public HudValue(@NotNull String text, @NotNull Hud hud) {
      super(text, hud);
   }

   @Override
   public OptionType getOptionType() {
      return null;
   }
}
