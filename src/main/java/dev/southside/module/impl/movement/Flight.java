package dev.southside.module.impl.movement;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.TransferOrigin;
import dev.southside.event.events.ClientWorldEvent;
import dev.southside.event.events.DirectionEvent;
import dev.southside.event.events.StateEvent;
import dev.southside.event.events.XEvent;
import dev.southside.event.events.PlayerMoveEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.BlinkUtils;
import dev.southside.utils.MovementMathUtils;
import dev.southside.utils.IText;
import dev.southside.utils.MillisTimer;
import dev.southside.utils.NumberValue;
import dev.southside.utils.ModeValue;
import dev.southside.utils.FakePlayerEntity;
import dev.southside.utils.MovementUtils;
import dev.southside.utils.math.RandomUtils;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookAndOnGround;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public class Flight extends Module {
   @SerializedName("mode")
   public final ModeValue mode = new ModeValue("Mode", "Vanilla", "Vanilla", "Cubecraft", "CubeZoom");
   @SerializedName("horizontal")
   public final NumberValue horizontal = new NumberValue("Horizontal", 1.0F, 0.0F, 10.0F);
   @SerializedName("vertical")
   public final NumberValue vertical = new NumberValue("Vertical", 0.78F, 0.0F, 5.0F);
   @SerializedName("speed")
   public final NumberValue speed = new NumberValue("Speed", 0.33, 0.1, 0.34, 0.01);
   @SerializedName("releaseDelay")
   public final NumberValue releaseDelay = new NumberValue("Release Delay", 300, 100, 2000, 50);
   @SerializedName("viewBobbing")
   public final BooleanValue viewBobbing = new BooleanValue("View Bobbing", true);
   @SerializedName("releaseTimer")
   private final MillisTimer releaseTimer = new MillisTimer();
   @SerializedName("idleTimer")
   private final MillisTimer idleTimer = new MillisTimer();
   @SerializedName("jumpCooldownSet")
   private boolean jumpCooldownSet;
   @SerializedName("holdingPackets")
   private boolean holdingPackets;
   @SerializedName("fakePlayer")
   private FakePlayerEntity fakePlayer;
   private static final long n = 3000L;

   public Flight() {
      super(Category.Movement, "Flight");
   }

   @Override
   public void postInit() {
      super.postInit();
      this.hideIf(this.horizontal.getLabel(), () -> this.mode.matches("Cubecraft"));
      this.hideIf(this.vertical.getLabel(), () -> this.mode.matches("Cubecraft"));
      this.hideIf(this.speed.getLabel(), () -> !this.mode.matches("Cubecraft"));
      this.hideIf(this.releaseDelay.getLabel(), () -> !this.mode.matches("CubeZoom"));
      this.hideIf(this.viewBobbing.getLabel(), () -> !this.mode.matches("CubeZoom"));
   }

   @Override
   public void onEnable() {
      this.jumpCooldownSet = false;
      this.idleTimer.reset();
      if (this.mode.matches("CubeZoom")) {
         if (mc2.player != null && mc2.world != null) {
            this.fakePlayer = new FakePlayerEntity(mc2.player, mc2.player.getGameProfile().getName(), 20.0F, true);
            this.fakePlayer.spawned = true;
            this.fakePlayer.targeted = true;
            this.fakePlayer.addEntity();
         }

         this.holdingPackets = BlinkUtils.startBlink();
         this.releaseTimer.reset();
      }
   }

   @Override
   public void onDisable() {
      this.jumpCooldownSet = false;
      this.performActionAlt();
      this.performActionImpl();
   }

   @EventTarget
   public void onClientWorld(ClientWorldEvent clientWorldEvent) {
      this.jumpCooldownSet = false;
      this.performActionAlt();
      this.performActionImpl();
   }

   @EventTarget
   public void onDirection(DirectionEvent directionEvent) {
      if (mc2.player != null
         && this.mode.matches("CubeZoom")
         && directionEvent.getDirection() == TransferOrigin.SEND
         && this.fakePlayer != null
         && this.releaseTimer.hasTimeElapsed(((Number)this.releaseDelay.getValue()).intValue())) {
         this.handlePacket(directionEvent.getPacket());
      }
   }

   @EventTarget
   public void onX(XEvent xevent) {
      this.setInfoText(this.mode.getDisplayTextEx());
      if (mc2.player != null) {
         if (this.holdingPackets && !BlinkUtils.blinking) {
            this.holdingPackets = false;
         }

         if (this.mode.matches("Vanilla")) {
            this.initPostRunnable();
         } else if (this.mode.matches("CubeZoom")) {
            if (!MovementUtils.isValid()) {
               this.performActionAlt();
               return;
            }

            this.initPostRunnable();
            if (this.releaseTimer.hasTimeElapsed(((Number)this.releaseDelay.getValue()).intValue())) {
               if (this.holdingPackets) {
                  BlinkUtils.releaseAll();
               } else {
                  this.holdingPackets = BlinkUtils.startBlink();
               }

               this.releaseTimer.reset();
               if (this.fakePlayer != null) {
                  this.fakePlayer.setPosition(mc2.player.getPos());
                  this.fakePlayer.setYaw(mc2.player.getYaw());
                  this.fakePlayer.setPitch(mc2.player.getPitch());
               }
            }
         }
      }
   }

   private void initPostRunnable() {
      if (MovementUtils.isValid()) {
         double[] doubleList = MovementUtils.getDoubleList(((Number)this.horizontal.getValue()).floatValue());
         mc2.player.setVelocity(doubleList[0], 0.0, doubleList[1]);
      } else {
         mc2.player.setVelocity(Vec3d.ZERO);
      }

      if (mc2.options.jumpKey.isPressed()) {
         mc2.player.setVelocity(mc2.player.getVelocity().add(0.0, ((Number)this.vertical.getValue()).floatValue(), 0.0));
      }

      if (mc2.options.sneakKey.isPressed()) {
         mc2.player.setVelocity(mc2.player.getVelocity().add(0.0, -((Number)this.vertical.getValue()).floatValue(), 0.0));
      }
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      if (mc2.player != null) {
         if (this.mode.matches("CubeZoom")) {
            if (mc2.isInSingleplayer()) {
               this.setEnabled(false);
               return;
            }

            if (mc2.player.isOnGround() || MovementUtils.isValid()) {
               this.idleTimer.reset();
            } else if (this.idleTimer.hasTimeElapsed(n)) {
               if (mc2.inGameHud != null) {
                  mc2.inGameHud.getChatHud().addMessage(Text.empty().append(IText.TEXT).append(Text.literal("想死号?!")));
               }

               this.setEnabled(false);
            }
         } else if (this.mode.matches("Cubecraft") && !mc2.player.isOnGround()) {
            double d = mc2.player.isSneaking() ? -0.4 : (mc2.player.input.playerInput.jump() ? 0.42 : 0.2);
            Vec3d vec = new Vec3d(mc2.player.getVelocity().x, d, mc2.player.getVelocity().z);
            double d2 = (float)(RandomUtils.RAND.nextDouble() * (this.speed.getMax() - this.speed.getMin()) + this.speed.getMin());
            mc2.player.setVelocity(this.computePosition(vec, d2));
            if (!this.jumpCooldownSet) {
               MovementUtils.setCount(6);
               this.jumpCooldownSet = true;
            }
         }
      }
   }

   @EventTarget
   public void onXEvent2(PlayerMoveEvent playerMoveEvent) {
      if (mc2.player != null && this.mode.matches("Cubecraft")) {
         Vec3d vec = this.computePosition(new Vec3d(playerMoveEvent.getX(), playerMoveEvent.getY(), playerMoveEvent.getZ()), 1.0);
         playerMoveEvent.setX(vec.x);
         playerMoveEvent.setY(vec.y);
         playerMoveEvent.setZ(vec.z);
      }
   }

   private Vec3d computePosition(Vec3d vec, double d) {
      boolean isPressed = mc2.options.forwardKey.isPressed();
      boolean isPressed2 = mc2.options.backKey.isPressed();
      boolean isPressed3 = mc2.options.leftKey.isPressed();
      boolean isPressed4 = mc2.options.rightKey.isPressed();
      float yaw = mc2.player.getYaw();
      if (isPressed) {
         yaw += isPressed3 ? 45.0F : (isPressed4 ? -45.0F : 0.0F);
      } else if (isPressed2) {
         yaw += 180.0F;
         yaw += isPressed3 ? -45.0F : (isPressed4 ? 45.0F : 0.0F);
      } else if (isPressed3) {
         yaw += 90.0F;
      } else if (isPressed4) {
         yaw -= 90.0F;
      }

      return MovementMathUtils.computePosition(vec, d, 1.0, isPressed || isPressed2 || isPressed3 || isPressed4, yaw);
   }

   private void handlePacket(Packet<?> packet) {
      if (packet instanceof PlayerMoveC2SPacket playerMoveC2SPacket && (packet instanceof PositionAndOnGround || packet instanceof LookAndOnGround || packet instanceof Full)) {
         this.fakePlayer.setPosition(playerMoveC2SPacket.getX(this.fakePlayer.getX()), playerMoveC2SPacket.getY(this.fakePlayer.getY()), playerMoveC2SPacket.getZ(this.fakePlayer.getZ()));
         if (!(packet instanceof PositionAndOnGround)) {
            this.fakePlayer.setYaw(playerMoveC2SPacket.getYaw(this.fakePlayer.getYaw()));
            this.fakePlayer.setHeadYaw(this.fakePlayer.getYaw());
            this.fakePlayer.setPitch(playerMoveC2SPacket.getPitch(this.fakePlayer.getPitch()));
         }

         this.fakePlayer.setOnGround(playerMoveC2SPacket.isOnGround());
      } else if (packet instanceof ClientCommandC2SPacket clientCommandC2SPacket) {
         switch (clientCommandC2SPacket.getMode()) {
            case START_SPRINTING:
               this.fakePlayer.setSprinting(true);
               break;
            case STOP_SPRINTING:
               this.fakePlayer.setSprinting(false);
               break;
            case PRESS_SHIFT_KEY:
               this.fakePlayer.setSneaking(true);
               break;
            case RELEASE_SHIFT_KEY:
               this.fakePlayer.setSneaking(false);
         }
      } else if (packet instanceof HandSwingC2SPacket handSwingC2SPacket) {
         this.fakePlayer.swingHand(handSwingC2SPacket.getHand());
      }
   }

   private void performActionAlt() {
      if (this.holdingPackets) {
         BlinkUtils.stopBlink();
         this.holdingPackets = false;
      }
   }

   private void performActionImpl() {
      if (this.fakePlayer != null) {
         if (this.fakePlayer.getWorld() instanceof ClientWorld world) {
            world.removeEntity(this.fakePlayer.getId(), RemovalReason.DISCARDED);
         }

         this.fakePlayer = null;
      }
   }
}
