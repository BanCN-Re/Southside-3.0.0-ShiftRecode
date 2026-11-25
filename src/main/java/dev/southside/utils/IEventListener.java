package dev.southside.utils;

public interface IEventListener {
   int getPriority();

   Class<?> getEventClass();

   @Deprecated
   boolean isIsStatic();

   void invoke(Object obj);
}
