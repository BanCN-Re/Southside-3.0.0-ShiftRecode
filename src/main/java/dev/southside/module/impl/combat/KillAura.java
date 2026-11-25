package dev.southside.module.impl.combat;

import cc.polyfrost.oneconfig.config.core.OneColor;
import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.events.CameraEvent;
import dev.southside.event.events.ClientPlayerEntityEvent;
import dev.southside.event.events.DisableRotationEvent;
import dev.southside.event.events.MinecraftClientEvent;
import dev.southside.event.events.StateEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.movement.NoSlow;
import dev.southside.module.impl.player.AutoTool;
import dev.southside.module.impl.render.OldHitting;
import dev.southside.module.impl.world.Scaffold;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.AttackTracker;
import dev.southside.utils.ColorValue;
import dev.southside.utils.ValuePair;
import dev.southside.utils.BoxRaytraceUtils;
import dev.southside.utils.WorldBoxRenderer;
import dev.southside.utils.AimPointUtils;
import dev.southside.utils.HealthColorUtils;
import dev.southside.utils.EntityTargetUtils;
import dev.southside.utils.PacketUtils;
import dev.southside.utils.BoxMeshRenderer;
import dev.southside.utils.MillisTimer;
import dev.southside.utils.NumberValue;
import dev.southside.utils.ModeValue;
import dev.southside.utils.RotationManager;
import dev.southside.utils.NanoTimer;
import dev.southside.utils.MovementUtils;
import dev.southside.utils.BlockLookUtils;
import dev.southside.utils.Rotation;
import dev.southside.utils.math.RandomUtils;
import dev.southside.utils.render.MarkDrawer;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;

public class KillAura extends Module {
   private static final int DEFAULT_INT = 20;
   private static KillAura INSTANCE;
   @SerializedName("range")
   public NumberValue range = new NumberValue("Range", 3.0, 3.0, 8.0, 0.01);
   @SerializedName("searchRangeOffset")
   public NumberValue searchRangeOffset = new NumberValue("Discover Offset", 1.0, 1.0, 3.0, 0.01);
   @SerializedName("minCps")
   public NumberValue minCps = new NumberValue("Min CPS", 8, 0, 20, 1);
   @SerializedName("maxCps")
   public NumberValue maxCps = new NumberValue("Max CPS", 10, 0, 20, 1);
   @SerializedName("targetMode")
   public ModeValue targetMode = new ModeValue("Target Mode", "Switch", "Switch", "Single", "Multi");
   @SerializedName("switchDelay")
   public NumberValue switchDelay = new NumberValue("Switch delay", 500.0, 0.0, 1000.0, 10.0);
   @SerializedName("rotationMode")
   public ModeValue rotationMode = new ModeValue("Rotation Mode", "Normal", "Normal", "Legit", "Better", "Smart", "None");
   @SerializedName("targetPriority")
   public ModeValue targetPriority = new ModeValue("Sorted by", "Closest Distance", "Closest Distance", "Lowest Armor", "Lowest Health");
   @SerializedName("quantizeRotation")
   public BooleanValue quantizeRotation = new BooleanValue("Fix Rotation", true);
   @SerializedName("useAttackCooldown")
   public BooleanValue useAttackCooldown = new BooleanValue("Attack Cooldown", false);
   @SerializedName("waitForRod")
   public BooleanValue waitForRod = new BooleanValue("Check Auto FishRod", true);
   @SerializedName("raycast")
   public BooleanValue raycast = new BooleanValue("RayCast", true);
   @SerializedName("requireRaycastForRotation")
   public BooleanValue requireRaycastForRotation = new BooleanValue("Rotation Only RayCast", true);
   @SerializedName("rotationNoise")
   public BooleanValue rotationNoise = new BooleanValue("Noise", false);
   public static final BooleanValue keepSprint = new BooleanValue("Keep Sprint", false);
   @SerializedName("autoDisable")
   public final BooleanValue autoDisable = new BooleanValue("Auto Disable", false);
   @SerializedName("targetMarker")
   public ModeValue targetMarker = new ModeValue("Mark Mode", "NurikZapen", "NurikZapen", "ThunderHack2", "Box", "None");
   @SerializedName("blockingAnimation")
   public BooleanValue blockingAnimation = new BooleanValue("Blocking Animation", true);
   @SerializedName("healthColor")
   public final BooleanValue healthColor = new BooleanValue("Health Color", false);
   @SerializedName("sideColor")
   public final ColorValue sideColor = new ColorValue("Side Color", true, new OneColor(255, 10, 10, 70));
   @SerializedName("lineColor")
   public final ColorValue lineColor = new ColorValue("Line Color", true, new OneColor(255, 10, 10, 150));
   public static List<Entity> targets = new ArrayList<>();
   private static final Map<Entity, Rotation> attackableTargets = new HashMap<>();
   @SerializedName("switchTimer")
   private final NanoTimer switchTimer = new NanoTimer();
   public static final MillisTimer lastTimeUtil = new MillisTimer();
   @SerializedName("targetRotation")
   private Rotation targetRotation = new Rotation(0.0F, 0.0F);
   @SerializedName("attackReady")
   private boolean attackReady;
   @SerializedName("hasTarget")
   private boolean hasTarget;
   @SerializedName("canKeepSprinting")
   private boolean canKeepSprinting = true;
   public static boolean activeValue;
   @SerializedName("currentCps")
   private double currentCps;
   public static Entity target;
   @SerializedName("targetIndex")
   private int targetIndex;

