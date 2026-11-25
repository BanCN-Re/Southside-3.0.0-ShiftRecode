package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.events.ButtonEvent;
import dev.southside.event.events.KeyCodeEvent;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.InputUtil.Key;
import net.minecraft.client.util.InputUtil.Type;
import org.lwjgl.glfw.GLFW;

public class KeyBindHandler {
   public static final KeyBindHandler INSTANCE = new KeyBindHandler();
   @SerializedName("keyBinds")
   private final ConcurrentHashMap<KeyBindHandler.Inner, OneKeyBind> keyBinds = new ConcurrentHashMap<>();

   public static boolean isValid(KeyBinding keyBinding) {
      Key key = InputUtil.fromTranslationKey(keyBinding.getBoundKeyTranslationKey());
      long handle = MinecraftClient.getInstance().getWindow().getHandle();
      int code = key.getCode();
      if (key.getCategory() == Type.MOUSE) {
         return code >= 0 && code <= 7 && GLFW.glfwGetMouseButton(handle, code) == 1;
      } else {
         return key.getCategory() != Type.KEYSYM ? keyBinding.isPressed() : code >= 32 && code <= 348 && GLFW.glfwGetKey(handle, code) == 1;
      }
   }

   public void performAction(Field field, Object obj, OneKeyBind oneKeyBind) {
      this.keyBinds.put(new KeyBindHandler.Inner(field, obj), oneKeyBind);
   }

   @EventTarget
   public void onButton(ButtonEvent buttonEvent) {
      if (buttonEvent.getAction() == 1) {
         this.performActionEx();
      }
   }

   public void performAction() {
      for (KeyBindHandler.Inner inner : this.keyBinds.keySet()) {
         if (inner.owner != null) {
            try {
               this.keyBinds.put(inner, (OneKeyBind)inner.field.get(inner.owner));
            } catch (IllegalAccessException e) {
            }
         }
      }
   }

   public void clear() {
      this.keyBinds.clear();
   }

   @EventTarget
   public void onKeyCode(KeyCodeEvent keyCodeEvent) {
      this.performActionEx();
   }

   private void performActionEx() {
      for (OneKeyBind oneKeyBind : this.keyBinds.values()) {
         if (oneKeyBind.isValid()) {
            oneKeyBind.performAction();
         }
      }
   }

   private static final class Inner {
      @SerializedName("field")
      public final Field field;
      @SerializedName("owner")
      public final Object owner;

      public Inner(Field field2, Object obj) {
         this.field = field2;
         this.owner = obj;
      }
   }
}
