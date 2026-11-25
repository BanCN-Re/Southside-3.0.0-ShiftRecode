package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventBus;
import dev.southside.event.events.CustomEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class OneConfigEventManager {
   public static final OneConfigEventManager INSTANCE;
   public static final Logger LOGGER;
   @SerializedName("eventBus")
   public final EventBus eventBus = Southside.INSTANCE.getEventBus();

   public void unregister(Object obj) {
      this.eventBus.unsubscribe(obj);
   }

   public EventBus getEventBus() {
      return this.eventBus;
   }

   public void performAction() {
      this.post(new OneConfigInitEvent());
   }

   public void register(Object obj) {
      this.eventBus.subscribe(obj);
   }

   public OneConfigEventManager() {
      this.eventBus.subscribeTarget(this);
   }

   public void post(Object obj) {
      this.eventBus.postEvent(obj);
   }

   public void performActionEx() {
      this.post(new CustomEvent());
   }

   // $VF: Irreducible bytecode was duplicated to produce valid code
   static {
      String text = "OneConfig/EventManager";
      INSTANCE = new OneConfigEventManager();
      LOGGER = LogManager.getLogger(text);
   }

}