   public KillAura() {
      super(Category.Combat, "KillAura", "Automatically attacks entities", 82);
      INSTANCE = this;
   }

   @Override
   public int getHeightAlt() {
      return 1;
   }

   @Override
   public void postInit() {
      super.postInit();
      this.hideIf(this.switchDelay.getLabel(), () -> !this.targetMode.getDisplayTextEx().equals("Switch"));
      this.hideIf(this.blockingAnimation.getLabel(), () -> !Southside.INSTANCE.getModuleManager().getModule(OldHitting.class).isEnabled());
      this.hideIf(this.healthColor.getLabel(), this::isValidEx);
      this.hideIf(this.sideColor.getLabel(), this::isValidEx);
      this.hideIf(this.lineColor.getLabel(), this::isValidEx);
      this.hideIf(this.rotationNoise.getLabel(), () -> !this.rotationMode.getDisplayTextEx().equalsIgnoreCase("Normal"));
      this.addRangedValueRestrict(this.minCps, this.maxCps);
   }

   private boolean isValidEx() {
      return !this.targetMarker.getDisplayTextEx().equalsIgnoreCase("Box");
   }

   private boolean isValidAlt() {
      return this.targetMode.getDisplayTextEx().equalsIgnoreCase("Multi");
   }

   private boolean isValidImpl() {
      return Module.mc.player != null
         && (Module.mc.player.getMainHandStack().isOf(Items.END_CRYSTAL) || Module.mc.player.getOffHandStack().isOf(Items.END_CRYSTAL));
   }

   @Override
   public void onEnable() {
      this.hasTarget = false;
      this.attackReady = false;
      targets.clear();
      this.switchTimer.reset();
      target = null;
      this.canKeepSprinting = true;
      attackableTargets.clear();
      this.targetIndex = 0;
      this.currentCps = this.getCurrentValue();
      super.onEnable();
   }

   @Override
   public void onDisable() {
      this.hasTarget = false;
      target = null;
      this.canKeepSprinting = true;
      attackableTargets.clear();
      targets.clear();
      this.targetIndex = 0;
   }

