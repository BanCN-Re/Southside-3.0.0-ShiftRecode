package cc.polyfrost.oneconfig.internal.config;

import com.google.gson.annotations.SerializedName;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.InternalConfig;
import dev.southside.utils.KeyBindValue;
import dev.southside.utils.IBlurHandler;
import dev.southside.utils.OneConfigPlatform;
import dev.southside.utils.OneKeyBind;
import dev.southside.utils.ConfigField;
import dev.southside.utils.NumberValue;
import dev.southside.utils.IntegerValue;
import dev.southside.utils.ModeValue;
import dev.southside.utils.TickDelayTask;
import dev.southside.utils.OneConfigGui;
import dev.southside.utils.ButtonValue;

public class Preferences extends InternalConfig {
   public static Preferences INSTANCE;
   @SerializedName("oneConfigKeyBind")
   public KeyBindValue oneConfigKeyBind = new KeyBindValue("Click GUI OneKeyBind", "Choose what key opens the OneConfig UI", 2, new OneKeyBind(344)) {
      @Override
      public String getDisplayText() {
         return "GUI Settings";
      }
   };
   @SerializedName("imeSupport")
   public BooleanValue imeSupport = new BooleanValue("IME Support", "Enable composition text and cursor-following IME candidates on Windows", 2, true) {
      @Override
      public String getDisplayText() {
         return "GUI Settings";
      }
   };
   @SerializedName("enableBlur")
   public BooleanValue enableBlur = new BooleanValue("Enable Blur", true) {
      @Override
      public String getDisplayText() {
         return "GUI Settings";
      }
   };
   @SerializedName("blurMaxRadius")
   public NumberValue blurMaxRadius = new NumberValue(
      "Blur Max Radius", 1.0F, 50.0F, "Maximum blur radius. Higher values look stronger but cost more FPS.", false, 0.1F, 5.0F
   ) {
      @Override
      public String getDisplayText() {
         return "GUI Settings";
      }
   };
   @SerializedName("blurFadeStep")
   public NumberValue blurFadeStep = new NumberValue(
      "Blur Fade Step", 0.01F, 2.0F, "Blur animation speed per frame. Higher values fade faster and are lighter on FPS.", false, 0.01F, 0.25F
   ) {
      @Override
      public String getDisplayText() {
         return "GUI Settings";
      }
   };
   @SerializedName("blurSampleRate")
   public NumberValue blurSampleRate = new NumberValue(
      "Blur Sample Rate",
      1.0F,
      4.0F,
      "Reduces blur sampling density while keeping blur range. 1.0 is full quality; higher values improve FPS.",
      false,
      0.1F,
      1.0F
   ) {
      @Override
      public String getDisplayText() {
         return "GUI Settings";
      }
   };
   @SerializedName("enableCustomScale")
   public BooleanValue enableCustomScale = new BooleanValue("Use custom GUI scale", false) {
      @Override
      public String getDisplayText() {
         return "GUI Settings";
      }
   };
   @SerializedName("enableCustomNotificationScale")
   public BooleanValue enableCustomNotificationScale = new BooleanValue("Use custom Notification scale", false) {
      @Override
      public String getDisplayText() {
         return "GUI Settings";
      }
   };
   @SerializedName("customScale")
   public NumberValue customScale = new NumberValue("Custom GUI scale", 0.5F, 2.0F, "", false, 1.0F) {
      @Override
      public String getDisplayText() {
         return "GUI Settings";
      }
   };
   @SerializedName("customNotificationScale")
   public NumberValue customNotificationScale = new NumberValue("Custom Notification scale", 0.5F, 2.0F, "", false, 1.0F) {
      @Override
      public String getDisplayText() {
         return "GUI Settings";
      }
   };
   @SerializedName("openingBehavior")
   public ModeValue openingBehavior = new ModeValue(
      "Opening Behavior", new String[]{"Mods", "Preferences", "Previous page", "Smart reset"}, "Choose which page will show when you open OneConfig", 2, 3
   ) {
      @Override
      public String getA() {
         return "Behavior";
      }

      @Override
      public String getDisplayText() {
         return "GUI Settings";
      }
   };
   @SerializedName("showPageAnimationOnOpen")
   public BooleanValue showPageAnimationOnOpen = new BooleanValue(
      "Show opening page animation", "Whether or not to show the page switch animation when opening OneConfig", 2, false
   ) {
      @Override
      public String getA() {
         return "Behavior";
      }

      @Override
      public String getDisplayText() {
         return "GUI Settings";
      }
   };
   @SerializedName("timeUntilReset")
   public NumberValue timeUntilReset = new NumberValue(
      "Time before reset", 5.0F, 60.0F, "How much time (in seconds) before opening back to the \"Mods\" page", false, 15
   ) {
      @Override
      public String getA() {
         return "Behavior";
      }

      @Override
      public String getDisplayText() {
         return "GUI Settings";
      }
   };
   @SerializedName("searchDistance")
   public IntegerValue searchDistance = new IntegerValue("Search Distance", 0.0F, 10.0F, "The maximum Levenshtein distance to search for similar config names", 2) {
      @Override
      public String getA() {
         return "Behavior";
      }

      @Override
      public String getDisplayText() {
         return "Search";
      }
   };
   @SerializedName("guiOpenAnimation")
   public BooleanValue guiOpenAnimation = new BooleanValue("Opening Animation", "Plays an animation when opening the GUI", true) {
      @Override
      public String getA() {
         return "Animations";
      }

      @Override
      public String getDisplayText() {
         return "Global";
      }
   };
   @SerializedName("guiClosingAnimation")
   public BooleanValue guiClosingAnimation = new BooleanValue("Closing Animation", "Plays an animation when closing the GUI", true) {
      @Override
      public String getA() {
         return "Animations";
      }

      @Override
      public String getDisplayText() {
         return "Global";
      }
   };
   @SerializedName("animationTime")
   public NumberValue animationTime = new NumberValue("Opening Time", 0.05F, 2.0F, "The duration of the opening and closing animations, in seconds", false, 0.6F) {
      @Override
      public String getA() {
         return "Animations";
      }

      @Override
      public String getDisplayText() {
         return "Global";
      }
   };
   @SerializedName("animationType")
   public ModeValue animationType = new ModeValue("Opening Type", new String[]{"Subtle", "Full"}, "The type of opening/closing animation to use", 1, 0) {
      @Override
      public String getA() {
         return "Animations";
      }

      @Override
      public String getDisplayText() {
         return "Global";
      }
   };
   @SerializedName("showPageAnimations")
   public BooleanValue showPageAnimations = new BooleanValue("Show Page Animations", "Enables or disables the page switch animation", true) {
      @Override
      public String getA() {
         return "Animations";
      }

      @Override
      public String getDisplayText() {
         return "Pages";
      }
   };
   @SerializedName("pageAnimationDuration")
   public NumberValue pageAnimationDuration = new NumberValue(
      "Page Animation Duration", 0.1F, 0.6F, "The duration of the page switch animation, in seconds", false, 0.3F
   ) {
      @Override
      public String getA() {
         return "Animations";
      }

      @Override
      public String getDisplayText() {
         return "Pages";
      }
   };
   @SerializedName("toggleSwitchBounce")
   public BooleanValue toggleSwitchBounce = new BooleanValue("Toggle Switch Bounce", "Enables or disables the bounce animation on toggle switches", true) {
      @Override
      public String getA() {
         return "Animations";
      }

      @Override
      public String getDisplayText() {
         return "Toggles";
      }
   };
   @SerializedName("trackerResponseDuration")
   public NumberValue trackerResponseDuration = new NumberValue(
      "Tracker Response Time", 0.0F, 100.0F, "The time it takes for the slider tracker to move, in milliseconds", false, 60
   ) {
      @Override
      public String getA() {
         return "Animations";
      }

      @Override
      public String getDisplayText() {
         return "Sliders";
      }
   };
   @SerializedName("button")
   public ButtonValue button = new ButtonValue("Show First Launch Message", "Show", "Shows the first launch message again", 1, () -> {
      firstLaunch.setValue(true);
      activeValue = false;
      this.saveToJson();
   });
   public static ConfigField<Boolean> firstLaunch = new ConfigField<>("First Launch", true);
   public static transient boolean activeValue = false;

