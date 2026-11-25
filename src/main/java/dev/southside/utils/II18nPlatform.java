package dev.southside.utils;

public interface II18nPlatform {
   String translate(String text, Object... objArray);

   String getKeyName(int n);
}
