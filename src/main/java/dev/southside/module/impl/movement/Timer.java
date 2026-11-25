package dev.southside.module.impl.movement;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.StateEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.values.BooleanValue;
import dev.southside.render.ProgressBarOverlay;
import dev.southside.utils.TimerSpeedManager;
import dev.southside.utils.NumberValue;
import org.lwjgl.glfw.GLFW;

public class Timer extends Module {
   @SerializedName("timerSpeed")
   public final NumberValue timerSpeed = new NumberValue("Timer Speed", 1.6F, 1.0F, 5.0F, 0.1F);
   @SerializedName("ticks")
   public final NumberValue ticks = new NumberValue("Ticks", 8, 1, 20, 1);
   @SerializedName("showText")
   public final BooleanValue showText = new BooleanValue("ProgressBar", true);
   @SerializedName("autoHide")
   public final BooleanValue autoHide = new BooleanValue("Auto Hide", false);
   @SerializedName("balance")
   private float balance;
   @SerializedName("progressHandle")
   private ProgressBarOverlay.Inner progressHandle;

   private void performActionAlt() {
      if (this.progressHandle != null) {
         this.progressHandle.performActionAlt();
         this.progressHandle = null;
      }
   }

   public Timer() {
      super(Category.Movement, "Timer", "Allows you to speed up or slow down time");
   }

   @Override
   public void postInit() {
      super.postInit();
      this.hideIf(this.autoHide.getLabel(), () -> !(Boolean)this.showText.getValue());
   }

   @Override
   public void onDisable() {
      this.balance = 0.0F;
      TimerSpeedManager.setTimerSpeed(1.0F);
      this.performActionAlt();
      super.onDisable();
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      if (mc2.player != null && mc2.world != null) {
         if (stateEvent.getState() == Stage.PRE) {
            boolean flag = Module.mc.options.forwardKey.isPressed()
               || Module.mc.options.backKey.isPressed()
               || Module.mc.options.leftKey.isPressed()
               || Module.mc.options.rightKey.isPressed();
            boolean isOnGround = Module.mc.player.isOnGround();
            if (GLFW.glfwGetMouseButton(Module.mc.getWindow().getHandle(), 3) == 1 && this.balance > 0.1F) {
               float f = ((Number)this.timerSpeed.getValue()).floatValue();
               if (this.balance >= f - 1.0F) {
                  TimerSpeedManager.setTimerSpeed(f);
                  this.balance -= f - 1.0F;
               } else {
                  TimerSpeedManager.setTimerSpeed(1.0F);
               }
            } else if (!(this.balance < ((Number)this.ticks.getValue()).doubleValue())) {
               TimerSpeedManager.setTimerSpeed(1.0F);
            } else {
               float f2 = !flag && isOnGround ? 0.8F : 0.96F;
               TimerSpeedManager.setTimerSpeed(f2);
               this.balance += 1.0F - f2;
            }

            this.balance = (float)Math.max(0.0, Math.min(this.balance, ((Number)this.ticks.getValue()).doubleValue()));
         }

         this.update();
      } else {
         this.balance = 0.0F;
         TimerSpeedManager.setTimerSpeed(1.0F);
      }
   }

   private void update() {
      if (!(Boolean)this.showText.getValue()) {
         this.performActionAlt();
      } else {
         float f = (float)(this.balance / ((Number)this.ticks.getValue()).doubleValue());
         f = Math.max(0.0F, Math.min(1.0F, f));
         int n = Math.round(f * 100.0F);
         if ((Boolean)this.autoHide.getValue() && n >= 100) {
            this.performActionAlt();
         } else {
            ProgressBarOverlay progressBarOverlay = Southside.INSTANCE.getProgressBarOverlay();
            if (progressBarOverlay != null) {
               int n2 = Math.max(1, ((Number)this.ticks.getValue()).intValue() * 1000);
               if (this.progressHandle == null || !this.progressHandle.isValid()) {
                  this.progressHandle = progressBarOverlay.getInner(n2, "Timer");
               }

               this.progressHandle.onKeyTyped((int)(f * n2));
            }
         }
      }
   }
}
