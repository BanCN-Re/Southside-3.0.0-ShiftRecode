package dev.southside.module;

import cc.polyfrost.oneconfig.config.Config;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.module.impl.client.ClientSettings;
import dev.southside.module.impl.client.Notification;
import dev.southside.utils.KeyBindValue;
import dev.southside.utils.ISerializable;
import dev.southside.utils.IChatFeedback;
import dev.southside.utils.SouthsideSounds;
import dev.southside.utils.OneKeyBind;
import dev.southside.utils.NumberValue;
import dev.southside.utils.ModData;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.NotNull;

public abstract class Module extends Config implements IChatFeedback, ISerializable<Module>, Comparable<Module> {
   @SerializedName("bind")
   public KeyBindValue bind;
   public static MinecraftClient mc = MinecraftClient.getInstance();
   protected static final MinecraftClient mc2 = mc;
   @SerializedName("initPostRunnable")
   public Runnable initPostRunnable;
   @SerializedName("initialized")
   public boolean initialized = true;
   @SerializedName("random")
   public Random random = Random.create();
   @SerializedName("category")
   public Category category;
   @SerializedName("enabledByDefault")
   public boolean enabledByDefault;
   @SerializedName("name")
   public String name;
   @SerializedName("description")
   public String description;
   @SerializedName("infoText")
   public String infoText = "";
   @SerializedName("nameKey")
   public String nameKey;
   @SerializedName("visible")
   public boolean visible = true;

   public Module(Category category2, String text, String text2, int n, boolean flag, boolean flag2) {
      super(new ModData(text, category2));
      this.category = category2;
      this.name = text;
      this.description = text2;
      this.nameKey = text;
      OneKeyBind oneKeyBind = new OneKeyBind(n);
      this.bind = new KeyBindValue("Bind", "", 1, oneKeyBind);
      this.enabledByDefault = flag;
      this.visible = flag2;
   }

   public Module(Category category2, String text, String text2, int n, boolean flag) {
      this(category2, text, text2, n, flag, true);
   }

   public Module(Category category2, String text, String text2, int n) {
      this(category2, text, text2, n, false, true);
   }

   public Module(Category category2, String text, String text2, boolean flag) {
      this(category2, text, text2, -1, flag, true);
   }

   public Module(Category category2, String text, String text2) {
      this(category2, text, text2, -1, false, true);
   }

   public Module(Category category2, String text, int n, boolean flag) {
      this(category2, text, "NONE", n, flag, true);
   }

   public Module(Category category2, String text, int n) {
      this(category2, text, "NONE", n, false, true);
   }

   public Module(Category category2, String text, boolean flag) {
      this(category2, text, "NONE", -1, flag, true);
   }

   public Module(Category category2, String text) {
      this(category2, text, "NONE", -1, false, true);
   }

   public Module(Category category2, String text, boolean flag, boolean flag2) {
      this(category2, text, "NONE", -1, flag, flag2);
   }

   @Override
   public void initialize() {
      super.initialize();
      this.registerKeyBind((OneKeyBind)this.bind.getValue(), this::toggleFromKeybind);
      ((OneKeyBind)this.bind.getDefaultValue()).setRunnable(((OneKeyBind)this.bind.getValue()).getRunnable());
   }

   public void postInit() {
      if (this.initPostRunnable != null) {
         this.initPostRunnable.run();
      }
   }

   public void toggleFromKeybind() {
      if (mc.currentScreen == null) {
         this.setEnabled(!super.enabled.getValue());
      }
   }

   public void toggle() {
      this.setEnabled(!super.enabled.getValue());
   }

   public void setEnabled(boolean flag) {
      if (flag != super.enabled.getValue()) {
         float f = ((Number)ClientSettings.soundVolume.getValue()).floatValue();
         this.notifyToggle(flag);
         this.playToggleSound(flag, f);
         super.enabled.setValue(flag);
         if (flag) {
            this.onEnable();
            if (super.enabled.getValue()) {
               Southside.INSTANCE.getEventBus().subscribe(this);
            }
         } else {
            Southside.INSTANCE.getEventBus().unsubscribe(this);
            this.onDisable();
         }
      }
   }

