package cc.polyfrost.oneconfig.config.data;

public enum OptionType {
   SWITCH,
   CHECKBOX,
   DUAL_OPTION,
   TEXT,
   SLIDER,
   NUMBER,
   COLOR,
   DROPDOWN,
   INFO,
   HEADER,
   BUTTON,
   KEYBIND;


   // $VF: synthetic method
   private static OptionType[] getOptionTypeArray() {
      return new OptionType[]{SWITCH, CHECKBOX, DUAL_OPTION, TEXT, SLIDER, NUMBER, COLOR, DROPDOWN, INFO, HEADER, BUTTON, KEYBIND};
   }


}
