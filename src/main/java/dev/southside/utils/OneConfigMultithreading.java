package dev.southside.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class OneConfigMultithreading {
   private static final ExecutorService executorService;
   private static final ScheduledExecutorService scheduledExecutorService;

   public static void getFutureEx(Runnable action) {
      getFuture(action);
   }

   public static void performAction(Runnable... actionArray) {
      for (Runnable action : actionArray) {
         getFutureEx(action);
      }
   }

   public static Future<?> getFuture(Runnable action) {
      return executorService.submit(action);
   }

   public static void getScheduledFutureEx(Runnable action, long l, TimeUnit timeUnit) {
      getScheduledFuture(action, l, timeUnit);
   }

   public static ScheduledFuture<?> getScheduledFuture(Runnable action, long l, TimeUnit timeUnit) {
      return scheduledExecutorService.schedule(action, l, timeUnit);
   }

   static {
      executorService = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("OneConfig-%d").build());
      scheduledExecutorService = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() + 1);
   }

}
