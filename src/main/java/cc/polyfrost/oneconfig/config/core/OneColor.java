package cc.polyfrost.oneconfig.config.core;

import com.google.gson.annotations.SerializedName;
import java.awt.Color;
import java.io.Serializable;
import org.jetbrains.annotations.NotNull;

public final class OneColor implements Serializable, Cloneable, Comparable<OneColor> {
   private transient Integer argb = null;
   @SerializedName(value = "hsba", alternate = "shortValueArray")
   public short[] hsba;
   @SerializedName(value = "dataBit", alternate = "chromaPeriodMillis")
   public int dataBit = -1;

   public OneColor(String text) {
      this.hsba = new short[]{0, 0, 0, 0};
      if (text.length() > 7) {
         this.hsba[3] = (short)Integer.parseInt(text.substring(6, 8), 16);
      }

      this.toHsb(text);
   }

   public OneColor(int n) {
      this.argb = n;
      this.hsba = ARGBtoHSBA(this.argb);
   }

   public OneColor(int n, int n2, int n3, int n4) {
      this.argb = (n4 & 0xFF) << 24 | (n & 0xFF) << 16 | (n2 & 0xFF) << 8 | n3 & 0xFF;
      this.hsba = ARGBtoHSBA(this.argb);
   }

   public OneColor(int n, int n2, int n3) {
      this(n, n2, n3, 255);
   }

   public OneColor(@NotNull Color color) {
      this(color.getRGB());
   }

   public OneColor(float x, float y, float width, float height) {
      this.hsba = new short[]{(short)x, (short)y, (short)width, (short)height};
      this.argb = HSBAtoARGB(this.hsba[0], this.hsba[1], this.hsba[2], this.hsba[3]);
   }

   public OneColor(float x, float y, float z) {
      this(x, y, z, 1.0F);
   }

   public OneColor(int n, int n2, int n3, int n4, int n5) {
      if (n5 == -1) {
         this.hsba = new short[]{(short)n, (short)n2, (short)n3, (short)n4};
         this.argb = HSBAtoARGB(this.hsba[0], this.hsba[1], this.hsba[2], this.hsba[3]);
      } else {
         this.dataBit = n5;
         this.hsba = new short[]{(short)n, (short)n2, (short)n3, (short)n4};
      }
   }

   public static int HSBAtoARGB(float f, float f2, float f3, int n) {
      int n2 = Color.HSBtoRGB(f / 360.0F, f2 / 100.0F, f3 / 100.0F);
      return n2 & 16777215 | n << 24;
   }

   public static short[] ARGBtoHSBA(int n) {
      short[] shortArray = new short[4];
      float[] floats = Color.RGBtoHSB(n >> 16 & 0xFF, n >> 8 & 0xFF, n & 0xFF, null);
      shortArray[0] = (short)(floats[0] * 360.0F);
      shortArray[1] = (short)(floats[1] * 100.0F);
      shortArray[2] = (short)(floats[2] * 100.0F);
      shortArray[3] = (short)(n >> 24 & 0xFF);
      return shortArray;
   }

   public int getHeight() {
      return this.fromHsb() >> 16 & 0xFF;
   }

   public int getHeightEx() {
      return this.fromHsb() >> 8 & 0xFF;
   }

   public int getHeightAlt() {
      return this.fromHsb() & 0xFF;
   }

   public int getHeightImpl() {
      return this.hsba[0];
   }

   public int getHeightDirect() {
      return this.hsba[1];
   }

   public int getHeightSafe() {
      return this.hsba[2];
   }

   public int getHeightCustom() {
      return this.hsba[3];
   }

   public void onKeyTyped(int n) {
      this.hsba[3] = (short)n;
      this.argb = HSBAtoARGB(this.hsba[0], this.hsba[1], this.hsba[2], this.hsba[3]);
   }

   public int getHeightWithOffset() {
      return this.dataBit == -1 ? -1 : this.dataBit / 1000;
   }

   public void onKeyTypedEx(int n) {
      if (n == -1) {
         this.dataBit = -1;
      } else {
         if (n < 1) {
            n = 1;
         }

         if (n > 30) {
            n = 30;
         }

         this.dataBit = n * 1000;
      }
   }

   public void performAction(int n, int n2, int n3, int n4) {
      this.hsba[0] = (short)n;
      this.hsba[1] = (short)n2;
      this.hsba[2] = (short)n3;
      this.hsba[3] = (short)n4;
      this.argb = HSBAtoARGB(this.hsba[0], this.hsba[1], this.hsba[2], this.hsba[3]);
   }

   public void setHSBA(int n, int n2) {
      this.hsba[n] = (short)n2;
   }

   public short[] getHsba() {
      return this.hsba;
   }

   public void performAction(OneColor other) {
      this.performAction(other.hsba[0], other.hsba[1], other.hsba[2], other.hsba[3]);
   }

