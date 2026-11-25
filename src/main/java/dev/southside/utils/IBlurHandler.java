package dev.southside.utils;

import cc.polyfrost.oneconfig.internal.gui.impl.BlurHandlerImpl;

public interface IBlurHandler {
   IBlurHandler INSTANCE = new BlurHandlerImpl();

   void performAction(Object obj);
}
