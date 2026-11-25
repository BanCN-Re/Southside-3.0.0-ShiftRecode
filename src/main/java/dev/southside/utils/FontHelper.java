package dev.southside.utils;

public interface FontHelper {
   FontHelper INSTANCE = LwjglManager.INSTANCE.getFontHelper();

   void loadFont(long l, OneConfigFont font);

   void initialize(long l);
}
