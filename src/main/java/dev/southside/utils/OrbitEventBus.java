package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventBus;
import dev.southside.event.EventTarget;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class OrbitEventBus implements EventBus {
   @SerializedName("listenersByTarget")
   public final Map<Object, List<IEventListener>> listenersByTarget = Collections.synchronizedMap(new IdentityHashMap<>());
   @SerializedName("sharedListenersByClass")
   public final Map<Class<?>, List<IEventListener>> sharedListenersByClass = new ConcurrentHashMap<>();
   @SerializedName("listenersByEventClass")
   public final Map<Class<?>, List<IEventListener>> listenersByEventClass = new ConcurrentHashMap<>();
   @SerializedName("lambdaEntries")
   public final List<OrbitEventBus.Inner> lambdaEntries = new ArrayList<>();

   @Override
   public void registerLambdaFactory(LambdaEventListener.Inner inner) {
      synchronized (this.lambdaEntries) {
         this.lambdaEntries.add(new OrbitEventBus.Inner("", inner));
      }
   }

   @Override
   public boolean hasListeners(Class<?> clazz) {
      List list = this.listenersByEventClass.get(clazz);
      return list != null && !list.isEmpty();
   }

   @Override
   public Object postEvent(Object obj) {
      List list = this.listenersByEventClass.get(obj.getClass());
      if (list != null) {
         for (Object obj2 : list) {
            IEventListener ieventlistener = (IEventListener)obj2;
            ieventlistener.invoke(obj);
         }
      }

      return obj;
   }

   @Override
   public ICancellable postCancellableEvent(ICancellable icancellable) {
      List list = this.listenersByEventClass.get(icancellable.getClass());
      if (list != null) {
         icancellable.setCancelled(false);

         for (Object obj : list) {
            IEventListener ieventlistener = (IEventListener)obj;
            ieventlistener.invoke(icancellable);
            if (icancellable.isCancelled()) {
               break;
            }
         }
      }

      return icancellable;
   }

   @Override
   public void subscribeTarget(Object obj) {
      this.performAction(this.collectItems(obj.getClass(), obj), false);
   }

   @Override
   public void subscribeStatic(Class<?> clazz) {
      this.performAction(this.collectItems(clazz, null), true);
   }

   @Override
   public void subscribeListener(IEventListener ieventlistener) {
      this.performAction(ieventlistener, false);
   }

   public void performAction(List<IEventListener> list, boolean flag) {
      for (IEventListener ieventlistener : list) {
         this.performAction(ieventlistener, flag);
      }
   }

   public void performAction(IEventListener ieventlistener, boolean flag) {
      if (!flag || ieventlistener.isIsStatic()) {
         List list = this.listenersByEventClass.computeIfAbsent(ieventlistener.getEventClass(), clazz -> new CopyOnWriteArrayList<>());
         this.performAction(list, ieventlistener);
      }
   }

   public void performAction(List<IEventListener> list, IEventListener ieventlistener) {
      int n = 0;

      while (n < list.size() && ieventlistener.getPriority() <= ((IEventListener)list.get(n)).getPriority()) {
         n++;
      }

      list.add(n, ieventlistener);
   }

   @Override
   public void unsubscribeTarget(Object obj) {
      this.performActionEx(this.collectItems(obj.getClass(), obj), false);
   }

   @Override
   public void unsubscribeStatic(Class<?> clazz) {
      this.performActionEx(this.collectItems(clazz, null), true);
   }

   @Override
   public void unsubscribeListener(IEventListener ieventlistener) {
      List list = this.listenersByEventClass.get(ieventlistener.getEventClass());
      if (list != null) {
         list.remove(ieventlistener);
      }
   }

   public void performActionEx(List<IEventListener> list, boolean flag) {
      for (IEventListener ieventlistener : list) {
         List list2 = this.listenersByEventClass.get(ieventlistener.getEventClass());
         if (list2 != null && (!flag || ieventlistener.isIsStatic())) {
            list2.remove(ieventlistener);
         }
      }
   }

   public List<IEventListener> collectItems(Class<?> clazz, Object obj) {
      return obj == null
         ? this.sharedListenersByClass.computeIfAbsent(clazz, clazz2 -> this.collectItemsEx((Class<?>)clazz2, null))
         : this.listenersByTarget.computeIfAbsent(obj, obj2 -> this.collectItemsEx(clazz, obj2));
   }

   public List<IEventListener> collectItemsEx(Class<?> clazz, Object obj) {
      CopyOnWriteArrayList copyOnWriteArrayList = new CopyOnWriteArrayList();
      this.performAction(copyOnWriteArrayList, clazz, obj);
      return copyOnWriteArrayList;
   }

   public void performAction(List<IEventListener> list, Class<?> clazz, Object obj) {
      for (Method method : clazz.getDeclaredMethods()) {
         if (this.isValid(method)) {
            list.add(new LambdaEventListener(this.getInner(clazz), clazz, obj, method));
         }
      }

      Class superclass = clazz.getSuperclass();
      if (superclass != null) {
         this.performAction(list, superclass, obj);
      }
   }

   public boolean isValid(Method method) {
      return method.isAnnotationPresent(EventTarget.class)
         && method.getReturnType() == void.class
         && method.getParameterCount() == 1
         && !method.getParameters()[0].getType().isPrimitive();
   }

   public LambdaEventListener.Inner getInner(Class<?> clazz) {
      synchronized (this.lambdaEntries) {
         for (OrbitEventBus.Inner inner : this.lambdaEntries) {
            if (clazz.getName().startsWith(inner.classPrefix)) {
               return inner.lookupProvider;
            }
         }
      }

      throw new NoLambdaFactoryException(clazz);
   }

   public static class Inner {
      @SerializedName("classPrefix")
      public final String classPrefix;
      @SerializedName("lookupProvider")
      public final LambdaEventListener.Inner lookupProvider;

      public Inner(String text, LambdaEventListener.Inner inner) {
         this.classPrefix = text;
         this.lookupProvider = inner;
      }
   }
}
