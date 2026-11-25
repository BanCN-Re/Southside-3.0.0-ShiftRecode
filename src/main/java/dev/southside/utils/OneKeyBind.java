package dev.southside.utils;

import cc.polyfrost.oneconfig.libs.universal.UKeyboard;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

public class OneKeyBind {
   @SerializedName("keyBinds")
   public final ArrayList<Integer> keyBinds = new ArrayList<>();
   protected transient Runnable runnable;
   protected transient boolean active;

   public OneKeyBind(boolean flag, int... values) {
      for (int n : values) {
         this.keyBinds.add(flag ? n - 100 : n);
      }
   }

   public OneKeyBind(int... values) {
      this(false, values);
   }

   public OneKeyBind() {
   }

   public boolean isValid() {
      if (this.keyBinds.size() == 0) {
         return false;
      }

      for (int n : this.keyBinds) {
         if (!UKeyboard.isKeyDown(n)) {
            this.active = false;
            return false;
         }
      }

      return true;
   }

   public void performAction() {
      if (this.runnable != null && !this.active) {
         this.runnable.run();
         this.active = true;
      }
   }

   public String getDisplayText() {
      StringBuilder sb = new StringBuilder();

      for (int n : this.keyBinds) {
         if (sb.length() != 0) {
            sb.append(" + ");
         }

         sb.append(OneConfigPlatform.getI18nPlatform().getKeyName(n));
      }

      return sb.toString().trim();
   }

   public void onKeyTyped(int n) {
      this.performAction(n, false);
   }

   public void performAction(int n, boolean flag) {
      if (!this.keyBinds.contains(n)) {
         this.keyBinds.add(flag ? n - 100 : n);
      }
   }

   public void clear() {
      this.keyBinds.clear();
   }

   public int getHeight() {
      return this.keyBinds.size();
   }

   public void setRunnable(Runnable action) {
      this.runnable = action;
   }

   public Runnable getRunnable() {
      return this.runnable;
   }

   public ArrayList<Integer> getKeyBinds() {
      return this.keyBinds;
   }
}
