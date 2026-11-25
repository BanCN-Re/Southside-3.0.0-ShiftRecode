package dev.southside.mixin.misc;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.southside.Southside;
import dev.southside.event.events.ServerJoinEvent;
import dev.southside.event.events.WorldJoinEvent;
import dev.southside.event.events.CustomPayloadEvent;
import dev.southside.event.events.MessageEvent;
import dev.southside.module.impl.misc.ChatBypass;
import dev.southside.module.impl.misc.Disabler;
import dev.southside.module.impl.movement.TP2Bed;
import dev.southside.utils.NetworkReadyEvent;
import dev.southside.utils.IEntityInterpolation;
import dev.southside.utils.IMinecraft;
import dev.southside.utils.IText;
import dev.southside.utils.ServerPacketManager;
import dev.southside.utils.RotationManager;
import dev.southside.utils.Rotation;
import java.util.Set;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerPosition;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.play.EnterReconfigurationS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerAbilitiesS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {
   @Unique
   private float savedClientYaw;
   @Unique
   private float savedClientPitch;
   @Unique
   private boolean restoreClientRotation;
   @Shadow
   private ClientWorld world;
   @Unique
   private boolean worldAlreadyLoaded;

   @Inject(
      method = "onPlayerAbilities",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V",
         shift = Shift.AFTER
      )
   )
   private void onTp2BedAbilities(PlayerAbilitiesS2CPacket playerAbilitiesS2CPacket, CallbackInfo ci) {
      TP2Bed.performAction((ClientPlayNetworkHandler)(Object)this, playerAbilitiesS2CPacket.isFlying());
   }

   @Inject(
      method = "onGameStateChange",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V",
         shift = Shift.AFTER
      )
   )
   private void onTp2BedGameMode(GameStateChangeS2CPacket gameStateChangeS2CPacket, CallbackInfo ci) {
      TP2Bed.performAction((ClientPlayNetworkHandler)(Object)this, gameStateChangeS2CPacket);
   }

   @Inject(
      method = "onPlayerRespawn",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V",
         shift = Shift.AFTER
      )
   )
   private void onTp2BedRespawn(PlayerRespawnS2CPacket playerRespawnS2CPacket, CallbackInfo ci) {
      TP2Bed.performAction((ClientPlayNetworkHandler)(Object)this);
   }

   @Inject(
      method = "onPlayerPositionLook",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V",
         shift = Shift.AFTER
      ),
      cancellable = true
   )
   private void onTp2BedPosition(PlayerPositionLookS2CPacket playerPositionLookS2CPacket, CallbackInfo ci) {
      this.restoreClientRotation = false;
      if (TP2Bed.isValid((ClientPlayNetworkHandler)(Object)this, playerPositionLookS2CPacket)) {
         ci.cancel();
      }
   }

   @Inject(method = "onPlayerPositionLook", at = @At("TAIL"))
   private void afterTp2BedPosition(PlayerPositionLookS2CPacket playerPositionLookS2CPacket, CallbackInfo ci) {
      TP2Bed.performActionEx((ClientPlayNetworkHandler)(Object)this);
   }

   @Inject(
      method = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;onPlayerPositionLook(Lnet/minecraft/network/packet/s2c/play/PlayerPositionLookS2CPacket;)V",
      at = @At("TAIL")
   )
   public void restoreRotationAfterPositionUpdate(PlayerPositionLookS2CPacket playerPositionLookS2CPacket, CallbackInfo ci) {
      if (this.restoreClientRotation) {
         this.restoreClientRotation = false;
         ClientPlayerEntity player = MinecraftClient.getInstance().player;
         if (player != null) {
            player.lastYaw = player.getYaw();
            player.lastPitch = player.getPitch();
            player.setYaw(this.savedClientYaw);
            player.setPitch(this.savedClientPitch);
         }
      }
   }

   @Inject(
      method = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;onEnterReconfiguration(Lnet/minecraft/network/packet/s2c/play/EnterReconfigurationS2CPacket;)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V",
         shift = Shift.AFTER
      )
   )
   private void onEnterReconfiguration(EnterReconfigurationS2CPacket enterReconfigurationS2CPacket, CallbackInfo ci) {
      Southside.INSTANCE.getEventBus().postEvent(ServerJoinEvent.INSTANCE);
   }

   @WrapOperation(
      method = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;onCustomPayload(Lnet/minecraft/network/packet/CustomPayload;)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;warnOnUnknownPayload(Lnet/minecraft/network/packet/CustomPayload;)V"
      )
   )
   private void wrapOnCustomPayload(ClientPlayNetworkHandler networkHandler, CustomPayload customPayload, Operation<Void> operation) {
      Southside.INSTANCE.getEventBus().postEvent(new CustomPayloadEvent(customPayload));
      operation.call(new Object[]{networkHandler, customPayload});
   }

   @Inject(
      method = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;onPlayerPositionLook(Lnet/minecraft/network/packet/s2c/play/PlayerPositionLookS2CPacket;)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;setPosition(Lnet/minecraft/entity/player/PlayerPosition;Ljava/util/Set;Lnet/minecraft/entity/Entity;Z)Z",
         shift = Shift.BEFORE
      )
   )
   public void applyRotationBeforePositionUpdate(PlayerPositionLookS2CPacket playerPositionLookS2CPacket, CallbackInfo ci) {
      if (MinecraftClient.getInstance().player != null) {
         this.restoreClientRotation = true;
         this.savedClientYaw = MinecraftClient.getInstance().player.getYaw();
         this.savedClientPitch = MinecraftClient.getInstance().player.getPitch();
         ClientPlayerEntity player = MinecraftClient.getInstance().player;
         Rotation positionUpdateRotation = getPositionUpdateRotation();
         player.setYaw(Disabler.normalizeYawDiff(positionUpdateRotation.yaw, RotationManager.getServerRotation().yaw));
         ClientPlayerEntity player2 = MinecraftClient.getInstance().player;
         Rotation positionUpdateRotation2 = getPositionUpdateRotation();
         player2.setPitch(positionUpdateRotation2.getPitch());
      }
   }

   @Unique
   private static Rotation getPositionUpdateRotation() {
      if (RotationManager.currentRotation != null) {
         return RotationManager.currentRotation;
      } else {
         return RotationManager.mc.player != null ? new Rotation(RotationManager.mc.player.getYaw(), RotationManager.mc.player.getPitch()) : new Rotation(0.0F, 0.0F);
      }
   }

   @Inject(
      method = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;setPosition(Lnet/minecraft/entity/player/PlayerPosition;Ljava/util/Set;Lnet/minecraft/entity/Entity;Z)Z",
      at = @At("HEAD"),
      cancellable = true
   )
   private static void onSetPositionHead(PlayerPosition playerPosition, Set<PositionFlag> set, Entity entity, boolean flag, CallbackInfoReturnable<Boolean> cir) {
      if (entity instanceof OtherClientPlayerEntity otherClientPlayerEntity) {
         IEntityInterpolation ientityinterpolation = (IEntityInterpolation)otherClientPlayerEntity;
         PlayerPosition playerPosition2 = PlayerPosition.fromEntityLerpTarget(entity);
         PlayerPosition playerPosition3 = PlayerPosition.apply(playerPosition2, playerPosition, set);
         boolean flag2 = playerPosition2.position().squaredDistanceTo(playerPosition3.position()) > 4096.0;
         if (flag && !flag2) {
            entity.updateTrackedPositionAndAngles(playerPosition3.position().getX(), playerPosition3.position().getY(), playerPosition3.position().getZ(), playerPosition3.yaw(), playerPosition3.pitch(), 3);
            entity.setVelocity(playerPosition3.deltaMovement());
            cir.setReturnValue(true);
         } else {
            if (ServerPacketManager.active) {
               synchronized (ServerPacketManager.linkedBlockingQueue) {
                  ServerPacketManager.linkedBlockingQueue.add((Runnable)() -> {
                     ientityinterpolation.setInterpolatedPosition(playerPosition3.position());
                     ientityinterpolation.setTargetPosition(playerPosition3.position());
                     entity.setPosition(playerPosition3.position());
                  });
               }

               entity.setPos(playerPosition3.position().x, playerPosition3.position().y, playerPosition3.position().z);
            } else {
               ientityinterpolation.setInterpolatedPosition(playerPosition3.position());
               ientityinterpolation.setTargetPosition(playerPosition3.position());
               entity.setPosition(playerPosition3.position());
            }

            entity.setVelocity(playerPosition3.deltaMovement());
            entity.setYaw(playerPosition3.yaw());
            entity.setPitch(playerPosition3.pitch());
            PlayerPosition playerPosition4 = new PlayerPosition(entity.getLastRenderPos(), Vec3d.ZERO, entity.prevYaw, entity.prevPitch);
            PlayerPosition playerPosition5 = PlayerPosition.apply(playerPosition4, playerPosition, set);
            entity.setPrevPositionAndAngles(playerPosition5.position(), playerPosition5.yaw(), playerPosition5.pitch());
            cir.setReturnValue(false);
         }
      }
   }

   @Inject(
      method = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;onGameJoin(Lnet/minecraft/network/packet/s2c/play/GameJoinS2CPacket;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;addEntity(Lnet/minecraft/entity/Entity;)V", shift = Shift.BEFORE)
   )
   private void beforePlayerJoinsWorld(GameJoinS2CPacket gameJoinS2CPacket, CallbackInfo ci) {
      Southside.INSTANCE.getEventBus().postEvent(NetworkReadyEvent.INSTANCE);
   }

   @Inject(
      method = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;onGameJoin(Lnet/minecraft/network/packet/s2c/play/GameJoinS2CPacket;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;init()V", shift = Shift.AFTER)
   )
   private void afterPlayerJoinsWorld(GameJoinS2CPacket gameJoinS2CPacket, CallbackInfo ci) {
      if (this.worldAlreadyLoaded) {
         Southside.INSTANCE.getEventBus().postEvent(ServerJoinEvent.INSTANCE);
      }

      Southside.INSTANCE.getEventBus().postEvent(WorldJoinEvent.INSTANCE);
   }

   @ModifyVariable(
      method = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendChatMessage(Ljava/lang/String;)V",
      at = @At("HEAD"),
      ordinal = 0,
      argsOnly = true
   )
   private String modifySendChatMessage(String text) {
      return Southside.INSTANCE.getModuleManager().getModule(ChatBypass.class).isEnabled() ? ChatBypass.formatText(text) : text;
   }

   @Inject(method = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendChatMessage(Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true)
   private void onSendChatMessageHead(String text, CallbackInfo ci) {
      MessageEvent messageEvent = new MessageEvent(text);
      Southside.INSTANCE.getEventBus().postCancellable(messageEvent);
      if (text.startsWith(".")) {
         try {
            Southside.INSTANCE.getCommandManager().applyText(text.substring(1));
         } catch (CommandSyntaxException e) {
            String message = e.getMessage();
            if (message != null && MinecraftClient.getInstance().inGameHud != null) {
               MinecraftClient.getInstance()
                  .inGameHud
                  .getChatHud()
                  .addMessage(Text.empty().append(IText.TEXT).append(Text.literal(message).setStyle(Style.EMPTY.withColor(Formatting.RED))));
            }
         }

         IMinecraft.mc.inGameHud.getChatHud().addToMessageHistory(text);
         ci.cancel();
      }

      if (messageEvent.isCancelled()) {
         ci.cancel();
      }
   }

   @Inject(
      method = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;onGameJoin(Lnet/minecraft/network/packet/s2c/play/GameJoinS2CPacket;)V",
      at = @At("HEAD")
   )
   private void captureWorldBeforeJoin(GameJoinS2CPacket gameJoinS2CPacket, CallbackInfo ci) {
      this.worldAlreadyLoaded = this.world != null;
   }
}
