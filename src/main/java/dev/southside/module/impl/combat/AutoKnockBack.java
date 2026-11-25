package dev.southside.module.impl.combat;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.DisableRotationEvent;
import dev.southside.event.events.MinecraftClientEvent;
import dev.southside.event.events.StateEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.movement.StrafeFix;
import dev.southside.module.impl.player.KeyPearl;
import dev.southside.module.impl.world.Scaffold;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.ValuePair;
import dev.southside.utils.EntityTargetUtils;
import dev.southside.utils.EntityPositionHistory;
import dev.southside.utils.NumberValue;
import dev.southside.utils.RotationManager;
import dev.southside.utils.NanoTimer;
import dev.southside.utils.Rotation;
import java.util.Optional;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class AutoKnockBack extends Module {
   @SerializedName("minRange")
   public NumberValue minRange = new NumberValue("Min Range", 3, 2, 10, 0.1F);
   @SerializedName("maxRange")
   public NumberValue maxRange = new NumberValue("Max Range", 8, 2, 16, 0.1F);
   @SerializedName("delay")
   public NumberValue delay = new NumberValue("Delay", 500, 0, 1000, 50);
   @SerializedName("autoSwitchOnlyMainHand")
   public BooleanValue autoSwitchOnlyMainHand = new BooleanValue("AutoSwitch(OnlyMainHand)", true);
   @SerializedName("noSpecialItems")
   public BooleanValue noSpecialItems = new BooleanValue("No Special Items", true);
   @SerializedName("prediction")
   public BooleanValue prediction = new BooleanValue("Prediction", true);
   @SerializedName("predictTicks")
   public NumberValue predictTicks = new NumberValue("Predict Ticks", 4, 1, 10, 1);
   @SerializedName("wallCheck")
   public BooleanValue wallCheck = new BooleanValue("Wall Check", true);
   @SerializedName("throwTimer")
   public NanoTimer throwTimer = new NanoTimer();
   @SerializedName("target")
   public PlayerEntity target;
   @SerializedName("previousSlot")
   public int previousSlot = -1;
   @SerializedName("throwQueued")
   public boolean throwQueued;

   public AutoKnockBack() {
      super(Category.Combat, "AutoKnockBack", "test");
   }

   public static Optional<PlayerEntity> getTarget(float x, float y) {
      double d = x * x;
      double d2 = y * y;
      PlayerEntity player = null;
      double max_value = Double.MAX_VALUE;

      for (Entity entity : Module.mc.world.getEntities()) {
         if (entity instanceof PlayerEntity player2 && player2.isAlive() && EntityTargetUtils.isLivingEntity(player2)) {
            double dist = Module.mc.player.squaredDistanceTo(player2);
            if (dist <= d2 && dist >= d && dist < max_value) {
               max_value = dist;
               player = player2;
            }
         }
      }

      return Optional.ofNullable(player);
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      if (stateEvent.getState() == Stage.PRE && Module.mc.player != null && Module.mc.world != null && (Boolean)this.prediction.getValue()) {
         for (Entity entity : Module.mc.world.getEntities()) {
            if (entity instanceof PlayerEntity player && player.isAlive() && EntityTargetUtils.isLivingEntity(player)) {
               double dist = Module.mc.player.squaredDistanceTo(player);
               double d = ((Number)this.maxRange.getValue()).floatValue();
               if (dist <= d * d) {
                  EntityPositionHistory.updateEntity(player);
               }
            }
         }
      }
   }

   @EventTarget
   public void onDisableRotation(DisableRotationEvent disableRotationEvent) {
      if (Module.mc.player != null && Module.mc.world != null && !Module.mc.player.isUsingItem()) {
         if (!Southside.INSTANCE.getModuleManager().getModule(Scaffold.class).isEnabled()
            && Module.mc.currentScreen == null
            && !Module.mc.options.attackKey.isPressed()) {
            if (!BowSpammer.activeValue && !KeyPearl.isWorking()) {
               Optional target2 = getTarget(((Number)this.minRange.getValue()).floatValue(), ((Number)this.maxRange.getValue()).floatValue());
               this.target = null;
               if (!target2.isEmpty()) {
                  PlayerEntity player = (PlayerEntity)target2.get();
                  this.target = player;
                  Rotation rotation;
                  if ((Boolean)this.prediction.getValue()) {
                     int n = ((Number)this.predictTicks.getValue()).intValue();
                     Vec3d vec = EntityPositionHistory.computePosition(player, n, (Boolean)this.wallCheck.getValue());
                     rotation = EntityPositionHistory.computeRotation(vec);
                  } else {
                     rotation = RotationManager.getBowRotationToEntity(player);
                  }

                  if (Module.mc.player.canSee(player)) {
                     if (this.throwTimer.hasTimePassedDouble((double)((Number)this.delay.getValue()).longValue())) {
                        boolean flag = (Boolean)this.autoSwitchOnlyMainHand.getValue() && this.computeInt() != -1;
                        if (flag || this.isValidEx()) {
                           RotationManager.rotationRequests.add(new ValuePair<>(rotation, 0));
                           RotationManager.moveFixMode = Southside.INSTANCE.getModuleManager().getModule(StrafeFix.class).getMoveFixMode();
                           this.throwQueued = true;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static boolean isValidItem(ItemStack stack) {
      return stack.getItem() == Items.SNOWBALL || stack.getItem() == Items.EGG;
   }

   private boolean isValidItemEx(ItemStack stack) {
      return isValidItem(stack) && (!(Boolean)this.noSpecialItems.getValue() || !stack.contains(DataComponentTypes.CUSTOM_NAME));
   }

   public boolean isValidEx() {
      return this.isValidItemEx(Module.mc.player.getOffHandStack());
   }

   private int computeInt() {
      if (!(Module.mc.player.getInventory().getMainHandStack().getItem() instanceof EnderPearlItem)
         && !(Module.mc.player.getInventory().getMainHandStack().getItem() instanceof BowItem)
         && !(Module.mc.player.getInventory().getMainHandStack().getItem() instanceof CrossbowItem)) {
         for (int i = 0; i < 9; i++) {
            if (this.isValidItemEx(Module.mc.player.getInventory().getStack(i))) {
               return i;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   @EventTarget
   public void onMinecraftClient(MinecraftClientEvent minecraftClientEvent) {
      if (Module.mc.player != null) {
         if (this.previousSlot != -1) {
            Module.mc.player.getInventory().selectedSlot = this.previousSlot;
            this.previousSlot = -1;
         }

         if (this.throwQueued && !Module.mc.player.usingItem) {
            if (this.throwTimer.hasTimePassedDouble((double)((Number)this.delay.getValue()).longValue())
               && this.isValidEx()
               && !(Boolean)this.autoSwitchOnlyMainHand.getValue()) {
               this.performAction(Hand.OFF_HAND);
            }

            if (this.throwTimer.hasTimePassedDouble((double)((Number)this.delay.getValue()).longValue())) {
               int n = this.computeInt();
               if (n != -1 && (Boolean)this.autoSwitchOnlyMainHand.getValue()) {
                  if (KillAura.targets.isEmpty() && Module.mc.player.getInventory().getMainHandStack().getItem() != Items.BOW) {
                     int n2 = this.computeInt();
                     if (n2 != -1) {
                        this.previousSlot = Module.mc.player.getInventory().selectedSlot;
                        Module.mc.player.getInventory().selectedSlot = n2;
                     }
                  }

                  if (isValidItem(Module.mc.player.getMainHandStack())) {
                     this.performAction(Hand.MAIN_HAND);
                  }
               }
            }
         }
      }
   }

   private void performAction(Hand hand) {
      ClientPlayerEntity player = Module.mc.player;
      ActionResult actionResult = Module.mc.interactionManager != null ? Module.mc.interactionManager.interactItem(player, hand) : ActionResult.FAIL;
      if (actionResult.isAccepted()) {
         Module.mc.player.swingHand(hand);
         this.throwTimer.reset();
         this.throwQueued = false;
      }
   }

   @Override
   public void onDisable() {
      super.onDisable();
      EntityPositionHistory.clear();
   }

   @Override
   public void postInit() {
      this.addRangedValueRestrict(this.minRange, this.maxRange);
      this.hideIf(this.predictTicks.getLabel(), () -> !(Boolean)this.prediction.getValue());
      this.hideIf(this.wallCheck.getLabel(), () -> !(Boolean)this.prediction.getValue());
   }
}
