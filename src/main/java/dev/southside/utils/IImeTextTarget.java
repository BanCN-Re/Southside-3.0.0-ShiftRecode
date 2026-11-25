package dev.southside.utils;

public interface IImeTextTarget {
   void commitText(String text);

   default void performActionEx() {
   }
}
