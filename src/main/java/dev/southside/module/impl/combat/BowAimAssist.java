package dev.southside.module.impl.combat;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.CameraEvent;
import dev.southside.event.events.DisableRotationEvent;
import dev.southside.event.events.MinecraftClientEvent;
import dev.southside.event.events.StateEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.movement.StrafeFix;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.ValuePair;
import dev.southside.utils.HealthColorUtils;
import dev.southside.utils.EntityTargetUtils;
import dev.southside.utils.BoxMeshRenderer;
import dev.southside.utils.NumberValue;
import dev.southside.utils.ModeValue;
import dev.southside.utils.RotationManager;
import dev.southside.utils.Rotation;
import java.awt.Color;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class BowAimAssist extends Module {
   private static final int DEFAULT_INT = 3;
   private static final double DEFAULT_DOUBLE = 0.99;
   @SerializedName("fov")
   public final NumberValue fov = new NumberValue("Fov", 180, 10, 360);
   @SerializedName("range")
   public final NumberValue range = new NumberValue("Range", 100, 1, 200);
   @SerializedName("priority")
   public final ModeValue priority = new ModeValue("Priority", "Range", "Angle", "Range");
   @SerializedName("autoRelease")
   public final BooleanValue autoRelease = new BooleanValue("AutoRelease", true);
   @SerializedName("rotationShot")
   public final BooleanValue rotationShot = new BooleanValue("RotationShot", true);
   @SerializedName("prediction")
   public final BooleanValue prediction = new BooleanValue("Prediction", true);
   @SerializedName("target")
   private LivingEntity target;
   @SerializedName("predictedPosition")
   private Vec3d predictedPosition;
   @SerializedName("shotAlignmentTicks")
   private int shotAlignmentTicks;
   @SerializedName("shotInProgress")
   private boolean shotInProgress;

   public BowAimAssist() {
      super(Category.Combat, "BowAimAssist");
   }

   @Override
   public void postInit() {
      super.postInit();
      this.hideIf(this.autoRelease.getLabel(), () -> false);
      this.hideIf(this.rotationShot.getLabel(), () -> false);
      this.hideIf(this.prediction.getLabel(), () -> false);
   }

   @EventTarget
   public void onDisableRotation(DisableRotationEvent disableRotationEvent) {
      if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().world != null) {
         ItemStack mainHandStack = Module.mc.player.getMainHandStack();
         boolean flag = mainHandStack != null && mainHandStack.getItem() == Items.BOW && Module.mc.player.isUsingItem();
         boolean flag2 = mainHandStack != null && mainHandStack.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(mainHandStack);
         if (!flag && !flag2) {
            this.target = null;
            this.performActionAlt();
         } else {
            this.target = this.getLivingEntity();
            if (this.target == null) {
               this.performActionAlt();
            } else if (flag) {
               Rotation rotation = this.computeRotation(this.target);
               this.predictedPosition = null;
               this.performAction(rotation);
               if ((Boolean)this.autoRelease.getValue() && Module.mc.player.getItemUseTime() >= 20) {
                  Module.mc.interactionManager.stopUsingItem(Module.mc.player);
               }
            } else {
               Rotation rotation2;
               if ((Boolean)this.prediction.getValue()) {
                  this.predictedPosition = this.computePositionEx(this.target);
                  Vec3d eyePos = Module.mc.player.getEyePos();
                  double dx = this.predictedPosition.x - eyePos.x;
                  double dy = this.predictedPosition.y - eyePos.y;
                  double dz = this.predictedPosition.z - eyePos.z;
                  double dist = Math.sqrt(dx * dx + dz * dz);
                  float f = (float)(Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
                  float f2 = (float)(-(Math.atan2(dy, dist) * 180.0 / Math.PI));
                  rotation2 = new Rotation(f, f2);
               } else {
                  this.predictedPosition = null;
                  rotation2 = this.computeRotation(this.target);
               }

               if ((Boolean)this.rotationShot.getValue()) {
                  if (this.shotInProgress) {
                     this.shotAlignmentTicks++;
                     this.performAction(rotation2);
                     Module.mc.options.useKey.setPressed(false);
                  }
               } else {
                  this.performAction(rotation2);
               }
            }
         }
      }
   }

   private void performActionAlt() {
      this.predictedPosition = null;
      this.shotAlignmentTicks = 0;
      this.shotInProgress = false;
   }

   private void performAction(Rotation rotation) {
      RotationManager.rotationRequests.add(new ValuePair<>(rotation, 0));
      RotationManager.moveFixMode = Southside.INSTANCE.getModuleManager().getModule(StrafeFix.class).getMoveFixMode();
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      if (stateEvent.getState() == Stage.PRE && Module.mc.player != null) {
         ItemStack mainHandStack = Module.mc.player.getMainHandStack();
         boolean flag = mainHandStack != null && mainHandStack.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(mainHandStack);
         if ((Boolean)this.rotationShot.getValue() && flag && Module.mc.options.useKey.isPressed() && this.target != null && !this.shotInProgress) {
            this.shotInProgress = true;
            this.shotAlignmentTicks = 0;
            Module.mc.options.useKey.setPressed(false);
         }
      }
   }

   @EventTarget(priority = -200)
   public void onMinecraftClient(MinecraftClientEvent minecraftClientEvent) {
      if (Module.mc.player != null) {
         ItemStack mainHandStack = Module.mc.player.getMainHandStack();
         if (mainHandStack != null && this.shotInProgress && mainHandStack.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(mainHandStack) && this.shotAlignmentTicks >= 3) {
            this.shotInProgress = false;
            this.shotAlignmentTicks = 0;
            Module.mc.interactionManager.interactItem(Module.mc.player, Hand.MAIN_HAND);
         }
      }
   }

   public LivingEntity getLivingEntity() {
      Stream<LivingEntity> stream = StreamSupport.<Entity>stream(Module.mc.world.getEntities().spliterator(), false)
         .filter(LivingEntity.class::isInstance)
         .map(LivingEntity.class::cast)
         .filter(EntityTargetUtils::isLivingEntity)
         .filter(this::isValidTarget)
         .filter(Module.mc.player::canSee)
         .filter(this::isValidTargetEx);
      if (this.priority.options[this.priority.getValue()].equalsIgnoreCase("Range")) {
         stream = stream.sorted(Comparator.comparingDouble(living -> living.distanceTo(Module.mc.player)));
      } else if (this.priority.options[this.priority.getValue()].equalsIgnoreCase("Angle")) {
         stream = stream.sorted(Comparator.comparingDouble(RotationManager::computeFloat));
      }

      List list = stream.toList();
      return list.isEmpty() ? null : (LivingEntity)list.get(0);
   }

   private boolean isValidTarget(LivingEntity living) {
      return Module.mc.player.distanceTo(living) <= ((Number)this.range.getValue()).doubleValue();
   }

   private boolean isValidTargetEx(LivingEntity living) {
      float f = ((Number)this.fov.getValue()).floatValue();
      return RotationManager.computeFloat(living) <= f;
   }

   private Vec3d computePosition(LivingEntity living) {
      return new Vec3d(living.getLerpTargetX(), living.getLerpTargetY(), living.getLerpTargetZ());
   }

   public Vec3d computePositionEx(LivingEntity living) {
      Vec3d vec = this.computePosition(living);
      Vec3d vec2 = vec.add(0.0, living.getHeight() / 2.0, 0.0);
      double dist = Module.mc.player.getEyePos().distanceTo(vec2);
      int n = this.estimateFlightTicks(dist, 3.15);
      Vec3d vec3 = new Vec3d(living.getX() - living.prevX, living.getY() - living.prevY, living.getZ() - living.prevZ);
      double d = vec.x + vec3.x * n;
      double d2 = vec.y + vec3.y * n;
      double d3 = vec.z + vec3.z * n;
      return new Vec3d(d, d2 + living.getHeight() / 2.0, d3);
   }

   public int estimateFlightTicks(double x, double y) {
      double d = 0.0;
      double d2 = y;

      int n;
      for (n = 0; d < x && n < 100; n++) {
         d += d2;
         d2 *= 0.99;
      }

      return n;
   }

   public Rotation computeRotation(LivingEntity living) {
      Vec3d vec = this.computePosition(living);
      double dist = Module.mc.player.distanceTo(living);
      Vec3d vec2 = new Vec3d(living.getX() - living.lastRenderX, living.getY() - living.lastRenderY, living.getZ() - living.lastRenderZ);
      double d = vec.x + vec2.x * dist * 0.8;
      double d2 = vec.z + vec2.z * dist * 0.8;
      double d3 = d - Module.mc.player.getX();
      double d4 = d2 - Module.mc.player.getZ();
      double d5 = vec.y + 1.0 - (Module.mc.player.getY() + Module.mc.player.getEyeHeight(Module.mc.player.getPose()));
      double dist2 = Math.sqrt(d3 * d3 + d4 * d4);
      float f = (float)(Math.atan2(d4, d3) * 180.0 / Math.PI) - 90.0F;
      float f2 = (float)dist2;
      float f3 = (float)d5;
      float f4 = 1.0F;
      float f5 = 0.006F;
      float f6 = f4 * f4 * f4 * f4 - f5 * (f5 * f2 * f2 + 2.0F * f3 * f4 * f4);
      float f7 = -((float)Math.toDegrees(Math.atan((f4 * f4 - Math.sqrt(f6)) / (f5 * f2))));
      return new Rotation(f, f7);
   }

   @EventTarget
   public void onCamera(CameraEvent cameraEvent) {
      if (this.target != null) {
         Color color = HealthColorUtils.blendColor(this.target.getHealth(), 20.0F);
         float tickDelta = cameraEvent.tickDelta;
         if ((Boolean)this.prediction.getValue() && this.predictedPosition != null) {
            double d = this.target.getWidth() / 2.0;
            double height = this.target.getHeight();
            Box box = new Box(
               this.predictedPosition.x - d,
               this.predictedPosition.y,
               this.predictedPosition.z - d,
               this.predictedPosition.x + d,
               this.predictedPosition.y + height,
               this.predictedPosition.z + d
            );
            BoxMeshRenderer.renderMesh.renderBox(box, new Color(100, 200, 255, 70));
            BoxMeshRenderer.renderMeshValue.renderBoxEx(box, new Color(100, 200, 255, 150));
         }

         double d2 = this.target.getX() - this.target.prevX;
         double d3 = this.target.getY() - this.target.prevY;
         double d4 = this.target.getZ() - this.target.prevZ;
         Box box2 = this.target.getBoundingBox().expand(0.2).offset(-d2 + d2 * tickDelta, -d3 + d3 * tickDelta, -d4 + d4 * tickDelta);
         BoxMeshRenderer.renderMesh.renderBox(box2, new Color(color.getRed(), color.getGreen(), color.getBlue(), 100));
         BoxMeshRenderer.renderMeshValue.renderBoxEx(box2, new Color(color.getRed(), color.getGreen(), color.getBlue(), 150));
      }
   }
}
