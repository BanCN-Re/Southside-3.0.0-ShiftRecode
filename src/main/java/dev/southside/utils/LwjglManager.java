package dev.southside.utils;

import cc.polyfrost.oneconfig.internal.renderer.LwjglManagerImpl;
import cc.polyfrost.oneconfig.renderer.TinyFD;

@Deprecated
public interface LwjglManager {
   LwjglManager INSTANCE = new LwjglManagerImpl();

   @Deprecated
   NanoVGHelper getNanoVGHelper();

   @Deprecated
   ScissorHelper getScissorHelper();

   @Deprecated
   AssetHelper getAssetHelper();

   @Deprecated
   FontHelper getFontHelper();

   TinyFD getTinyFD();
}
