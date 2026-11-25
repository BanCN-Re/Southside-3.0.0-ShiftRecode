package dev.southside.module.impl.misc;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.values.BooleanValue;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class ChatBypass extends Module {
   private static final int DEFAULT_INT = 4;
   @SerializedName("addComma")
   public final BooleanValue addComma = new BooleanValue("Add Comma", true);

   public static boolean getAddComma() {
      ChatBypass chatBypass = Southside.INSTANCE.getModuleManager().getModule(ChatBypass.class);
      return (Boolean)chatBypass.addComma.getValue();
   }

   public static String[] getStringList(char ch) {
      int[][] intArrayList = getIntArrayList(ch);
      StringBuilder[] sbArray = new StringBuilder[4];

      for (int i = 0; i < sbArray.length; i++) {
         sbArray[i] = new StringBuilder();
      }

      String[] textArray = new String[4];

      for (byte j = 0; j < 16; j += 4) {
         for (byte k = 0; k < 16; k += 2) {
            int n = intArrayList[j][k]
               + intArrayList[j + 1][k] * 2
               + intArrayList[j + 2][k] * 4
               + intArrayList[j][k + 1] * 8
               + intArrayList[j + 1][k + 1] * 16
               + intArrayList[j + 2][k + 1] * 32
               + intArrayList[j + 3][k] * 64
               + intArrayList[j + 3][k + 1] * 128;
            sbArray[j / 4].append((char)(10240 + n));
         }
      }

      for (int m = 0; m < textArray.length; m++) {
         textArray[m] = sbArray[m].toString();
      }

      return textArray;
   }

   public static String formatText(String text) {
      if (!text.isEmpty() && !text.startsWith("/") && !text.startsWith(".")) {
         boolean addComma2 = getAddComma();
         StringBuilder sb = new StringBuilder();

         for (char ch : text.toCharArray()) {
            if (ch == 'L') {
               sb.append('Ｌ');
            } else if (ch == 'l') {
               sb.append('ｌ');
            } else {
               sb.append(ch);
            }

            if (addComma2) {
               sb.append(',');
            }
         }

         return sb.toString();
      } else {
         return text;
      }
   }

   public static String[] getStringList(String text) {
      String text2 = text.trim();
      if (text2.isEmpty()) {
         return null;
      }

      StringBuilder[] sbArray = new StringBuilder[4];

      for (int i = 0; i < sbArray.length; i++) {
         sbArray[i] = new StringBuilder();
      }

      for (int j = 0; j < text2.length(); j++) {
         String[] stringList = getStringList(text2.charAt(j));

         for (int k = 0; k < sbArray.length; k++) {
            sbArray[k].append(stringList[k]);
         }
      }

      String[] textArray = new String[4];

      for (int m = 0; m < textArray.length; m++) {
         textArray[m] = sbArray[m].toString();
      }

      return textArray;
   }

   public static int[][] getIntArrayList(char ch) {
      BufferedImage bufferedImage = new BufferedImage(16, 16, 1);
      Graphics2D graphics2D = bufferedImage.createGraphics();
      graphics2D.setColor(Color.WHITE);
      graphics2D.fillRect(0, 0, 16, 16);
      graphics2D.setFont(new Font("Courier", 0, 12));
      graphics2D.setColor(Color.BLACK);
      graphics2D.drawString(String.valueOf(ch), 0, 12);
      int[][] val = new int[16][16];

      for (int i = 0; i < 16; i++) {
         for (int j = 0; j < 16; j++) {
            val[i][j] = bufferedImage.getRGB(j, i) == Color.WHITE.getRGB() ? 0 : 1;
         }
      }

      graphics2D.dispose();
      return val;
   }

   public ChatBypass() {
      super(Category.Misc, "ChatBypass");
   }
}
