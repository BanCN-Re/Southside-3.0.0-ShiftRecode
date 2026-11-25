package dev.southside.mixin.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import dev.southside.Southside;
import dev.southside.event.Stage;
import dev.southside.event.events.ClientPlayerEntityEvent;
import dev.southside.event.events.SlowDownEvent;
import dev.southside.event.events.SprintingEvent;
import dev.southside.event.events.XEvent;
import dev.southside.event.events.PlayerMoveEvent;
import dev.southside.module.impl.misc.Disabler;
import dev.southside.utils.PacketUtils;
import dev.southside.utils.MovementUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookAndOnGround;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.OnGroundOnly;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity extends PlayerEntity {
   @Shadow
   public int ticksSinceLastPositionPacketSent;
   @Shadow
   public double lastX;
   @Shadow
   public double lastBaseY;
   @Shadow
   public double lastZ;
   @Shadow
   public float lastYaw;
   @Shadow
   public float lastPitch;
   @Shadow
   public boolean lastOnGround;
   @Shadow
   public boolean lastSprinting;
   @Shadow
   public boolean lastSneaking;
   @Shadow
   private boolean autoJumpEnabled = true;
   @Shadow
   @Final
   public ClientPlayNetworkHandler networkHandler;
   @Shadow
   @Final
   protected MinecraftClient client;
   @Shadow
   private boolean lastHorizontalCollision;
   private static final int POSITION_PACKET_INTERVAL_TICKS = 20;

   @Shadow
   public abstract boolean isSneaking();

   @Overwrite
   private void sendMovementPackets() {
      if (!this.isOnGround()) {
         MovementUtils.countValue++;
         MovementUtils.countData = 0;
      } else {
         MovementUtils.countValue = 0;
         MovementUtils.countData++;
         MovementUtils.countRef = MovementUtils.countData;
      }

      this.sendSprintingPacket();
      XEvent xevent = new XEvent(this.getX(), this.getY(), this.getZ(), this.getYaw(), this.getPitch(), this.isOnGround(), this.horizontalCollision, Stage.PRE);
      Southside.INSTANCE.getEventBus().postCancellable(xevent);
      float f = Disabler.normalizeYawDiff(xevent.getYaw(), this.lastYaw);
      if (this.isCamera()) {
         double d = xevent.getX() - this.lastX;
         double d2 = xevent.getY() - this.lastBaseY;
         double d3 = xevent.getZ() - this.lastZ;
         double d4 = f - this.lastYaw;
         double d5 = xevent.getPitch() - this.lastPitch;
         MovementUtils.value = Math.abs(d5);
         this.ticksSinceLastPositionPacketSent++;
         boolean flag = MathHelper.squaredMagnitude(d, d2, d3) > MathHelper.square(2.0E-4)
            || this.ticksSinceLastPositionPacketSent >= 20
            || xevent.isForceFullPacket();
         boolean flag2 = d4 != 0.0 || d5 != 0.0 || xevent.isForceFullPacket();
         if (flag && flag2) {
            this.networkHandler
               .sendPacket(new Full(xevent.getX(), xevent.getY(), xevent.getZ(), f, xevent.getPitch(), xevent.isOnGround(), xevent.isHorizontalCollision()));
         } else if (flag) {
            this.networkHandler.sendPacket(new PositionAndOnGround(xevent.getX(), xevent.getY(), xevent.getZ(), xevent.isOnGround(), xevent.isHorizontalCollision()));
         } else if (flag2) {
            this.networkHandler.sendPacket(new LookAndOnGround(f, xevent.getPitch(), xevent.isOnGround(), xevent.isHorizontalCollision()));
         } else if (this.lastOnGround != xevent.isOnGround() || this.lastHorizontalCollision != xevent.isHorizontalCollision()) {
            this.networkHandler.sendPacket(new OnGroundOnly(xevent.isOnGround(), xevent.isHorizontalCollision()));
         } else if (xevent.isForceGroundPacket()) {
            this.networkHandler.sendPacket(new OnGroundOnly(xevent.isOnGround(), xevent.isHorizontalCollision()));
         } else {
            PacketUtils.trackMovementPacket(new OnGroundOnly(xevent.isOnGround(), xevent.isHorizontalCollision()));
         }

         Disabler.performActionAlt();
         if (flag) {
            this.lastX = xevent.getX();
            this.lastBaseY = xevent.getY();
            this.lastZ = xevent.getZ();
            this.ticksSinceLastPositionPacketSent = 0;
         }

         if (flag2) {
            this.lastYaw = f;
            this.lastPitch = xevent.getPitch();
         }

         this.lastOnGround = xevent.isOnGround();
         this.lastHorizontalCollision = xevent.isHorizontalCollision();
         this.autoJumpEnabled = (Boolean)(Object)this.client.options.getAutoJump().getValue();
         xevent.setPhase(Stage.POST);
         Southside.INSTANCE.getEventBus().postCancellable(xevent);
      }
   }

   @Inject(
      method = "Lnet/minecraft/client/network/ClientPlayerEntity;tick()V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;tick()V", shift = Shift.BEFORE, ordinal = 0),
      cancellable = true
   )
   private void onTickInvoke(CallbackInfo ci) {
      MovementUtils.countPrimary++;
      ClientPlayerEntityEvent clientPlayerEntityEvent = new ClientPlayerEntityEvent();
      Southside.INSTANCE.getEventBus().postCancellable(clientPlayerEntityEvent);
      MinecraftClient.getInstance().handleInputEvents();
      if (MovementUtils.count > 0) {
         MovementUtils.count--;
         ci.cancel();
      }
   }

   @Inject(
      method = "Lnet/minecraft/client/network/ClientPlayerEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V"
      ),
      cancellable = true
   )
   public void onMoveInvoke(MovementType movementType, Vec3d vec, CallbackInfo ci) {
      PlayerMoveEvent playerMoveEvent = new PlayerMoveEvent(vec.x, vec.y, vec.z);
      Southside.INSTANCE.getEventBus().postCancellable(playerMoveEvent);
      if (playerMoveEvent.isCancelled()) {
         super.move(movementType, new Vec3d(playerMoveEvent.getX(), playerMoveEvent.getY(), playerMoveEvent.getZ()));
         ci.cancel();
      }
   }

   @Overwrite
   private void sendSprintingPacket() {
      SprintingEvent sprintingEvent = new SprintingEvent(this.isSprinting());
      Southside.INSTANCE.getEventBus().postCancellable(sprintingEvent);
      if (!sprintingEvent.isCancelled()) {
         boolean isSprinting = sprintingEvent.isSprinting();
         if (isSprinting != this.lastSprinting) {
            Mode mode = isSprinting ? Mode.START_SPRINTING : Mode.STOP_SPRINTING;
            this.networkHandler.sendPacket(new ClientCommandC2SPacket(this, mode));
            this.lastSprinting = isSprinting;
         }
      }
   }

   public MixinClientPlayerEntity(World world, BlockPos pos, float f, GameProfile gameProfile) {
      super(world, pos, f, gameProfile);
   }

   @ModifyExpressionValue(
      method = "Lnet/minecraft/client/network/ClientPlayerEntity;tickMovement()V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z")
   )
   private boolean modifyTickMovement(boolean flag) {
      SlowDownEvent slowDownEvent = new SlowDownEvent();
      Southside.INSTANCE.getEventBus().postCancellable(slowDownEvent);
      return slowDownEvent.isCancelled() ? false : flag;
   }

   @Shadow
   protected abstract boolean isCamera();
}
