package dev.southside.utils;

public interface IMinecraftClientExt {
   boolean hasTicked();

   int getInitialTickCount();

   void setScreenCloseFlag(boolean flag);

   boolean getScreenCloseFlag();

   void performAction();

   void setAttackCooldown(int n);

   void attack();
}
