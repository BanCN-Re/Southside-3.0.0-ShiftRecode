package dev.southside.module.impl.combat;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.TransferOrigin;
import dev.southside.event.events.ClientWorldEvent;
import dev.southside.event.events.RightClickEvent;
import dev.southside.event.events.DirectionEvent;
import dev.southside.event.events.DisableRotationEvent;
import dev.southside.event.events.DrawContextEvent;
import dev.southside.event.events.ForwardEvent;
import dev.southside.event.events.MinecraftClientEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.misc.AutoGApple;
import dev.southside.module.impl.movement.NoSlow;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.AttackTracker;
import dev.southside.utils.ValuePair;
import dev.southside.utils.BoxRaytraceUtils;
import dev.southside.utils.OneConfigFonts;
import dev.southside.utils.EntityTargetUtils;
import dev.southside.utils.PacketUtils;
import dev.southside.utils.NanoVGHelper;
import dev.southside.utils.NumberValue;
import dev.southside.utils.ServerPacketManager;
import dev.southside.utils.RotationManager;
import dev.southside.utils.MovementFreezeManager;
import dev.southside.utils.MovementUtils;
import dev.southside.utils.BlockLookUtils;
import dev.southside.utils.Rotation;
import java.awt.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class Velocity extends Module {
   @SerializedName("debug")
   public final BooleanValue debug = new BooleanValue("Debug", false);
   @SerializedName("debugFails")
   public final BooleanValue debugFails = new BooleanValue("Debug Fails", false);
   @SerializedName("showDelayHud")
   public final BooleanValue showDelayHud = new BooleanValue("Delay HUD", false);
   @SerializedName("fireballCooldown")
   public final BooleanValue fireballCooldown = new BooleanValue("Fireball Cooldown", true);
   @SerializedName("modernCombat")
   public final BooleanValue modernCombat = new BooleanValue("1.9+", false);
   @SerializedName("partialReplay")
   public final BooleanValue partialReplay = new BooleanValue("Test Mode", true);
   @SerializedName("waitForGround")
   public final BooleanValue waitForGround = new BooleanValue("Delay Till Ground", false);
   @SerializedName("targetHorizontalSpeed")
   public final NumberValue targetHorizontalSpeed = new NumberValue("Target Motion", 0.15, 0.05, 0.5, 0.05);
   @SerializedName("multiTarget")
   public final BooleanValue multiTarget = new BooleanValue("Multi Target", true);
   @SerializedName("releaseDistance")
   public final NumberValue releaseDistance = new NumberValue("Release Distance", 1.6, 0.1, 3.0, 0.1);
   public static boolean activeValue;
   public static int count;
   @SerializedName("fireballCooldownTicks")
   private int fireballCooldownTicks;
   @SerializedName("reductionTarget")
   private Entity reductionTarget;
   @SerializedName("heldVelocityPacket")
   private EntityVelocityUpdateS2CPacket heldVelocityPacket;

   public Velocity() {
      super(Category.Combat, "Velocity", "Reduce KnockBack");
   }

   @Override
   public void onEnable() {
      this.performActionAlt();
   }

   @Override
   public void onDisable() {
      this.performActionAlt();
   }

   @Override
   public void resetToDefaults() {
      this.performActionAlt();
   }

   private void performActionAlt() {
      if (activeValue) {
         this.reductionTarget = null;
         if (!NoSlow.activeValue) {
            ServerPacketManager.setActive(false);
         }

         activeValue = false;
         count = 0;
         this.heldVelocityPacket = null;
      }
   }

   @EventTarget
   public void onClientWorld(ClientWorldEvent clientWorldEvent) {
      this.reductionTarget = null;
      this.fireballCooldownTicks = 0;
      activeValue = false;
      count = 0;
      this.heldVelocityPacket = null;
   }

   private boolean isValidEx() {
      return Module.mc.player != null
         && !Module.mc.player.isUsingItem()
         && Module.mc.player.isSprinting()
         && (Module.mc.player.isOnGround() || !(Boolean)this.waitForGround.getValue());
   }

   @EventTarget
   public void onMinecraftClient(MinecraftClientEvent minecraftClientEvent) {
      if (this.fireballCooldownTicks <= 0 && Module.mc.player != null && Module.mc.world != null) {
         if (activeValue && ServerPacketManager.active && this.isValidEx()) {
            Entity entity = this.getEntity();
            if (entity != null
               && BoxRaytraceUtils.computePosition(Module.mc.player.getEyePos(), KillAura.target.getBoundingBox()).distanceTo(Module.mc.player.getEyePos())
                  <= ((Number)this.releaseDistance.getValue()).doubleValue()) {
               this.setActiveEx(true);
            }
         }

         if (count > 0) {
            count--;
            if ((Boolean)this.modernCombat.getValue()) {
               this.performActionDirect();
            } else {
               this.performActionSafe();
            }
         }
      }
   }

   private Entity getEntity() {
      if (KillAura.target == null) {
         return null;
      }

      if (!Southside.INSTANCE.getModuleManager().getModule(KillAura.class).isValidTarget(KillAura.target)) {
         Vec3d vector = BlockLookUtils.vector;
         Vec3d vec = BoxRaytraceUtils.computePosition(BlockLookUtils.vector, KillAura.target.getBoundingBox());
         Vec3d delta = vec.subtract(vector);
         double horizDist = MathHelper.hypot(delta.x, delta.z);
         float f = (float)(MathHelper.atan2(delta.z, delta.x) * (180.0 / Math.PI)) - 90.0F;
         float f2 = (float)(-(MathHelper.atan2(delta.y, horizDist) * (180.0 / Math.PI)));
         Rotation rotation = new Rotation(f, f2);
         if (!BlockLookUtils.isHovered(rotation.yaw, rotation.pitch, 3.0, KillAura.target.getBoundingBox())) {
            return null;
         }

         RotationManager.currentRotation = new Rotation(rotation.yaw, rotation.pitch, rotation.targetPosition, rotation.sourcePosition);
      }

      return KillAura.target;
   }

   private void setActiveEx(boolean flag) {
      if ((Boolean)this.partialReplay.getValue()) {
         this.setActiveAlt(flag);
      } else {
         ServerPacketManager.setActive(false);
      }

      activeValue = false;
      this.heldVelocityPacket = null;
      if (Module.mc.player == null) {
         count = 0;
      } else {
         double len = new Vec3d(Module.mc.player.getVelocity().getX(), 0.0, Module.mc.player.getVelocity().getZ()).length();
         count = this.computeInt(len);
         this.applyText("Sync, ticks used: 0 calculated attacks: " + count + ".");
      }
   }

   private int computeInt(double d) {
      if (d < ((Number)this.targetHorizontalSpeed.getValue()).doubleValue()) {
         return 0;
      }

      for (int i = 1; i <= 8; i++) {
         d *= 0.6;
         if (d < ((Number)this.targetHorizontalSpeed.getValue()).doubleValue()) {
            return i;
         }
      }

      return 9;
   }

   private void setActiveAlt(boolean flag) {
      if (ServerPacketManager.mc.getNetworkHandler() == null) {
         ServerPacketManager.clear();
      } else {
         boolean flag2 = false;

         while (true) {
            Packet packet;
            synchronized (ServerPacketManager.linkedBlockingQueue) {
               Object obj = ServerPacketManager.linkedBlockingQueue.poll();
               if (!(obj instanceof Packet packet2)) {
                  if (obj instanceof Runnable action) {
                     action.run();
                     continue;
                  }

                  ServerPacketManager.count = 0;
                  break;
               }

               packet = packet2;
            }

            if (packet instanceof KeepAliveC2SPacket) {
               ServerPacketManager.count = Math.max(0, ServerPacketManager.count - 1);
               if (flag2) {
                  break;
               }
            } else {
               MovementFreezeManager.handlePacket(packet);

               try {
                  ServerPacketManager.handlePacket(packet);
               } catch (Exception e) {
                  Southside.LOGGER.error("Unexpected error in Velocity", e);
               }

               if (flag && packet.equals(this.heldVelocityPacket)) {
                  flag2 = true;
               }
            }
         }
      }
   }

   private void performActionImpl() {
      if (count == 0 && (Boolean)this.partialReplay.getValue() && !activeValue) {
         this.applyText("Hit complete.");
         ServerPacketManager.setActive(false);
      }
   }

   private void performActionDirect() {
      if (Module.mc.player.getAttackCooldownProgress(0.0F) < 1.0F) {
         count = 1;
      } else {
         this.performActionImpl();
         if (this.reductionTarget == null) {
            this.reductionTarget = this.getEntity();
         }

         if (this.reductionTarget == null) {
            this.performActionCustom();
         } else {
            PacketUtils.sendPacket(PlayerInteractEntityC2SPacket.attack(this.reductionTarget, false));
            PacketUtils.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            Module.mc.player.setVelocity(Module.mc.player.getVelocity().multiply(0.6, 1.0, 0.6));
            AttackTracker.INSTANCE.performAction();
            KillAura.lastTimeUtil.reset();
            this.applyText("1.9+ Reduced!");
            this.reductionTarget = null;
         }
      }
   }

   private void performActionSafe() {
      this.performActionImpl();
      if (this.reductionTarget == null) {
         this.reductionTarget = this.getEntity();
      }

      if (this.reductionTarget != null) {
         if (Module.mc.player.isSprinting()) {
            PacketUtils.sendPacket(PlayerInteractEntityC2SPacket.attack(this.reductionTarget, Module.mc.player.isSneaking()));
            Module.mc.player.setVelocity(Module.mc.player.getVelocity().multiply(0.6, 1.0, 0.6));
            Module.mc.player.swingHand(Hand.MAIN_HAND);
            AttackTracker.INSTANCE.performAction();
            KillAura.lastTimeUtil.reset();
            KillAura.activeValue = Module.mc.player.lastSprinting;
            this.applyText("Reduce.");
         } else if ((Boolean)this.debugFails.getValue()) {
            this.applyTextCustom("not sprinting.");
         }

         this.reductionTarget = null;
      } else {
         this.performActionCustom();
      }
   }

   private void performActionCustom() {
      if ((Boolean)this.debugFails.getValue()) {
         if (KillAura.target == null) {
            this.applyTextCustom("raytrace failed. no target.");
         } else {
            double dist = BoxRaytraceUtils.computePosition(BlockLookUtils.vector, KillAura.target.getBoundingBox()).distanceTo(BlockLookUtils.vector);
            this.applyTextCustom("raytrace failed. dist: " + dist);
         }
      }
   }

   @EventTarget(priority = -200)
   public void onDisableRotation(DisableRotationEvent disableRotationEvent) {
      if (Module.mc.player == null) {
         this.performActionAlt();
      } else if (this.fireballCooldownTicks > 0) {
         this.fireballCooldownTicks--;
         this.performActionAlt();
      } else {
         if (activeValue) {
            if (!ServerPacketManager.active) {
               this.performActionAlt();
               this.applyTextEx("Reset, reason: not desyncing.");
            } else {
               if (KillAura.target == null) {
                  this.performActionAlt();
                  this.applyTextEx("Reset, reason: aura no target.");
                  return;
               }

               if ((this.isValidEx() || (Boolean)this.multiTarget.getValue() && KillAura.targets.size() > 1) && this.isValidAlt()) {
                  this.setActiveEx(false);
               }
            }
         }

         if (activeValue && count > 0) {
            Module.mc.options.sprintKey.setPressed(true);
         }
      }
   }

   private boolean isValidAlt() {
      if (KillAura.target != null && Module.mc.world != null) {
         for (Entity entity : Module.mc.world.getEntities()) {
            if (entity instanceof PlayerEntity player) {
               Box boxAt = entity.dimensions.getBoxAt(player.getPos());
               if (EntityTargetUtils.isLivingEntity(entity)
                  && BoxRaytraceUtils.computePosition(Module.mc.player.getEyePos(), boxAt).distanceTo(Module.mc.player.getEyePos())
                     <= ((Number)this.releaseDistance.getValue()).doubleValue()) {
                  Vec3d vec = BoxRaytraceUtils.computePosition(BlockLookUtils.vector, boxAt);
                  Rotation rotationToPos = RotationManager.getRotationToPos(vec);
                  if (BlockLookUtils.isHovered(rotationToPos.yaw, rotationToPos.pitch, 3.0, boxAt)) {
                     KillAura.targets.add(entity);
                     KillAura.target = entity;
                     RotationManager.rotationRequests.add(new ValuePair<>(rotationToPos, 2));
                     return true;
                  }
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   @EventTarget
   public void onForward(ForwardEvent forwardEvent) {
      if (Module.mc.currentScreen == null && (activeValue && ServerPacketManager.active || count > 0)) {
         forwardEvent.setModified(true);
         forwardEvent.setForward(true);
         forwardEvent.setRight(false);
         forwardEvent.setLeft(false);
         forwardEvent.setBackward(false);
         forwardEvent.setSprint(true);
      }

      if (count > 0) {
         forwardEvent.setJump(false);
      }

      if (activeValue
         && ServerPacketManager.active
         && Criticals.mode.getDisplayTextEx().equalsIgnoreCase("Packet")
         && count <= 0
         && MovementUtils.countData <= 1
         && Southside.INSTANCE.getModuleManager().getModule(Criticals.class).isEnabled()
         && KillAura.target != null
         && Southside.INSTANCE.getModuleManager().getModule(KillAura.class).isValidTarget(KillAura.target)) {
         forwardEvent.setJump(false);
      }
   }

   @EventTarget(priority = -200)
   public void onCustomEvent2(RightClickEvent rightClickEvent) {
      if ((Boolean)this.fireballCooldown.getValue()
         && !rightClickEvent.isCancelled()
         && Module.mc.player != null
         && (Module.mc.player.getOffHandStack().getItem().equals(Items.FIRE_CHARGE) || Module.mc.player.getMainHandStack().getItem().equals(Items.FIRE_CHARGE))
         )
       {
         this.fireballCooldownTicks = 10;
      }
   }

   @EventTarget
   public void onDirection(DirectionEvent directionEvent) {
      if (MinecraftClient.getInstance() != null
         && MinecraftClient.getInstance().player != null
         && MinecraftClient.getInstance().world != null
         && !directionEvent.getDirection().equals(TransferOrigin.SEND)) {
         Packet packet = directionEvent.getPacket();
         if (packet instanceof EntityVelocityUpdateS2CPacket entityVelocityUpdateS2CPacket && entityVelocityUpdateS2CPacket.getEntityId() == Module.mc.player.getId() && this.fireballCooldownTicks <= 0) {
            boolean flag = Southside.INSTANCE.getModuleManager().getModule(KillAura.class).isEnabled()
               && !AutoGApple.activeValue
               && new Vec3d(entityVelocityUpdateS2CPacket.getVelocityX(), 0.0, entityVelocityUpdateS2CPacket.getVelocityZ()).length() >= this.targetHorizontalSpeed.getCurrentValue();
            if (flag && !NoSlow.activeValue) {
               activeValue = true;
               this.heldVelocityPacket = entityVelocityUpdateS2CPacket;
               if (!ServerPacketManager.active) {
                  ServerPacketManager.performAction();
               }
            }
         }

         if (packet instanceof PlayerPositionLookS2CPacket && activeValue) {
            this.applyTextEx("Flag Detected");
            this.fireballCooldownTicks = 2;
         }
      }
   }

   private void applyText(String text) {
      if ((Boolean)this.debug.getValue()) {
         this.applyTextDirect(text);
      }
   }

   private void applyTextEx(String text) {
      if ((Boolean)this.debug.getValue()) {
         this.applyTextCustom(text);
      }
   }

   @EventTarget
   public void onDrawContext(DrawContextEvent drawContextEvent) {
      if ((Boolean)this.showDelayHud.getValue() && activeValue && ServerPacketManager.active) {
         NanoVGHelper nvg = NanoVGHelper.INSTANCE;
         nvg.performAction(true, l -> performAction(nvg, l));
      }
   }

   private static void performAction(NanoVGHelper nvg, long l) {
      String text = "Delaying SPacket Ticks: " + ServerPacketManager.count;
      float textWidth = nvg.getTextWidth(l, text, 8.0F, OneConfigFonts.REGULAR);
      float f = (Module.mc.getWindow().getScaledWidth() - textWidth) / 2.0F;
      float f2 = Module.mc.getWindow().getScaledHeight() / 2.0F + 50.0F;
      nvg.draw(l, text, f, f2, Color.WHITE.getRGB(), 8.0F, OneConfigFonts.REGULAR);
   }
}