   @EventTarget(priority = -200)
   public void onMinecraftClient(MinecraftClientEvent minecraftClientEvent) {
      if (MinecraftClient.getInstance() != null
         && MinecraftClient.getInstance().player != null
         && MinecraftClient.getInstance().world != null
         && !this.isValidImpl()
         && target != null
         && target.isAlive()
         && Module.mc.player.canSee(target)) {
         if (!(Boolean)this.raycast.getValue() || this.isValidTarget(target) || this.isValidAlt()) {
            if (!Module.mc.player.isUsingItem() && !Southside.INSTANCE.getModuleManager().getModule(KBController.class).isValidEx()) {
               if (this.attackReady) {
                  this.performActionAlt();
                  this.attackReady = false;
               }
            }
         }
      }
   }

   private void performActionAlt() {
      if (Module.mc.player != null
         && !Module.mc.player.isUsingItem()
         && !this.isValidImpl()
         && Module.mc.player.getMainHandStack().getItem() != Items.ENDER_PEARL) {
         if (!(Boolean)this.waitForRod.getValue() || !Southside.INSTANCE.getModuleManager().getModule(AutoRod.class).rodThrown) {
            if ((!(Boolean)this.useAttackCooldown.getValue() || !(Module.mc.player.getAttackCooldownProgress(0.0F) < 1.0F))
               && target != null
               && target.isAlive()) {
               AutoTool autoTool = Southside.INSTANCE.getModuleManager().getModule(AutoTool.class);
               if (autoTool.isEnabled()) {
                  autoTool.performActionAlt();
               }

               if (lastTimeUtil.hasElapsed((long)(1000.0 / this.currentCps))) {
                  this.currentCps = this.getCurrentValue();
                  if (this.isValidAlt() && !attackableTargets.isEmpty()) {
                     attackableTargets.forEach(this::updateEntity);
                     lastTimeUtil.reset();
                     return;
                  }

                  if ((Boolean)keepSprint.getValue()
                     && !this.canKeepSprinting
                     && Module.mc.player.isSprinting()
                     && Velocity.count <= 0
                     && !Velocity.activeValue
                     && (!Module.mc.player.isOnGround() || !Module.mc.options.jumpKey.isPressed())) {
                     this.performActionImpl();
                     return;
                  }

                  this.updateEntity(target);
                  lastTimeUtil.reset();
                  activeValue = Module.mc.player.lastSprinting;
               }

               if (Criticals.mode.getDisplayTextEx().equalsIgnoreCase("Packet")
                  && Velocity.count <= 0
                  && Module.mc.player.getVelocity().y < 0.0
                  && Southside.INSTANCE.getModuleManager().getModule(Criticals.class).isEnabled()
                  && !Module.mc.player.isOnGround()) {
                  this.performActionImpl();
               }
            }
         }
      }
   }

   private double getCurrentValue() {
      double d = ((Number)this.minCps.getValue()).intValue();
      double d2 = ((Number)this.maxCps.getValue()).intValue();
      return d < d2 ? d + (d2 - d) * RandomUtils.RAND.nextDouble() : d;
   }

   private void performActionImpl() {
      MovementUtils.mc.player.setSprinting(false);
      MovementUtils.mc.options.sprintKey.setPressed(false);
   }

   private void updateEntity(Entity entity) {
      if (AttackTracker.INSTANCE.getHeight() < 20) {
         Module.mc.interactionManager.attackEntity(Module.mc.player, entity);
         Module.mc.player.swingHand(Hand.MAIN_HAND);
         AttackTracker.INSTANCE.performAction();
      }

      this.canKeepSprinting = false;
   }

   private void updateEntity(Entity entity, Rotation rotation) {
      PacketUtils.interactItemWithRotation(rotation);
      this.updateEntity(entity);
   }

