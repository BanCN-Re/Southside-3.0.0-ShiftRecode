package dev.southside.utils;

public interface IGuiPlatform {
   Object getScreen();

   void performAction(Object obj);

   boolean isChatScreen();

   boolean shouldShowDebugHud();

   void performAction();
}
