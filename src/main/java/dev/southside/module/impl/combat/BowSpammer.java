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
import dev.southside.module.impl.movement.NoSlow;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.ValuePair;
import dev.southside.utils.ItemScoreUtils;
import dev.southside.utils.EntityTargetUtils;
import dev.southside.utils.NumberValue;
import dev.southside.utils.ModeValue;
import dev.southside.utils.RotationManager;
import dev.southside.utils.Rotation;
import java.util.Comparator;
import java.util.stream.StreamSupport;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class BowSpammer extends Module {
   private static final int DEFAULT_INT = 3;
   @SerializedName("minRange")
   public final NumberValue minRange = new NumberValue("Min Range", 10, 1, 100, 0.1F);
   @SerializedName("maxRange")
   public final NumberValue maxRange = new NumberValue("Max Range", 50, 1, 100, 0.1F);
   @SerializedName("predictMovement")
   public final BooleanValue predictMovement = new BooleanValue("Predict Movement", true);
   @SerializedName("fieldOfView")
   public final NumberValue fieldOfView = new NumberValue("Fov", 360, 10, 360);
   @SerializedName("priority")
   public final ModeValue priority = new ModeValue("Priority", "Distance", "Distance", "Health");
   @SerializedName("state")
   private BowSpammer.Mode2 state = BowSpammer.Mode2.IDLE;
   @SerializedName("weaponType")
   private BowSpammer.Mode weaponType = BowSpammer.Mode.NONE;
   @SerializedName("target")
   private LivingEntity target;
   @SerializedName("aimTicks")
   private int aimTicks;
   @SerializedName("previousSlot")
   private int previousSlot = -1;
   public static boolean activeValue;

   public BowSpammer() {
      super(Category.Combat, "BowSpammer", "Rapidly draws and fires bow/crossbow at targets");
   }

   @Override
   public void postInit() {
      super.postInit();
      this.addRangedValueRestrict(this.minRange, this.maxRange);
   }

   @Override
   public void onEnable() {
      this.state = BowSpammer.Mode2.IDLE;
      this.weaponType = BowSpammer.Mode.NONE;
      this.target = null;
      this.aimTicks = 0;
      this.previousSlot = -1;
      activeValue = false;
   }

   @Override
   public void onDisable() {
      if (Module.mc.player != null) {
         if (this.state == BowSpammer.Mode2.DRAWING) {
            Module.mc.options.useKey.setPressed(false);
         }

         this.performActionDirect();
      }

      this.performActionAlt();
   }

   private void performActionAlt() {
      this.state = BowSpammer.Mode2.IDLE;
      this.weaponType = BowSpammer.Mode.NONE;
      this.target = null;
      this.aimTicks = 0;
      activeValue = false;
   }

   public void performActionImpl() {
      Module.mc.options.useKey.setPressed(false);
      if (this.previousSlot != -1 && Module.mc.player != null) {
         this.performActionDirect();
      }

      this.performActionAlt();
   }

   private void performActionDirect() {
      if (this.previousSlot != -1) {
         Module.mc.player.getInventory().selectedSlot = this.previousSlot;
         this.previousSlot = -1;
      }
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      if (stateEvent.getState() == Stage.PRE
         && MinecraftClient.getInstance() != null
         && MinecraftClient.getInstance().player != null
         && MinecraftClient.getInstance().world != null) {
         NoSlow noSlow = Southside.INSTANCE.getModuleManager().getModule(NoSlow.class);
         if (noSlow.isEnabled() && !(Boolean)noSlow.old.getValue()) {
            if (!this.isValidEx()) {
               if (this.state != BowSpammer.Mode2.IDLE) {
                  this.performActionImpl();
               }

               this.setInfoText("No Arrows");
            } else {
               switch (this.state) {
                  case IDLE:
                     this.performActionSafe();
                     break;
                  case SWAPPING:
                     this.performActionCustom();
                     break;
                  case DRAWING:
                     this.performActionByIndex();
                     break;
                  case AIMING:
                     this.performActionHelper();
                     break;
                  case FIRING:
                     this.performActionV2();
               }

               activeValue = this.state != BowSpammer.Mode2.IDLE;
            }
         } else {
            if (this.state != BowSpammer.Mode2.IDLE) {
               this.performActionImpl();
            }

            this.setInfoText(noSlow.isEnabled() ? "NoSlow Old" : "NoSlow OFF");
         }
      }
   }

   @EventTarget(priority = -100)
   public void onDisableRotation(DisableRotationEvent disableRotationEvent) {
      if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().world != null) {
         if ((this.state == BowSpammer.Mode2.AIMING || this.state == BowSpammer.Mode2.FIRING) && this.target != null && this.target.isAlive()) {
            Rotation rotation = this.computeRotation(this.target);
            if (rotation != null) {
               RotationManager.rotationRequests.add(new ValuePair<>(rotation, 2));
            }
         }
      }
   }

   @EventTarget(priority = -200)
   public void onMinecraftClient(MinecraftClientEvent minecraftClientEvent) {
      if (Module.mc.player != null && this.state == BowSpammer.Mode2.FIRING && this.weaponType == BowSpammer.Mode.CROSSBOW) {
         ItemStack mainHandStack = Module.mc.player.getMainHandStack();
         if (mainHandStack.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(mainHandStack)) {
            Module.mc.interactionManager.interactItem(Module.mc.player, Hand.MAIN_HAND);
            this.performActionDirect();
            this.aimTicks = 0;
            this.state = BowSpammer.Mode2.IDLE;
            activeValue = false;
            this.setInfoText("Fired");
         }
      }
   }

   private void performActionSafe() {
      this.target = this.getLivingEntity();
      if (this.target == null) {
         this.setInfoText("No Target");
      } else {
         int n = this.computeInt();
         if (n == -1) {
            this.setInfoText("No Weapon");
         } else {
            ItemStack stack = Module.mc.player.getInventory().getStack(n);
            if (stack.getItem() instanceof BowItem) {
               this.weaponType = BowSpammer.Mode.BOW;
            } else {
               if (!(stack.getItem() instanceof CrossbowItem)) {
                  this.setInfoText("No Weapon");
                  return;
               }

               this.weaponType = BowSpammer.Mode.CROSSBOW;
               if (CrossbowItem.isCharged(stack)) {
                  if (Module.mc.player.getInventory().selectedSlot != n) {
                     this.previousSlot = Module.mc.player.getInventory().selectedSlot;
                     Module.mc.player.getInventory().selectedSlot = n;
                  }

                  this.performActionInternal();
                  return;
               }
            }

            if (Module.mc.player.getInventory().selectedSlot == n) {
               this.performActionWithOffset();
            } else {
               this.previousSlot = Module.mc.player.getInventory().selectedSlot;
               Module.mc.player.getInventory().selectedSlot = n;
               this.state = BowSpammer.Mode2.SWAPPING;
               this.setInfoText("Swapping");
            }
         }
      }
   }

   private void performActionCustom() {
      if (!this.isValidItem(Module.mc.player.getMainHandStack())) {
         this.performActionImpl();
      } else {
         this.target = this.getLivingEntity();
         if (this.target == null) {
            this.performActionImpl();
         } else {
            this.performActionWithOffset();
         }
      }
   }

   private void performActionWithOffset() {
      this.state = BowSpammer.Mode2.DRAWING;
      this.aimTicks = 0;
      Module.mc.options.useKey.setPressed(true);
      this.setInfoText("Drawing");
   }

   private void performActionByIndex() {
      ItemStack mainHandStack = Module.mc.player.getMainHandStack();
      if (!this.isValidItem(mainHandStack)) {
         this.performActionImpl();
      } else if (!Module.mc.player.isUsingItem() && this.aimTicks > 1) {
         if (this.weaponType == BowSpammer.Mode.CROSSBOW && CrossbowItem.isCharged(mainHandStack)) {
            Module.mc.options.useKey.setPressed(false);
            this.performActionInternal();
         } else {
            this.performActionImpl();
         }
      } else {
         this.target = this.getLivingEntity();
         if (this.target == null) {
            Module.mc.options.useKey.setPressed(false);
            this.performActionImpl();
         } else {
            this.aimTicks++;
            if (this.weaponType == BowSpammer.Mode.BOW) {
               if (Module.mc.player.getItemUseTime() >= 20) {
                  this.performActionInternal();
               } else {
                  this.setInfoText("Bow " + Module.mc.player.getItemUseTime() + "/20");
               }
            } else if (CrossbowItem.isCharged(mainHandStack)) {
               Module.mc.options.useKey.setPressed(false);
               this.performActionInternal();
            } else {
               this.setInfoText("Loading...");
            }
         }
      }
   }

   private void performActionInternal() {
      this.state = BowSpammer.Mode2.AIMING;
      this.aimTicks = 0;
      this.setInfoText("Aiming");
   }

   private void performActionHelper() {
      ItemStack mainHandStack = Module.mc.player.getMainHandStack();
      if (this.weaponType == BowSpammer.Mode.BOW) {
         if (!(mainHandStack.getItem() instanceof BowItem) || !Module.mc.player.isUsingItem()) {
            this.performActionImpl();
            return;
         }
      } else if (!(mainHandStack.getItem() instanceof CrossbowItem) || !CrossbowItem.isCharged(mainHandStack)) {
         this.performActionImpl();
         return;
      }

      this.target = this.getLivingEntity();
      if (this.target == null) {
         if (this.weaponType == BowSpammer.Mode.BOW) {
            Module.mc.options.useKey.setPressed(false);
         }

         this.performActionImpl();
      } else {
         this.aimTicks++;
         if (this.aimTicks >= 3) {
            this.state = BowSpammer.Mode2.FIRING;
            this.setInfoText("Firing");
         } else {
            this.setInfoText("Aim " + this.aimTicks + "/3");
         }
      }
   }

   private void performActionV2() {
      if (this.weaponType == BowSpammer.Mode.BOW) {
         Module.mc.interactionManager.stopUsingItem(Module.mc.player);
         Module.mc.options.useKey.setPressed(false);
         this.performActionDirect();
         this.aimTicks = 0;
         this.state = BowSpammer.Mode2.IDLE;
         this.setInfoText("Fired");
      }
   }

   private boolean isValidItem(ItemStack stack) {
      return stack != null && !stack.isEmpty() && (stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem);
   }

   private boolean isValidEx() {
      return Module.mc.player != null && ItemScoreUtils.getSlotIndexRecord(Items.ARROW, Items.TIPPED_ARROW, Items.SPECTRAL_ARROW).isValidEx();
   }

   public int computeInt() {
      if (Module.mc.player == null) {
         return -1;
      }

      int n = -1;
      double d = -1.0;
      int n2 = -1;
      double d2 = -1.0;
      int n3 = -1;
      double d3 = -1.0;

      for (int i = 0; i <= 8; i++) {
         ItemStack stack = Module.mc.player.getInventory().getStack(i);
         if (!stack.isEmpty()) {
            if (stack.getItem() instanceof BowItem) {
               double d4 = ItemScoreUtils.computeDouble(stack);
               if (d4 > d || n == -1) {
                  d = d4;
                  n = i;
               }
            } else if (stack.getItem() instanceof CrossbowItem) {
               double d5 = ItemScoreUtils.computeDoubleEx(stack);
               if (CrossbowItem.isCharged(stack)) {
                  if (d5 > d3 || n3 == -1) {
                     d3 = d5;
                     n3 = i;
                  }
               } else if (d5 > d2 || n2 == -1) {
                  d2 = d5;
                  n2 = i;
               }
            }
         }
      }

      return n3 != -1 ? n3 : (n != -1 ? n : n2);
   }

   public LivingEntity getLivingEntity() {
      if (Module.mc.world != null && Module.mc.player != null) {
         double d = ((Number)this.minRange.getValue()).doubleValue();
         double d2 = ((Number)this.maxRange.getValue()).doubleValue();
         return StreamSupport.<Entity>stream(Module.mc.world.getEntities().spliterator(), false)
            .filter(LivingEntity.class::isInstance)
            .map(LivingEntity.class::cast)
            .filter(EntityTargetUtils::isLivingEntity)
            .filter(living -> isValid(d, d2, living))
            .filter(Module.mc.player::canSee)
            .filter(this::isValidTarget)
            .sorted(this.getComparator())
            .findFirst()
            .orElse(null);
      } else {
         return null;
      }
   }

   private static boolean isValid(double d, double d2, LivingEntity living) {
      double dist = Module.mc.player.distanceTo(living);
      return dist >= d && dist <= d2;
   }

   private boolean isValidTarget(LivingEntity living) {
      float f = ((Number)this.fieldOfView.getValue()).floatValue();
      return RotationManager.computeFloat(living) <= f;
   }

   private Comparator<LivingEntity> getComparator() {
      return this.priority.options[this.priority.getValue()].equalsIgnoreCase("Health")
         ? Comparator.comparingDouble(LivingEntity::getHealth)
         : Comparator.comparingDouble(living -> living.distanceTo(Module.mc.player));
   }

   public Rotation computeRotation(LivingEntity living) {
      Vec3d vec = new Vec3d(living.getLerpTargetX(), living.getLerpTargetY(), living.getLerpTargetZ());
      double dist = Module.mc.player.distanceTo(living);
      double x = vec.x;
      double z = vec.z;
      if ((Boolean)this.predictMovement.getValue()) {
         Vec3d vec2 = new Vec3d(living.getX() - living.lastRenderX, living.getY() - living.lastRenderY, living.getZ() - living.lastRenderZ);
         x += vec2.x * dist * 0.8;
         z += vec2.z * dist * 0.8;
      }

      double d = x - Module.mc.player.getX();
      double d2 = z - Module.mc.player.getZ();
      double d3 = vec.y + 1.0 - (Module.mc.player.getY() + Module.mc.player.getEyeHeight(Module.mc.player.getPose()));
      double dist2 = Math.sqrt(d * d + d2 * d2);
      float f = (float)(Math.atan2(d2, d) * 180.0 / Math.PI) - 90.0F;
      float f2 = (float)dist2;
      float f3 = (float)d3;
      float f4 = 1.0F;
      float f5 = 0.006F;
      float f6 = f4 * f4 * f4 * f4 - f5 * (f5 * f2 * f2 + 2.0F * f3 * f4 * f4);
      float f7 = -((float)Math.toDegrees(Math.atan((f4 * f4 - Math.sqrt(f6)) / (f5 * f2))));
      if (Float.isNaN(f7)) {
         f7 = (float)(-Math.atan2(d3, dist2) * 180.0 / Math.PI);
      }

      return new Rotation(f, f7);
   }

   private enum Mode {
      NONE,
      BOW,
      CROSSBOW;


      // $VF: synthetic method
      private static BowSpammer.Mode[] createValues() {
         return new BowSpammer.Mode[]{NONE, BOW, CROSSBOW};
      }


   }

   private enum Mode2 {
      IDLE,
      SWAPPING,
      DRAWING,
      AIMING,
      FIRING;


      // $VF: synthetic method
      private static BowSpammer.Mode2[] getMode2Array() {
         return new BowSpammer.Mode2[]{IDLE, SWAPPING, DRAWING, AIMING, FIRING};
      }


   }
}
