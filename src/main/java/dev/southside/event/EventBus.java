package dev.southside.event;

import dev.southside.utils.LambdaEventListener;
import dev.southside.utils.ICancellable;
import dev.southside.utils.IEventListener;

public interface EventBus {
   void registerLambdaFactory(LambdaEventListener.Inner inner);

   boolean hasListeners(Class<?> clazz);

   Object postEvent(Object obj);

   ICancellable postCancellableEvent(ICancellable icancellable);

   void subscribeTarget(Object obj);

   void subscribeStatic(Class<?> clazz);

   void subscribeListener(IEventListener ieventlistener);

   void unsubscribeTarget(Object obj);

   void unsubscribeStatic(Class<?> clazz);

   void unsubscribeListener(IEventListener ieventlistener);

   default Object dispatch(Object obj) {
      return this.postEvent(obj);
   }

   default Object post(Object obj) {
      return this.postEvent(obj);
   }

   default ICancellable postCancellable(ICancellable icancellable) {
      return this.postCancellableEvent(icancellable);
   }

   default void subscribe(Object obj) {
      this.subscribeTarget(obj);
   }

   default void subscribeClass(Class<?> clazz) {
      this.subscribeStatic(clazz);
   }

   default void registerListener(IEventListener ieventlistener) {
      this.subscribeListener(ieventlistener);
   }

   default void unsubscribe(Object obj) {
      this.unsubscribeTarget(obj);
   }

   default void unsubscribeClass(Class<?> clazz) {
      this.unsubscribeStatic(clazz);
   }

   default void unregisterListener(IEventListener ieventlistener) {
      this.unsubscribeListener(ieventlistener);
   }
}
