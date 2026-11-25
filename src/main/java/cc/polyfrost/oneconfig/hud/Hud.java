package cc.polyfrost.oneconfig.hud;

import cc.polyfrost.oneconfig.config.Config;
import com.google.gson.annotations.SerializedName;
import dev.southside.module.values.BooleanValue;
import dev.southside.render.UIMatrixStack;
import dev.southside.utils.HudPosition;
import dev.southside.utils.OneConfigPlatform;
import dev.southside.utils.IMinecraft;
import dev.southside.utils.ConfigField;
import dev.southside.utils.NumberValue;
import dev.southside.utils.OneConfigGui;
import dev.southside.utils.ButtonValue;
import net.minecraft.client.gui.DrawContext;

public abstract class Hud implements IMinecraft {
   @SerializedName("resetPosition")
   public ButtonValue resetPosition = new ButtonValue("Reset Position", "Reset", "Reset HUD to default position", 1, this::resetPosition);
   @SerializedName("locked")
   public BooleanValue locked = new BooleanValue("Locked", "If the position is locked", 2, false);
   @SerializedName("position")
   public ConfigField<HudPosition> position = new ConfigField<>("HUD Position", new HudPosition(null, 0.0F, 0.0F, 0.0F, 0.0F));
   @SerializedName("scale")
   public NumberValue scale = new NumberValue("Scale", 0.2F, 10.0F, "The scale of the HUD", false, 1);
   private transient Config config;
   @SerializedName("deltaTicks")
   public float deltaTicks;
   @SerializedName("defaultScale")
   private float defaultScale;
   @SerializedName("defaultPosition")
   private HudPosition defaultPosition;
   @SerializedName("showInChat")
   public BooleanValue showInChat = new BooleanValue("Show in Chat", true);
   @SerializedName("showInDebug")
   public BooleanValue showInDebug = new BooleanValue("Show in F3 (Debug)", false);
   @SerializedName("showInGuis")
   public BooleanValue showInGuis = new BooleanValue("Show in GUIs", true);

   public Hud(float f, float f2, int n, float f3) {
      this.scale.performAction(f3);
      this.position.setValue(new HudPosition(this, 0.0F, 0.0F, this.computeFloat(f3, true), this.setScale(f3, true)));
      this.position.getValue().anchor = HudPosition.getMode(n);
      this.defaultPosition = new HudPosition(this, 0.0F, 0.0F, this.computeFloat(f3, true), this.setScale(f3, true));
      this.defaultPosition.setProgress(f);
      this.defaultPosition.setProgressEx(f2);
      this.defaultPosition.anchor = HudPosition.getMode(n);
      this.defaultScale = f3;
   }

   public Hud(float x, float y, float z) {
      this(x, y, 0, z);
   }

   public Hud(float x, float y) {
      this(x, y, 1.0F);
   }

   public Hud() {
      this(0.0F, 0.0F, 1.0F);
   }

   public abstract void draw(DrawContext context, UIMatrixStack matrices, float f, float f2, float f3, boolean flag);

   public abstract float computeFloat(float f, boolean flag);

   public abstract float setScale(float f, boolean flag);

   public void setActive(boolean flag) {
   }

   public void resetPosition() {
      HudPosition defaultPosition2 = this.defaultPosition;
      float width = this.position.getValue().getWidth();
      float height = this.position.getValue().getHeight();
      this.scale.performAction(this.defaultScale);
      this.position.getValue().anchor = defaultPosition2.anchor;
      float otherAnchorX = this.position.getValue().anchor.x;
      float otherAnchorY = this.position.getValue().anchor.y;
      if (otherAnchorX == 0.0F) {
         this.position.getValue().setProgress(defaultPosition2.getProgress());
      } else if (otherAnchorX == 0.5F) {
         this.position.getValue().setProgress(defaultPosition2.getProgressDirect() - width / 2.0F);
      } else {
         this.position.getValue().setProgress(defaultPosition2.getProgressAlt() - width);
      }

      if (otherAnchorY == 0.0F) {
         this.position.getValue().setProgressEx(defaultPosition2.getProgressEx());
      } else if (otherAnchorY == 0.5F) {
         this.position.getValue().setProgressEx(defaultPosition2.getProgressSafe() - height / 2.0F);
      } else {
         this.position.getValue().setProgressEx(defaultPosition2.getProgressImpl() - height);
      }
   }

   public void drawAll(DrawContext context, UIMatrixStack matrices, boolean flag) {
      if (flag || this.isValid()) {
         this.setActive(flag);
         this.position
            .getValue()
            .performActionEx(this.computeFloat(((Number)this.scale.getValue()).floatValue(), flag), this.setScale(((Number)this.scale.getValue()).floatValue(), flag));
         this.draw(
            context, matrices, this.position.getValue().getProgress(), this.position.getValue().getProgressEx(), ((Number)this.scale.getValue()).floatValue(), flag
         );
      }
   }

   public boolean isValid() {
      if (!(Boolean)this.showInGuis.getValue()
         && OneConfigPlatform.getGuiPlatform().getScreen() != null
         && !(OneConfigPlatform.getGuiPlatform().getScreen() instanceof OneConfigGui)) {
         return false;
      } else {
         return !this.showInChat.getValue() && OneConfigPlatform.getGuiPlatform().isChatScreen()
            ? false
            : (Boolean)this.showInDebug.getValue() || !OneConfigPlatform.getGuiPlatform().shouldShowDebugHud();
      }
   }

   public boolean isValidEx() {
      return this.config == null || this.config.isEnabled();
   }

   public boolean isValidAlt() {
      return (Boolean)this.locked.getValue() && (this.config == null || this.config.isEnabled());
   }

   public void setConfig(Config config2) {
      this.config = config2;
   }

   public Config getConfig() {
      return this.config;
   }

   public float getProgress() {
      return ((Number)this.scale.getValue()).floatValue();
   }

   public void performAction(float f, boolean flag) {
      this.scale.performAction(f);
      this.position.getValue().performActionAlt(this.computeFloat(f, flag), this.setScale(f, flag));
   }
}
