package cc.polyfrost.oneconfig.utils;

import java.util.HashMap;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public final class SimpleProfiler {
   public static final HashMap<String, Long> longMap = new HashMap<>();
   public static final Logger LOGGER = LogManager.getLogger("OneConfig Profiler");

   public static float pop(@NotNull String text, @NotNull Level level) {
      float f = (float)(System.nanoTime() - longMap.remove(text)) / 1000000.0F;
      LOGGER.log(level, text + " took " + f + "ms");
      return f;
   }

   public static boolean matches(@NotNull String text) {
      return longMap.put(text, System.nanoTime()) == null;
   }

   public static float computeFloat(@NotNull String text) {
      return pop(text, Level.DEBUG);
   }

   static {
      short s = 13480;
      short s2 = -22132;
   }
}
