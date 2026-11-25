package dev.southside.utils;

public interface AssetHelper {
   AssetHelper INSTANCE = LwjglManager.INSTANCE.getAssetHelper();
   int DEFAULT_INT_VALUE = 7;

   boolean loadImage(long l, String text, int n, Class<?> clazz);

   boolean isValid(long l, ImageAsset image);

   boolean isValid(long l, ImageAsset image, Class<?> clazz);

   boolean isValid(long l, String text, Class<?> clazz);

   boolean loadSvg(long vg, String text, float x, float y, int n);

   boolean isValid(long l, SvgAsset svg, float f, float f2);

   boolean isValid(long l, SvgAsset svg, float f, float f2, Class<?> clazz);

   boolean isValid(long vg, String text, float x, float y, Class<?> clazz);

   int getImage(String text);

   NanoVGImage get(String text);

   void performAction(long l, String text);

   void performAction(long l);

   int computeInt(String text, float f, float f2);

   NanoVGImage getEx(String text);

   void draw(long vg, String text, float x, float y);

   void performActionEx(long l);

}