   public int fromHsb() {
      if (this.dataBit == 0) {
         this.dataBit = -1;
      }

      if (this.dataBit == -1) {
         if (this.argb == null) {
            this.argb = HSBAtoARGB(this.hsba[0], this.hsba[1], this.hsba[2], this.hsba[3]);
         }

         return this.argb;
      } else {
         int n = Color.HSBtoRGB((float)(System.currentTimeMillis() % this.dataBit) / this.dataBit, this.hsba[1] / 100.0F, this.hsba[2] / 100.0F);
         this.hsba[0] = (short)((float)(System.currentTimeMillis() % this.dataBit) / this.dataBit * 360.0F);
         return n & 16777215 | this.hsba[3] << 24;
      }
   }

   public int getHeightByIndex() {
      return 0xFF000000 | this.fromHsb();
   }

   public int fromHsb(boolean flag) {
      if (this.dataBit == 0) {
         this.dataBit = -1;
      }

      if (this.dataBit == -1) {
         return HSBAtoARGB(this.hsba[0], 100.0F, flag ? 100.0F : 0.0F, this.hsba[3]);
      }

      int n = Color.HSBtoRGB((float)(System.currentTimeMillis() % this.dataBit) / this.dataBit, 1.0F, flag ? 1.0F : 0.0F);
      this.hsba[0] = (short)((float)(System.currentTimeMillis() % this.dataBit) / this.dataBit * 360.0F);
      return n & 16777215 | this.hsba[3] << 24;
   }

   public String getDisplayText() {
      return Integer.toHexString(0xFF000000 | this.fromHsb()).toUpperCase().substring(2);
   }

   public void toHsb(String text) {
      text = text.replace("#", "");
      if (text.length() == 8) {
         this.hsba[3] = (short)Integer.parseInt(text.substring(6, 8), 16);
      }

      if (text.length() > 6) {
         text = text.substring(0, 6);
      }

      if (text.length() == 3) {
         text = this.formatText(text.charAt(0), text.charAt(0), text.charAt(1), text.charAt(1), text.charAt(2), text.charAt(2));
      }

      if (text.length() == 1) {
         text = this.formatText(text.charAt(0), text.charAt(0), text.charAt(0), text.charAt(0), text.charAt(0), text.charAt(0));
      }

      if (text.length() == 2 && text.charAt(1) == text.charAt(0)) {
         text = this.formatText(text.charAt(0), text.charAt(0), text.charAt(0), text.charAt(0), text.charAt(0), text.charAt(0));
      }

      StringBuilder sb = new StringBuilder(text);

      while (sb.length() < 6) {
         sb.append("0");
      }

      text = sb.toString();
      int n = Integer.valueOf(text.substring(0, 2), 16);
      int n2 = Integer.valueOf(text.substring(2, 4), 16);
      int n3 = Integer.valueOf(text.substring(4, 6), 16);
      this.argb = (this.getHeightCustom() & 0xFF) << 24 | (n & 0xFF) << 16 | (n2 & 0xFF) << 8 | n3 & 0xFF;
      this.hsba = ARGBtoHSBA(this.argb);
   }

   private String formatText(char... chars) {
      StringBuilder sb = new StringBuilder();

      for (char ch : chars) {
         sb.append(ch);
      }

      return sb.toString();
   }

   public Color blendColor() {
      return new Color(this.fromHsb(), true);
   }

   @Override
   public String toString() {
      short s = -12252;
      int height = this.getHeight();
      int heightEx = this.getHeightEx();
      int heightAlt = this.getHeightAlt();
      int heightCustom = this.getHeightCustom();
      int heightImpl = this.getHeightImpl();
      int heightDirect = this.getHeightDirect();
      int heightSafe = this.getHeightSafe();
      String displayText = this.getDisplayText();
      int heightCustom2 = this.getHeightCustom();
      int n = heightSafe;
      int n2 = heightDirect;
      int n3 = heightImpl;
      int n4 = heightCustom;
      int n5 = heightAlt;
      int n6 = heightEx;
      int n7 = height;
      short s2 = 18093;
      return "OneColor{rgba=[r="
         + n7
         + ", g="
         + n6
         + ", b="
         + n5
         + ", a="
         + n4
         + "], hsba=[h="
         + n3
         + ", s="
         + n2
         + ", b="
         + n
         + ", a="
         + heightCustom2
         + "], hex="
         + displayText
         + "}";
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      } else if (obj == this) {
         return true;
      } else {
         return !(obj instanceof OneColor color)
            ? false
            : this.getHeightImpl() == color.getHeightImpl()
               && this.getHeightDirect() == color.getHeightDirect()
               && this.getHeightSafe() == color.getHeightSafe()
               && this.getHeightCustom() == color.getHeightCustom()
               && this.getHeightWithOffset() == color.getHeightWithOffset();
      }
   }

   public int compareTo(@NotNull OneColor other) {
      return this.getHeightImpl()
         - other.getHeightImpl()
         + this.getHeightDirect()
         - other.getHeightDirect()
         + this.getHeightSafe()
         - other.getHeightSafe()
         + this.getHeightCustom()
         - other.getHeightCustom();
   }

   @NotNull
   public OneColor clone() {
      return new OneColor((float)this.getHeightImpl(), (float)this.getHeightDirect(), (float)this.getHeightSafe(), (float)this.getHeightCustom());
   }

   @NotNull
   public OneColor getOneColor() {
      return this.clone();
   }
}
