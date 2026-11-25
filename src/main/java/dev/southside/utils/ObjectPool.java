package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayDeque;
import java.util.Queue;

public class ObjectPool<T> {
   @SerializedName("queue")
   public final Queue<T> queue = new ArrayDeque<>();
   @SerializedName("factory")
   public final IObjectFactory<T> factory;

   public ObjectPool(IObjectFactory<T> iobjectfactory) {
      this.factory = iobjectfactory;
   }

   public synchronized T getT() {
      return this.queue.isEmpty() ? this.factory.getT() : this.queue.poll();
   }

   public synchronized void forEach(Iterable<T> iterable) {
      iterable.forEach(this.queue::add);
   }

   public synchronized void offer(T val) {
      this.queue.offer((T)val);
   }
}