   public Preferences() {
      super("Preferences", "Preferences.json");
      INSTANCE = this;
      this.initialize();
      this.addListener(this.enableBlur.getLabel(), (Runnable) Preferences::performActionAlt);
      this.addListener(this.blurMaxRadius.getLabel(), (Runnable) Preferences::performActionAlt);
      this.addListener(this.blurFadeStep.getLabel(), (Runnable) Preferences::performActionAlt);
      this.addListener(this.blurSampleRate.getLabel(), (Runnable) Preferences::performActionAlt);
      this.registerKeyBind(
         (OneKeyBind)this.oneConfigKeyBind.getValue(), () -> new TickDelayTask(() -> OneConfigPlatform.getGuiPlatform().performAction(OneConfigGui.getSideBarUtil()), 1)
      );
      this.addListener(this.animationTime.getLabel(), () -> {
         if ((Boolean)INSTANCE.guiOpenAnimation.getValue()) {
            OneConfigGui.INSTANCE.isClosed = true;
         }
      });
      this.addDependency(this.blurMaxRadius.getLabel(), this.enableBlur.getLabel());
      this.addDependency(this.blurFadeStep.getLabel(), this.enableBlur.getLabel());
      this.addDependency(this.blurSampleRate.getLabel(), this.enableBlur.getLabel());
      this.addDependency(this.guiClosingAnimation.getLabel(), this.guiOpenAnimation.getLabel());
      this.addDependency(this.timeUntilReset.getLabel(), "Smart Opening Behavior", () -> this.openingBehavior.getValue() == 3);
      this.addDependency(this.pageAnimationDuration.getLabel(), this.showPageAnimations.getLabel());
      INSTANCE = this;
   }

   private static void performActionAlt() {
      IBlurHandler.INSTANCE.performAction(OneConfigPlatform.getGuiPlatform().getScreen());
   }

   public static Preferences getPreferences() {
      return INSTANCE == null ? (INSTANCE = new Preferences()) : INSTANCE;
   }
}
