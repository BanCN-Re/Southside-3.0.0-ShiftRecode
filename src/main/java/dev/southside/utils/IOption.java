package dev.southside.utils;

import cc.polyfrost.oneconfig.config.data.OptionType;

public interface IOption {
   String DEFAULT_TEXT = "General";

   OptionType getOptionType();

   default String getA() {
      return DEFAULT_TEXT;
   }

   default String getDisplayText() {
      return "";
   }
}
