package dev.southside.utils;

public interface IMousePlatform {
   double getCurrentValue();

   double getCurrentValueEx();

   double getCurrentValueAlt();

   double getCursorDeltaX();

   double getCursorDeltaY();

   int computeInt(int n);

   boolean isKeyDown(int n);
}
