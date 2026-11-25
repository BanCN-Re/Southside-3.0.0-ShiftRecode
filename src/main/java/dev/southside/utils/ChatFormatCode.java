package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import java.awt.Color;

public class ChatFormatCode {
   public static final ChatFormatCode INSTANCE = new ChatFormatCode('0', new Color(0), false);
   public static final ChatFormatCode INSTANCE2 = new ChatFormatCode('1', new Color(170), false);
   public static final ChatFormatCode INSTANCE3 = new ChatFormatCode('2', new Color(43520), false);
   public static final ChatFormatCode INSTANCE4 = new ChatFormatCode('3', new Color(43690), false);
   public static final ChatFormatCode INSTANCE5 = new ChatFormatCode('4', new Color(11141120), false);
   public static final ChatFormatCode INSTANCE6 = new ChatFormatCode('5', new Color(11141290), false);
   public static final ChatFormatCode INSTANCE7 = new ChatFormatCode('6', new Color(16755200), false);
   public static final ChatFormatCode INSTANCE8 = new ChatFormatCode('7', new Color(11184810), false);
   public static final ChatFormatCode INSTANCE9 = new ChatFormatCode('8', new Color(5592405), false);
   public static final ChatFormatCode INSTANCE10 = new ChatFormatCode('9', new Color(5592575), false);
   public static final ChatFormatCode INSTANCE11 = new ChatFormatCode('a', new Color(5635925), false);
   public static final ChatFormatCode INSTANCE12 = new ChatFormatCode('b', new Color(5636095), false);
   public static final ChatFormatCode INSTANCE13 = new ChatFormatCode('c', new Color(16733525), false);
   public static final ChatFormatCode INSTANCE14 = new ChatFormatCode('d', new Color(16733695), false);
   public static final ChatFormatCode INSTANCE15 = new ChatFormatCode('e', new Color(16777045), false);
   public static final ChatFormatCode INSTANCE16 = new ChatFormatCode('f', new Color(16777215), false);
   public static final ChatFormatCode INSTANCE17 = new ChatFormatCode('k', null, true);
   public static final ChatFormatCode INSTANCE18 = new ChatFormatCode('l', null, true);
   public static final ChatFormatCode INSTANCE19 = new ChatFormatCode('m', null, true);
   public static final ChatFormatCode INSTANCE20 = new ChatFormatCode('n', null, true);
   public static final ChatFormatCode INSTANCE21 = new ChatFormatCode('o', null, true);
   public static final ChatFormatCode INSTANCE22 = new ChatFormatCode('r', null, false);
   public static final char ch = 167;
   @SerializedName("formatChar")
   private final char formatChar;
   @SerializedName("color")
   private final Color color;
   @SerializedName("isFormat")
   private final boolean isFormat;

   public ChatFormatCode(char ch2, Color color2, boolean flag) {
      this.formatChar = ch2;
      this.color = color2;
      this.isFormat = flag;
   }

   public String getDisplayText() {
      return "" + '§' + this.formatChar;
   }
}
