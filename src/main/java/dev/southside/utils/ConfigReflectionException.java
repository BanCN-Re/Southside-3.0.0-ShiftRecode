package dev.southside.utils;

public class ConfigReflectionException extends RuntimeException {
   public ConfigReflectionException(String text, Throwable t) {
      super(text, t);
   }

   public ConfigReflectionException() {
   }

   public ConfigReflectionException(String text) {
      super(text);
   }

   public ConfigReflectionException(Throwable t) {
      super(t);
   }

   protected ConfigReflectionException(String text, Throwable t, boolean flag, boolean flag2) {
      super(text, t, flag, flag2);
   }
}
