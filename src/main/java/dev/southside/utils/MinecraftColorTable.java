package dev.southside.utils;

public final class MinecraftColorTable {
   public static final int[] DEFAULT_VALUES = getIntList();

   private static int[] getIntList() {
      int[] values = new int[32];

      for (int i = 0; i < 32; i++) {
         int n = (i >> 3 & 1) * 85;
         int n2 = (i >> 2 & 1) * 170 + n;
         int n3 = (i >> 1 & 1) * 170 + n;
         int n4 = (i & 1) * 170 + n;
         if (i == 6) {
            n2 += 85;
         }

         if (i >= 16) {
            n2 /= 4;
            n3 /= 4;
            n4 /= 4;
         }

         values[i] = (n2 & 0xFF) << 16 | (n3 & 0xFF) << 8 | n4 & 0xFF;
      }

      return values;
   }
}
