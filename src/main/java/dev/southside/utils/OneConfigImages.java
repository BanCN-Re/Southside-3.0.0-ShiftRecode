package dev.southside.utils;

public class OneConfigImages {
   public static final ImageAsset HUE_GRADIENT;
   public static final ImageAsset COLOR_WHEEL;
   public static final ImageAsset ALPHA_GRID;
   public static final ImageAsset VERTICAL_ALPHA_GRID;

   // $VF: Irreducible bytecode was duplicated to produce valid code
   static {
      HUE_GRADIENT = new ImageAsset("/assets/oneconfig/options/HueGradient.png");
      COLOR_WHEEL = new ImageAsset("/assets/oneconfig/options/ColorWheel.png");
      ALPHA_GRID = new ImageAsset("/assets/oneconfig/options/AlphaGrid.png");
      VERTICAL_ALPHA_GRID = new ImageAsset("/assets/oneconfig/options/VerticalAlphaGrid.png");
   }
}