   @EventTarget(priority = -100)
   public void onDisableRotation(DisableRotationEvent disableRotationEvent) {
      MarkDrawer.performActionEx();
      this.hasTarget = target != null;
      if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().world != null) {
         if (this.isValidImpl()) {
            this.clear();
         } else {
            this.performActionSafe();
            if (!targets.isEmpty()) {
               this.performActionCustom();
               if (target != null && target.isAlive() && Module.mc.player.canSee(target)) {
                  this.performActionDirect();
                  this.attackReady = true;
               }
            }
         }
      }
   }

   private void performActionDirect() {
      if (this.isValidAlt()) {
         if (!attackableTargets.isEmpty()) {
            this.targetRotation = attackableTargets.get(target);
            if (this.targetRotation != null) {
               RotationManager.rotationRequests.add(new ValuePair<>(this.targetRotation, this.getHeightAlt()));
            }
         }
      } else {
         Rotation rotation = switch (this.rotationMode.getDisplayTextEx()) {
            case "Normal" -> RotationManager.computeRotation(
               Module.mc.player.getEyePos(), BoxRaytraceUtils.computePosition(Module.mc.player.getEyePos(), target.getBoundingBox()), (Boolean)this.rotationNoise.getValue()
            );
            case "Legit" -> RotationManager.raycast(target, ((Number)this.range.getValue()).doubleValue(), 0.0, 1.0F);
            case "Better" -> RotationManager.raycast(RotationManager.mc.player, target, ((Number)this.range.getValue()).doubleValue());
            case "Smart" -> AimPointUtils.computeRotation(AimPointUtils.getInner(Module.mc.player, target, Module.mc.getRenderTickCounter().getTickDelta(false)));
            default -> null;
         };
         if ((Boolean)this.quantizeRotation.getValue() && rotation != null) {
            float f = ((Double)Module.mc.options.getMouseSensitivity().getValue()).floatValue();
            float f2 = f * 0.6F + 0.2F;
            float f3 = f2 * f2 * f2 * 1.2F;
            rotation.yaw = rotation.yaw - rotation.yaw % f3;
            rotation.pitch = rotation.pitch - rotation.pitch % f3;
         }

         this.targetRotation = rotation;
         if (rotation != null) {
            if (!(Boolean)this.requireRaycastForRotation.getValue()
               || BlockLookUtils.isHovered(rotation.yaw, rotation.pitch, ((Number)this.range.getValue()).floatValue(), target.getBoundingBox())) {
               RotationManager.rotationRequests.add(new ValuePair<>(rotation, this.getHeightAlt()));
            }
         }
      }
   }

   private void performActionSafe() {
      targets.clear();
      attackableTargets.clear();
      if (!Southside.INSTANCE.getModuleManager().getModule(Scaffold.class).isEnabled() && !NoSlow.activeValue) {
         for (Entity entity : Module.mc.world.getEntities()) {
            if (EntityTargetUtils.isLivingEntity(entity)
               && this.computeDistance(entity) <= ((Number)this.range.getValue()).doubleValue() + ((Number)this.searchRangeOffset.getValue()).doubleValue()) {
               targets.add(entity);
               if (this.isValidAlt()) {
                  Rotation rotation = AimPointUtils.computeRotation(AimPointUtils.getInner(Module.mc.player, entity, Module.mc.getRenderTickCounter().getTickDelta(false)));
                  if (BlockLookUtils.isHovered(rotation.yaw, rotation.pitch, ((Number)this.range.getValue()).doubleValue(), entity.getBoundingBox())) {
                     attackableTargets.put(entity, rotation);
                  }
               }
            }
         }
      }

      if (targets.isEmpty()) {
         target = null;
         this.canKeepSprinting = true;
         attackableTargets.clear();
      } else {
         targets.sort(Comparator.comparingDouble(this::computeDistanceEx));
      }
   }

   private double computeDistance(Entity entity) {
      return BoxRaytraceUtils.computePosition(Module.mc.player.getEyePos(), entity.getBoundingBox()).distanceTo(Module.mc.player.getEyePos());
   }

   private double computeDistanceEx(Entity entity) {
      if (Module.mc.player != null && entity instanceof LivingEntity living) {
         return switch (this.targetPriority.getDisplayTextEx()) {
            case "Lowest Health" -> living.getHealth();
            case "Lowest Armor" -> living.getArmor();
            default -> Module.mc.player.distanceTo(entity);
         };
      } else {
         return Double.MAX_VALUE;
      }
   }

   private void performActionCustom() {
      if (!targets.isEmpty()) {
         if (this.targetIndex >= targets.size()) {
            this.targetIndex = 0;
         }

         target = this.computeDistance(targets.get(this.targetIndex)) <= ((Number)this.range.getValue()).doubleValue()
            ? targets.get(this.targetIndex)
            : targets.getFirst();
         if (targets.size() > 1
            && this.targetMode.getDisplayTextEx().equals("Switch")
            && this.switchTimer.hasTimePassedDouble(((Number)this.switchDelay.getValue()).doubleValue())) {
            this.targetIndex++;
            this.switchTimer.reset();
         }

         if (targets.size() > 1
            && this.targetMode.getDisplayTextEx().equals("Single")
            && (
               this.computeDistance(target) > ((Number)this.range.getValue()).doubleValue() + ((Number)this.searchRangeOffset.getValue()).doubleValue()
                  || !target.isAlive()
            )) {
            this.targetIndex++;
         }
      }
   }

   public boolean isValidTarget(Entity entity) {
      if (entity == null) {
         return false;
      }

      Rotation activeRotation = RotationManager.getActiveRotation();
      return BlockLookUtils.isHovered(activeRotation.yaw, activeRotation.pitch, this.range.getCurrentValue(), entity.getBoundingBox()) && Module.mc.currentScreen == null;
   }

   @EventTarget
   public void onClientPlayerEntity(ClientPlayerEntityEvent clientPlayerEntityEvent) {
      this.setInfoText(this.targetMode.getDisplayTextEx());
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      if (Module.mc.player != null && Module.mc.world != null) {
         if (this.isValidImpl()) {
            this.clear();
         }

         if ((Boolean)this.autoDisable.getValue() && Module.mc.world != null) {
            if (!Module.mc.player.isAlive()) {
               this.setEnabled(false);
               return;
            }

            if (Module.mc.player.age <= 1) {
               this.setEnabled(false);
            }
         }
      }
   }

   private void clear() {
      target = null;
      this.canKeepSprinting = true;
      attackableTargets.clear();
      this.attackReady = false;
   }

   public static boolean isBlocking() {
      return INSTANCE.isEnabled() && INSTANCE.hasTarget && (Boolean)INSTANCE.blockingAnimation.getValue();
   }

   @EventTarget
   public void onCamera(CameraEvent cameraEvent) {
      Entity entity = target;
      if (entity instanceof LivingEntity living) {
         Color color = HealthColorUtils.blendColor(living.getHealth(), 20.0F);
         double d = entity.getX() - entity.prevX;
         double d2 = entity.getY() - entity.prevY;
         double d3 = entity.getZ() - entity.prevZ;
         float tickDelta = cameraEvent.tickDelta;
         switch (this.targetMarker.getDisplayTextEx()) {
            case "NurikZapen":
               WorldBoxRenderer.performAction();
               MarkDrawer.updateEntity(entity);
               WorldBoxRenderer.render(cameraEvent.getMatrixStack());
               break;
            case "ThunderHack2":
               WorldBoxRenderer.performAction();
               MarkDrawer.renderTH(14, 8, 1.8F, 3.0F, entity);
               WorldBoxRenderer.render(cameraEvent.getMatrixStack());
               break;
            case "Box":
               Box box = entity.getBoundingBox().expand(0.1, 0.2, 0.1).offset(-d + d * tickDelta, -d2 + d2 * tickDelta, -d3 + d3 * tickDelta);
               Color color2 = this.healthColor.getValue()
                  ? new Color(color.getRed(), color.getGreen(), color.getBlue(), 50)
                  : ((OneColor)this.sideColor.getValue()).blendColor();
               Color color3 = this.healthColor.getValue()
                  ? new Color(color.getRed(), color.getGreen(), color.getBlue(), 90)
                  : ((OneColor)this.lineColor.getValue()).blendColor();
               BoxMeshRenderer.renderMesh.renderBox(box, color2);
               BoxMeshRenderer.renderMeshValue.renderBoxEx(box, color3);
         }
      }
   }
}
