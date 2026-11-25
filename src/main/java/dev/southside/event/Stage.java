package dev.southside.event;

public enum Stage {
   PRE,
   POST;

   private static int count;

   public static Stage getStage(String text) {
      return Enum.valueOf(Stage.class, text);
   }

   // $VF: synthetic method
   private static Stage[] getStageArray() {
      return new Stage[]{PRE, POST};
   }


   public static void setCount(int n) {
      count = n;
   }

}