   @Override
   public void setConfigEnabled(boolean flag) {
      this.setEnabled(flag);
   }

   private void notifyToggle(boolean flag) {
      String text = flag ? " on." : " off.";
      String nameKey2 = this.nameKey;
      String text2 = "Toggled " + nameKey2 + text;
      Notification.applyText("Module", text2, flag ? Notification.Mode.ENABLED : Notification.Mode.DISABLED);
   }

   private void playToggleSound(boolean flag, float f) {
      if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().world != null) {
         SoundEvent soundEvent = switch (ClientSettings.soundStyle.getDisplayTextEx()) {
            case "Sigma" -> flag ? SouthsideSounds.SIGMA_ACTIVATE : SouthsideSounds.SIGMA_DEACTIVATE;
            case "Piano" -> flag ? SouthsideSounds.PIANO_ON : SouthsideSounds.PIANO_OFF;
            case "Default" -> flag ? SouthsideSounds.TOGGLE_ON : SouthsideSounds.TOGGLE_OFF;
            default -> null;
         };
         if (soundEvent != null) {
            mc.world.playSound(mc.player, mc.player.getBlockPos(), soundEvent, SoundCategory.BLOCKS, f, 1.0F);
         }
      }
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public void addRangedValueRestrict(NumberValue numberValue, NumberValue numberValue2) {
      this.addListener(numberValue.getLabel(), () -> performAction(numberValue, numberValue2));
      this.addListener(numberValue2.getLabel(), () -> performActionEx(numberValue2, numberValue));
   }

   private static void performAction(NumberValue numberValue, NumberValue numberValue2) {
      if (((Number)numberValue.getValue()).floatValue() > ((Number)numberValue2.getValue()).floatValue()) {
         numberValue.performAction((Number)numberValue2.getValue());
      }
   }

   private static void performActionEx(NumberValue numberValue, NumberValue numberValue2) {
      if (((Number)numberValue.getValue()).floatValue() < ((Number)numberValue2.getValue()).floatValue()) {
         numberValue.performAction((Number)numberValue2.getValue());
      }
   }

   public final void loadConfigWithAliases(JsonObject json, Map<String, String> map) {
      if (json.has(this.modData.name) && json.get(this.modData.name).isJsonObject()) {
         JsonObject json2 = json.getAsJsonObject(this.modData.name).deepCopy();
         map.forEach((text, text2) -> {
            if (!json2.has(text) && json2.has(text2)) {
               json2.add(text, json2.get(text2));
            }
         });
         JsonObject json3 = new JsonObject();
         json3.add(this.modData.name, json2);
         super.loadFromJson(json3);
      } else {
         super.loadFromJson(json);
      }
   }

   @Override
   public JsonObject toJson() {
      return this.saveToJson();
   }

   public Module fromJson(JsonObject json) {
      if (json.has(this.modData.name)) {
         this.loadFromJson(json);
      }

      ModData modData = this.modData;
      this.addDependency(this, this.modData.defaultPage, modData);
      return this;
   }

   public int compareTo(@NotNull Module other) {
      return this.nameKey.compareTo(other.nameKey);
   }

   public String getConfigId() {
      return this.nameKey.toLowerCase().replace(' ', '-');
   }

   public int getHeightEx() {
      return 0;
   }

   public int getHeightAlt() {
      return this.getHeightEx();
   }

   public boolean isInitialized() {
      return this.initialized;
   }

   public boolean isEnabledByDefault() {
      return this.enabledByDefault;
   }

   public Runnable getInitPostRunnable() {
      return this.initPostRunnable;
   }

   public KeyBindValue getBind() {
      return this.bind;
   }

   public String getDescription() {
      return this.description;
   }

   public Random getRandom() {
      return this.random;
   }

   public boolean isVisible() {
      return this.visible;
   }

   public void setVisible(boolean flag) {
      this.visible = flag;
   }

   public Category getCategory() {
      return this.category;
   }

   public String getNameKey() {
      return this.nameKey;
   }

   public String getName() {
      return this.name;
   }

   public String getSuffix() {
      return this.infoText;
   }

   public void setInfoText(String text) {
      this.infoText = text;
   }
}
