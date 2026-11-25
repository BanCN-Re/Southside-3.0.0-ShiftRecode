package dev.southside.utils.rotation;

public enum MoveFixMode {
   Off,
   Silent,
   Strict;


   public static MoveFixMode getMoveFixMode(String text) {
      return Enum.valueOf(MoveFixMode.class, text);
   }

   // $VF: synthetic method
   private static MoveFixMode[] getMoveFixModeArray() {
      return new MoveFixMode[]{Off, Silent, Strict};
   }


}
