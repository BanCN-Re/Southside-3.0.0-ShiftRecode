package cc.polyfrost.oneconfig.utils.color;

import cc.polyfrost.oneconfig.config.core.OneColor;
import com.google.gson.annotations.SerializedName;
import dev.southside.utils.ArgbColorUtils;
import dev.southside.utils.OneConfigColors;
import java.awt.Color;

public class ColorPalette {
   public static final ColorPalette INSTANCE = new ColorPalette("Transparent", OneConfigColors.TRANSPARENT, OneConfigColors.TRANSPARENT, OneConfigColors.TRANSPARENT);
   public static final ColorPalette INSTANCE2 = new ColorPalette("Primary", OneConfigColors.PRIMARY_500, OneConfigColors.PRIMARY_600, OneConfigColors.PRIMARY_600_80);
   public static final ColorPalette INSTANCE3 = new ColorPalette("Secondary", OneConfigColors.GRAY_500, OneConfigColors.GRAY_400, OneConfigColors.GRAY_400_80);
   public static final ColorPalette INSTANCE4 = new ColorPalette("Tertiary", OneConfigColors.WHITE_80, OneConfigColors.WHITE, OneConfigColors.WHITE_80);
   public static final ColorPalette INSTANCE5 = new ColorPalette("Primary Destructive", OneConfigColors.ERROR_700, OneConfigColors.ERROR_600, OneConfigColors.ERROR_600_80);
   public static final ColorPalette INSTANCE6 = new ColorPalette("Secondary Destructive", OneConfigColors.GRAY_500, OneConfigColors.ERROR_800, OneConfigColors.ERROR_800_80);
   public static final ColorPalette INSTANCE7 = new ColorPalette("Tertiary Destructive", OneConfigColors.WHITE_90, OneConfigColors.ERROR_600_80, OneConfigColors.ERROR_600_80);
   @SerializedName("name")
   private final String name;
   @SerializedName("colorNormal")
   private final int colorNormal;
   @SerializedName("colorHovered")
   private final int colorHovered;
   @SerializedName("colorPressed")
   private final int colorPressed;
   @SerializedName("colorNormalf")
   private final float[] colorNormalf;
   @SerializedName("colorHoveredf")
   private final float[] colorHoveredf;
   @SerializedName("colorPressedf")
   private final float[] colorPressedf;

   public ColorPalette(int n, int n2, int n3) {
      this("Unnamed", n, n2, n3);
   }

   public ColorPalette(String text, int n, int n2, int n3) {
      this.name = text;
      this.colorNormal = n;
      this.colorHovered = n2;
      this.colorPressed = n3;
      this.colorNormalf = getFloatList(n);
      this.colorHoveredf = getFloatList(n2);
      this.colorPressedf = getFloatList(n3);
   }

   public ColorPalette(OneColor color, OneColor color2, OneColor color3) {
      this(color.fromHsb(), color2.fromHsb(), color3.fromHsb());
   }

   public ColorPalette(Color color, Color color2, Color color3) {
      this(color.getRGB(), color2.getRGB(), color3.getRGB());
   }

   public ColorPalette(float[] floats, float[] floats2, float[] floats3) {
      this.name = "Unnamed";
      this.colorNormalf = floats;
      this.colorHoveredf = floats2;
      this.colorPressedf = floats3;
      this.colorNormal = ArgbColorUtils.rgbaFloat(floats[0], floats[1], floats[2], floats[3]);
      this.colorHovered = ArgbColorUtils.rgbaFloat(floats2[0], floats2[1], floats2[2], floats2[3]);
      this.colorPressed = ArgbColorUtils.rgbaFloat(floats3[0], floats3[1], floats3[2], floats3[3]);
   }

   private static float[] getFloatList(int n) {
      return new float[]{
         ArgbColorUtils.getRed(n) / 255.0F,
         ArgbColorUtils.getGreen(n) / 255.0F,
         ArgbColorUtils.getBlue(n) / 255.0F,
         ArgbColorUtils.getAlpha(n) / 255.0F
      };
   }

   public int getColorNormal() {
      return this.colorNormal;
   }

   public int getColorHovered() {
      return this.colorHovered;
   }

   public int getColorPressed() {
      return this.colorPressed;
   }

   public float[] getColorNormalf() {
      return this.colorNormalf;
   }

   public float[] getColorHoveredf() {
      return this.colorHoveredf;
   }

   public float[] getColorPressedf() {
      return this.colorPressedf;
   }

   @Override
   public String toString() {
      short s = -4370;
      int colorPressed2 = this.colorPressed;
      int colorHovered2 = this.colorHovered;
      int colorNormal2 = this.colorNormal;
      String name2 = this.name;
      short s2 = -32540;
      return "ColorPalette(name=" + name2 + ", normal=" + colorNormal2 + ", hovered=" + colorHovered2 + ", pressed=" + colorPressed2 + ")";
   }
}
