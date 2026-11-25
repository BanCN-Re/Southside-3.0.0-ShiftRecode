package dev.southside.module.impl.combat;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.events.CameraEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.IMinecraftClientExt;
import dev.southside.utils.NumberValue;
import dev.southside.utils.NanoTimer;
import net.minecraft.item.BlockItem;
import org.apache.commons.lang3.RandomUtils;

public class AutoClicker extends Module {
   @SerializedName("leftClick")
   public BooleanValue leftClick = new BooleanValue("Left Click", true);
   @SerializedName("minLeftCps")
   public NumberValue minLeftCps = new NumberValue("Min Left CPS", 8.0, 0.0, 20.0, 1.0);
   @SerializedName("maxLeftCps")
   public NumberValue maxLeftCps = new NumberValue("Max Left CPS", 10.0, 0.0, 20.0, 1.0);
   @SerializedName("stopWhileBreaking")
   public BooleanValue stopWhileBreaking = new BooleanValue("Stop While Break", true);
   @SerializedName("rightClick")
   public BooleanValue rightClick = new BooleanValue("Right Click", true);
   @SerializedName("minRightCps")
   public NumberValue minRightCps = new NumberValue("Min Right CPS", 8.0, 0.0, 20.0, 1.0);
   @SerializedName("maxRightCps")
   public NumberValue maxRightCps = new NumberValue("Max Right CPS", 10.0, 0.0, 20.0, 1.0);
   @SerializedName("onlyBlocks")
   public BooleanValue onlyBlocks = new BooleanValue("Only Blocks", true);
   @SerializedName("leftClickTimer")
   private final NanoTimer leftClickTimer = new NanoTimer();
   @SerializedName("rightClickTimer")
   private final NanoTimer rightClickTimer = new NanoTimer();
   @SerializedName("leftClickDelay")
   private long leftClickDelay;
   @SerializedName("rightClickDelay")
   private long rightClickDelay;

   public AutoClicker() {
      super(Category.Combat, "AutoClicker", "Auto Simulation Click");
   }

   @Override
   public void postInit() {
      super.postInit();
      this.addRangedValueRestrict(this.minLeftCps, this.maxLeftCps);
      this.addRangedValueRestrict(this.minRightCps, this.maxRightCps);
   }

   @EventTarget
   public void onCamera(CameraEvent cameraEvent) {
      if (Module.mc.player != null) {
         this.performActionAlt();
         this.initPostRunnable();
      }
   }

   private void performActionAlt() {
      if ((Boolean)this.leftClick.getValue()
         && this.leftClickTimer.hasTimePassedDouble((double)this.leftClickDelay)
         && Module.mc.options.attackKey.isPressed()
         && (!(Boolean)this.stopWhileBreaking.getValue() || !Module.mc.interactionManager.isBreakingBlock())) {
         this.leftClickTimer.reset();
         ((IMinecraftClientExt)Module.mc).setAttackCooldown(0);
         ((IMinecraftClientExt)Module.mc).attack();
         this.leftClickDelay = getDurationMs(this.minLeftCps, this.maxLeftCps);
      }
   }

   private void initPostRunnable() {
      if ((Boolean)this.rightClick.getValue()
         && this.rightClickTimer.hasTimePassedDouble((double)this.rightClickDelay)
         && Module.mc.options.useKey.isPressed()
         && (
            !(Boolean)this.onlyBlocks.getValue()
               || Module.mc.player.getMainHandStack().getItem() instanceof BlockItem
               || Module.mc.player.getOffHandStack().getItem() instanceof BlockItem
         )) {
         this.rightClickTimer.reset();
         ((IMinecraftClientExt)Module.mc).performAction();
         this.rightClickDelay = getDurationMs(this.minRightCps, this.maxRightCps);
      }
   }

   private static long getDurationMs(NumberValue numberValue, NumberValue numberValue2) {
      return (long)(1000.0 / RandomUtils.nextDouble(((Number)numberValue.getValue()).doubleValue(), ((Number)numberValue2.getValue()).doubleValue()));
   }
}
