package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventTarget;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Consumer;

public class LambdaEventListener implements IEventListener {
   @SerializedName("eventClass")
   public final Class<?> eventClass;
   @SerializedName("isStatic")
   public final boolean isStatic;
   @SerializedName("priority")
   public final int priority;
   @SerializedName("consumer")
   public Consumer<Object> consumer;

   @Override
   public Class<?> getEventClass() {
      return this.eventClass;
   }

   public LambdaEventListener(LambdaEventListener.Inner inner, Class<?> clazz, Object obj, Method method) {
      this.eventClass = method.getParameterTypes()[0];
      this.isStatic = Modifier.isStatic(method.getModifiers());
      this.priority = method.getAnnotation(EventTarget.class).priority();

      try {
         Lookup lookup = inner.getLookup(clazz);
         MethodType methodType = MethodType.methodType(void.class, this.eventClass);
         MethodHandle methodHandle = lookup.unreflect(method);
         MethodType methodType2 = this.isStatic ? MethodType.methodType(Consumer.class) : MethodType.methodType(Consumer.class, clazz);
         MethodHandle target = LambdaMetafactory.metafactory(lookup, "accept", methodType2, MethodType.methodType(void.class, Object.class), methodHandle, methodType).getTarget();
         this.consumer = this.isStatic ? (Consumer)target.invoke() : (Consumer)target.invoke((Object)obj);
      } catch (Throwable e) {
         Southside.LOGGER.error("Unexpected error in LambdaListener", e);
      }
   }

   @Override
   public void invoke(Object obj) {
      try {
         this.consumer.accept(obj);
      } catch (Throwable e) {
         Southside.LOGGER.error("Unexpected error in LambdaListener", e);
      }
   }

   @Override
   public boolean isIsStatic() {
      return this.isStatic;
   }

   @Override
   public int getPriority() {
      return this.priority;
   }

   @FunctionalInterface
   public interface Inner {
      Lookup getLookup(Class<?> clazz) throws IllegalAccessException;
   }
}
