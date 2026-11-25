package dev.southside.module.impl.misc;

import cc.polyfrost.oneconfig.config.core.OneColor;
import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.events.CameraEvent;
import dev.southside.event.events.DisableRotationEvent;
import dev.southside.event.events.StateEvent;
import dev.southside.manager.FriendManager;
import dev.southside.manager.IRCManager;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.ModuleManager;
import dev.southside.module.impl.combat.KillAura;
import dev.southside.module.impl.player.Blink;
import dev.southside.module.impl.world.Scaffold;
import dev.southside.module.values.BooleanValue;
import dev.southside.render.ProgressBarOverlay;
import dev.southside.utils.ColorValue;
import dev.southside.utils.BlinkUtils;
import dev.southside.utils.BoxMeshRenderer;
import dev.southside.utils.MillisTimer;
import dev.southside.utils.NumberValue;
import java.awt.Color;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class FakeLag extends Module {
   @SerializedName("startRange")
   public final NumberValue startRange = new NumberValue("StartRange", 6.0, 1.0, 8.0, 0.01);
   @SerializedName("releaseRange")
   public final NumberValue releaseRange = new NumberValue("ReleaseRange", 6.0, 1.0, 8.0, 0.01);
   @SerializedName("delay")
   public final NumberValue delay = new NumberValue("Delay", 300, 50, 1000, 10);
   @SerializedName("renderPos")
   public final BooleanValue renderPos = new BooleanValue("RenderPos", true);
   @SerializedName("boxColor")
   public final ColorValue boxColor = new ColorValue("Box Color", new OneColor(64, 176, 255, 80));
   @SerializedName("antiBow")
   public final BooleanValue antiBow = new BooleanValue("AntiBow", false);
   @SerializedName("antiBowRange")
   public final NumberValue antiBowRange = new NumberValue("AntiBow Range", 30.0, 5.0, 50.0, 1.0);
   @SerializedName("hitLog")
   public final BooleanValue hitLog = new BooleanValue("HitLog", false);
   @SerializedName("progressBar")
   public final BooleanValue progressBar = new BooleanValue("ProgressBar", true);
   @SerializedName("inputTimer")
   private final MillisTimer inputTimer = new MillisTimer();
   @SerializedName("lagTimer")
   private final MillisTimer lagTimer = new MillisTimer();
   @SerializedName("heldLagActive")
   private boolean heldLagActive;
   @SerializedName("blinkLagActive")
   private boolean blinkLagActive;
   @SerializedName("blinkPosition")
   private Vec3d blinkPosition;
   @SerializedName("startHealth")
   private float startHealth = -1.0F;
   @SerializedName("lastArrowOwner")
   private Entity lastArrowOwner;
   @SerializedName("previousPathPoint")
   private Vec3d previousPathPoint;
   @SerializedName("currentPathPoint")
   private Vec3d currentPathPoint;
   @SerializedName("lastPathPointTime")
   private long lastPathPointTime;
   @SerializedName("progressBarHandle")
   private ProgressBarOverlay.Inner progressBarHandle;

   public FakeLag() {
      super(Category.Misc, "FakeLag");
   }

   @Override
   public void onEnable() {
      this.performActionDirect();
      this.blinkLagActive = false;
      this.blinkPosition = null;
      this.previousPathPoint = null;
      this.currentPathPoint = null;
      this.performActionInternal();
   }

   @Override
   public void onDisable() {
      this.performActionImpl();
      this.performActionCustom();
      this.performActionInternal();
   }

   private boolean isValidTarget(PlayerEntity player) {
      if (AntiBot.isBot(player) || player.isSpectator() || player.isSleeping()) {
         return false;
      }

      if (IRCManager.getIrcUser(player.getUuid().toString()) != null) {
         return false;
      }

      Teams teams = Southside.INSTANCE.getModuleManager().getModule(Teams.class);
      FriendManager friendManager = Southside.INSTANCE != null ? Southside.INSTANCE.getFriendManager() : null;
      if (teams.isEnabled() && Teams.isValidTarget(player)) {
         return false;
      }

      if (friendManager != null) {
         String text = player.getGameProfile() != null ? player.getGameProfile().getName() : player.getName().getString();
         if (text != null && friendManager.friends.contains(text)) {
            return false;
         }
      }

      return true;
   }

   private boolean isValidTarget(Entity entity) {
      return entity instanceof PlayerEntity player && player != Module.mc.player && player.isAlive() && !player.isInvisible() && this.isValidTarget(player);
   }

   private boolean isValidEx() {
      for (Entity entity : Module.mc.world.getEntities()) {
         if (this.isValidTarget(entity) && Module.mc.player.distanceTo(entity) <= ((Number)this.startRange.getValue()).floatValue()) {
            return true;
         }
      }

      return false;
   }

   private boolean isValidAlt() {
      ModuleManager moduleManager = Southside.INSTANCE.getModuleManager();
      return !moduleManager.getModule(KillAura.class).isEnabled()
         || moduleManager.getModule(Scaffold.class).isEnabled()
         || moduleManager.getModule(Blink.class).isEnabled();
   }

   private void performActionAlt() {
      if (BlinkUtils.startBlink()) {
         this.heldLagActive = true;
         Vec3d vec = new Vec3d(Module.mc.player.getX(), Module.mc.player.getY(), Module.mc.player.getZ());
         this.previousPathPoint = vec;
         this.currentPathPoint = vec;
         this.lastPathPointTime = System.currentTimeMillis();
      }
   }

   private void performActionImpl() {
      if (this.heldLagActive) {
         this.performActionDirect();
         BlinkUtils.stopBlink();
      }
   }

   private void performActionDirect() {
      this.inputTimer.reset();
      this.heldLagActive = false;
      this.previousPathPoint = null;
      this.currentPathPoint = null;
      this.performActionInternal();
   }

   private void performActionSafe() {
      if (!this.blinkLagActive && !BlinkUtils.blinking) {
         if (BlinkUtils.startBlink()) {
            this.blinkLagActive = true;
            this.lagTimer.reset();
            this.blinkPosition = new Vec3d(Module.mc.player.getX(), Module.mc.player.getY(), Module.mc.player.getZ());
            this.startHealth = Module.mc.player.getHealth();
            this.previousPathPoint = this.blinkPosition;
            this.currentPathPoint = this.blinkPosition;
            this.lastPathPointTime = System.currentTimeMillis();
         }

         this.performActionByIndex();
      }
   }

   private void performActionCustom() {
      if (this.blinkLagActive) {
         this.blinkLagActive = false;
         this.blinkPosition = null;
         this.startHealth = -1.0F;
         this.lastArrowOwner = null;
         this.previousPathPoint = null;
         this.currentPathPoint = null;
         BlinkUtils.stopBlink();
         this.performActionInternal();
      }
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      this.performActionByIndex();
      if ((Boolean)this.antiBow.getValue() && Module.mc.player != null && Module.mc.world != null) {
         ModuleManager moduleManager = Southside.INSTANCE.getModuleManager();
         if (!moduleManager.getModule(Blink.class).isEnabled()) {
            if (!moduleManager.getModule(Scaffold.class).isEnabled() && KillAura.target == null) {
               boolean flag = false;
               ArrowEntity arrowEntity = null;
               double max_value = Double.MAX_VALUE;

               for (Entity entity : Module.mc.world.getEntities()) {
                  if (entity instanceof ArrowEntity arrowEntity2 && !arrowEntity2.isInGround() && arrowEntity2.getOwner() != Module.mc.player) {
                     double dist = Module.mc.player.distanceTo(arrowEntity2);
                     if (!(dist > ((Number)this.antiBowRange.getValue()).floatValue())) {
                        flag = true;
                        if (dist < max_value) {
                           max_value = dist;
                           arrowEntity = arrowEntity2;
                        }
                     }
                  }
               }

               if (flag && !this.blinkLagActive) {
                  if (this.heldLagActive) {
                     this.performActionImpl();
                  }

                  this.performActionSafe();
               }

               if (this.blinkLagActive) {
                  if (this.lagTimer.hasTimeElapsed(((Number)this.delay.getValue()).longValue())) {
                     BlinkUtils.releaseAll();
                     if ((Boolean)this.hitLog.getValue()) {
                        this.applyText("§etimeout");
                     }

                     this.performActionCustom();
                  } else {
                     if (arrowEntity != null) {
                        Entity owner = arrowEntity.getOwner();
                        if (owner != null) {
                           this.lastArrowOwner = owner;
                        }

                        if (this.isValid(arrowEntity, max_value)) {
                           BlinkUtils.releaseAll();
                           this.blinkPosition = new Vec3d(Module.mc.player.getX(), Module.mc.player.getY(), Module.mc.player.getZ());
                           this.performAction(this.blinkPosition);
                        }
                     }

                     if (!flag) {
                        if ((Boolean)this.hitLog.getValue()) {
                           this.applyText((String)null);
                        }

                        this.performActionCustom();
                     }
                  }
               }
            } else {
               this.performActionCustom();
            }
         }
      } else {
         this.performActionCustom();
      }
   }

   private boolean isValid(ArrowEntity arrowEntity, double d) {
      Vec3d pos = arrowEntity.getPos();
      Vec3d velocity = arrowEntity.getVelocity();
      Vec3d delta = this.blinkPosition.subtract(pos);
      double d2 = velocity.dotProduct(delta);
      if (d2 > 0.0) {
         double len = velocity.length();
         if (len > 0.01) {
            double d3 = delta.length() / len;
            if (d3 <= 3.0) {
               return true;
            }
         }

         return d <= 3.0;
      } else {
         return false;
      }
   }

   @EventTarget(priority = 100)
   public void onDisableRotation(DisableRotationEvent disableRotationEvent) {
      if (Module.mc.player != null && Module.mc.world != null && !this.blinkLagActive) {
         if (this.isValidAlt()) {
            this.performActionImpl();
         } else if (KillAura.target != null && Module.mc.player.distanceTo(KillAura.target) <= ((Number)this.releaseRange.getValue()).floatValue()) {
            this.performActionImpl();
         } else if (this.isValidEx()) {
            if (!this.heldLagActive) {
               this.performActionAlt();
            }

            if (this.heldLagActive) {
               this.performActionWithOffset();
            }
         } else {
            this.performActionImpl();
         }
      }
   }

   private void performActionWithOffset() {
      if (this.inputTimer.hasTimeElapsed(((Number)this.delay.getValue()).intValue())
         || Module.mc.options.attackKey.isPressed()
         || Module.mc.options.useKey.isPressed()) {
         this.performAction(new Vec3d(Module.mc.player.getX(), Module.mc.player.getY(), Module.mc.player.getZ()));
         BlinkUtils.releaseAll();
         this.inputTimer.reset();
      }
   }

   private void performAction(Vec3d vec) {
      this.previousPathPoint = this.currentPathPoint == null ? vec : this.currentPathPoint;
      this.currentPathPoint = vec;
      this.lastPathPointTime = System.currentTimeMillis();
   }

   private void performActionByIndex() {
      if ((Boolean)this.progressBar.getValue() && (this.heldLagActive || this.blinkLagActive)) {
         ProgressBarOverlay progressBarOverlay = Southside.INSTANCE.getProgressBarOverlay();
         if (progressBarOverlay != null) {
            long l = ((Number)this.delay.getValue()).longValue();
            if (l <= 0L) {
               this.performActionInternal();
            } else {
               if (this.progressBarHandle == null || !this.progressBarHandle.isValid()) {
                  this.progressBarHandle = progressBarOverlay.getInner((int)l, "FakeLag");
               }

               long l2 = this.blinkLagActive ? this.lagTimer.getElapsed() : this.inputTimer.getElapsed();
               this.progressBarHandle.onKeyTyped((int)Math.min(l, Math.max(0L, l2)));
            }
         }
      } else {
         this.performActionInternal();
      }
   }

   private void performActionInternal() {
      if (this.progressBarHandle != null) {
         this.progressBarHandle.performActionAlt();
         this.progressBarHandle = null;
      }
   }

   private void applyText(String text) {
      if (Module.mc.player != null) {
         String text2 = "Unknown";
         if (this.lastArrowOwner instanceof PlayerEntity player) {
            text2 = player.getGameProfile() != null ? player.getGameProfile().getName() : player.getName().getString();
         } else if (this.lastArrowOwner != null) {
            text2 = this.lastArrowOwner.getName().getString();
         }

         float health = Module.mc.player.getHealth();
         float f = this.startHealth > 0.0F ? this.startHealth - health : 0.0F;
         if (f < 0.0F) {
            f = 0.0F;
         }

         int n = 0;
         PlayerListEntry playerListEntry = Module.mc.player.networkHandler.getPlayerListEntry(Module.mc.player.getUuid());
         if (playerListEntry != null) {
            n = playerListEntry.getLatency();
         }

         String text3 = text != null ? text : (f <= 0.0F ? "§amiss" : "§cfailed");
         String formatted = String.format("Target: §f%s §7| Dmg: §f%.1f §7| Ping: §f%dms §7| Type: %s", text2, f, n, text3);
         this.applyTextDirect(formatted);
      }
   }

   private boolean isValidImpl() {
      return (Boolean)this.renderPos.getValue()
         && Module.mc.player != null
         && this.currentPathPoint != null
         && this.isEnabled()
         && !Southside.INSTANCE.getModuleManager().getModule(Blink.class).isEnabled();
   }

   @EventTarget
   public void onCamera(CameraEvent cameraEvent) {
      if (this.isValidImpl()) {
         Vec3d vec;
         if (this.previousPathPoint != null && this.currentPathPoint != null) {
            long l = System.currentTimeMillis() - this.lastPathPointTime;
            float f = this.blinkLagActive ? 50.0F : ((Number)this.delay.getValue()).floatValue();
            float f2 = Math.min(1.0F, (float)l / f);
            vec = this.previousPathPoint.lerp(this.currentPathPoint, f2);
         } else {
            if (this.currentPathPoint == null) {
               return;
            }

            vec = this.currentPathPoint;
         }

         float f3 = 0.3F;
         float f4 = 1.8F;
         Box box = new Box(vec.x - f3, vec.y, vec.z - f3, vec.x + f3, vec.y + f4, vec.z + f3).expand(0.2);
         Color color = ((OneColor)this.boxColor.getValue()).blendColor();
         BoxMeshRenderer.renderBoxEx(box, color);
      }
   }
}
