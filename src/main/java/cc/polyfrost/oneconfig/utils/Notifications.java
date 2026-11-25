package cc.polyfrost.oneconfig.utils;

import cc.polyfrost.oneconfig.internal.config.Preferences;
import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.RenderStageEvent;
import dev.southside.event.events.StateEvent;
import dev.southside.utils.EaseInOutCubicAnimation;
import dev.southside.utils.OneConfigSvgs;
import dev.southside.utils.WindowMetrics;
import dev.southside.utils.GuiUtils;
import dev.southside.utils.MinecraftAccess;
import dev.southside.utils.OneConfigPlatform;
import dev.southside.utils.NanoVGHelper;
import dev.southside.utils.OneConfigIcon;
import dev.southside.utils.OneKeyBind;
import dev.southside.utils.UiAnimation;
import dev.southside.utils.OneConfigGui;
import dev.southside.utils.HudNotification;
import dev.southside.utils.DummyAnimation;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import org.jetbrains.annotations.Nullable;

public final class Notifications {
   public static final Notifications INSTANCE = new Notifications();
   private static final float DEFAULT_FLOAT = 4000.0F;
   @SerializedName("notifications")
   private final LinkedHashMap<HudNotification, UiAnimation> notifications = new LinkedHashMap<>();
   @SerializedName("frameDelta")
   private float frameDelta;
   @SerializedName("welcomeAnimation")
   private UiAnimation welcomeAnimation;

   private Notifications() {
   }

   public void send(String text, String text2, @Nullable OneConfigIcon oneConfigIcon, float f, @Nullable Callable<Float> callable, @Nullable Runnable action) {
      HudNotification hudNotification = new HudNotification(text, text2, oneConfigIcon, f, callable, action);
      this.notifications.put(hudNotification, new DummyAnimation(-1.0F));
   }

   public void send(String text, String text2, float f, @Nullable Callable<Float> callable, @Nullable Runnable action) {
      this.send(text, text2, null, f, callable, action);
   }

   public void send(String text, String text2, @Nullable OneConfigIcon oneConfigIcon, float f, @Nullable Callable<Float> callable) {
      this.send(text, text2, oneConfigIcon, f, callable, null);
   }

   public void send(String text, String text2, @Nullable OneConfigIcon oneConfigIcon, float f, @Nullable Runnable action) {
      this.send(text, text2, oneConfigIcon, f, null, action);
   }

   public void send(String text, String text2, float f, @Nullable Callable<Float> callable) {
      this.send(text, text2, null, f, callable, null);
   }

   public void send(String text, String text2, float f, @Nullable Runnable action) {
      this.send(text, text2, null, f, null, action);
   }

   public void send(String text, String text2, @Nullable OneConfigIcon oneConfigIcon, @Nullable Callable<Float> callable) {
      this.send(text, text2, oneConfigIcon, 4000.0F, callable, null);
   }

   public void send(String text, String text2, @Nullable OneConfigIcon oneConfigIcon, @Nullable Runnable action) {
      this.send(text, text2, oneConfigIcon, 4000.0F, null, action);
   }

   public void send(String text, String text2, @Nullable Callable<Float> callable) {
      this.send(text, text2, null, 4000.0F, callable, null);
   }

   public void send(String text, String text2, @Nullable OneConfigIcon oneConfigIcon, float f) {
      this.send(text, text2, oneConfigIcon, f, null, null);
   }

   public void send(String text, String text2, @Nullable Runnable action) {
      this.send(text, text2, null, 4000.0F, null, action);
   }

   public void send(String text, String text2, float f) {
      this.send(text, text2, null, f, null, null);
   }

   public void send(String text, String text2, @Nullable OneConfigIcon oneConfigIcon) {
      this.send(text, text2, oneConfigIcon, 4000.0F, null, null);
   }

   public void send(String text, String text2) {
      this.send(text, text2, null, 4000.0F, null, null);
   }

   @EventTarget
   public void onPhaseEvent2(RenderStageEvent renderStageEvent) {
      if (renderStageEvent.phase == Stage.PRE) {
         this.frameDelta = this.frameDelta + GuiUtils.getProgress();
      } else if (this.notifications.isEmpty()) {
         this.frameDelta = 0.0F;
      } else {
         NanoVGHelper.INSTANCE.performAction(this::performAction);
         this.frameDelta = 0.0F;
      }
   }

   private void performAction(long l) {
      float f = -16.0F;
      float progressEx = OneConfigGui.getProgressEx();

      for (Entry entry : this.notifications.entrySet()) {
         UiAnimation uiAnimation = (UiAnimation)entry.getValue();
         if (uiAnimation.getProgressEx() == -1.0F) {
            entry.setValue(new DummyAnimation(f));
         } else if (f != uiAnimation.getProgressEx()) {
            entry.setValue(new EaseInOutCubicAnimation(250, uiAnimation.evaluate(0.0F), f, false));
         }

         float f2 = ((HudNotification)entry.getKey())
            .computeFloat(l, WindowMetrics.getFramebufferHeight() / progressEx + ((UiAnimation)entry.getValue()).evaluate(this.frameDelta), progressEx, this.frameDelta);
         f -= f2 + 16.0F;
      }

      this.notifications.entrySet().removeIf(entry2 -> entry2.getKey().isValid());
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      if (Preferences.firstLaunch.getValue()
         && stateEvent.getState() == Stage.PRE
         && !(OneConfigPlatform.getGuiPlatform().getScreen() instanceof OneConfigGui)
         && OneConfigPlatform.getServerPlatform().isValidEx()
         && MinecraftAccess.getMc().player.age > 20) {
         Preferences.firstLaunch.setValue(false);
         Preferences.getPreferences().saveToJson();
         this.welcomeAnimation = new EaseInOutCubicAnimation(4000, 0.0F, 1.0F, false);
         String text = "Press '" + ((OneKeyBind)Preferences.INSTANCE.oneConfigKeyBind.getValue()).getDisplayText() + "' to open the Click GUI.";
         this.send(
            "Welcome to Southside!",
            text,
            new OneConfigIcon(OneConfigSvgs.ICON_WHITE),
            -1.0F,
            this::getFloat_,
            () -> GuiUtils.openScreen(OneConfigGui.getSideBarUtil())
         );
      }
   }

   private Float getFloat_() {
      if (!(OneConfigPlatform.getGuiPlatform().getScreen() instanceof OneConfigGui) && !Preferences.activeValue) {
         return 0.0F;
      }

      Preferences.activeValue = true;
      Preferences.getPreferences().saveToJson();
      return this.welcomeAnimation.evaluate(GuiUtils.getProgress());
   }
}
