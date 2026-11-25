package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import java.util.function.Consumer;

public class ConsumerEventListener<T> implements IEventListener {
   @SerializedName("eventClass")
   public final Class<T> eventClass;
   @SerializedName("priority")
   public final int priority;
   @SerializedName("consumer")
   public final Consumer<T> consumer;

   public ConsumerEventListener(Class<T> clazz, Consumer<T> consumer2) {
      this.eventClass = clazz;
      this.priority = 0;
      this.consumer = consumer2;
   }

   @Override
   public Class<T> getEventClass() {
      return this.eventClass;
   }

   @Override
   public int getPriority() {
      return this.priority;
   }

   @Override
   public boolean isIsStatic() {
      return false;
   }

   @Override
   public void invoke(Object obj) {
      this.consumer.accept((T)obj);
   }
}
