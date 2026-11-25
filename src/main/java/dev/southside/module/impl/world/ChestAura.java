package dev.southside.module.impl.world;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.events.ClientWorldEvent;
import dev.southside.event.events.DisableRotationEvent;
import dev.southside.event.events.EntityEvent;
import dev.southside.event.events.ForwardEvent;
import dev.southside.event.events.MinecraftClientEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.ModuleManager;
import dev.southside.module.impl.combat.KillAura;
import dev.southside.module.impl.movement.NoSlow;
import dev.southside.module.impl.player.Blink;
import dev.southside.module.impl.player.Stealer;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.ValuePair;
import dev.southside.utils.ItemScoreUtils;
import dev.southside.utils.PacketUtils;
import dev.southside.utils.MillisTimer;
import dev.southside.utils.NumberValue;
import dev.southside.utils.RotationManager;
import dev.southside.utils.BlockLookUtils;
import dev.southside.utils.Rotation;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class ChestAura extends Module {
   @SerializedName("range")
   public final NumberValue range = new NumberValue("Range", 4.0, 1.0, 8.0, 0.1);
   @SerializedName("minDelay")
   public final NumberValue minDelay = new NumberValue("Min Delay", 50, 0, 1000, 10);
   @SerializedName("maxDelay")
   public final NumberValue maxDelay = new NumberValue("Max Delay", 150, 0, 1000, 10);
   @SerializedName("openStolenChest")
   public final BooleanValue openStolenChest = new BooleanValue("OpenStolenChest", false);
   @SerializedName("rayCast")
   public final BooleanValue rayCast = new BooleanValue("RayCast", true);
   @SerializedName("enderChest")
   public final BooleanValue enderChest = new BooleanValue("Ender Chest", false);
   @SerializedName("noMove")
   public final BooleanValue noMove = new BooleanValue("NoMove", false);
   @SerializedName("delayTimer")
   private final MillisTimer delayTimer = new MillisTimer();
   @SerializedName("random")
   private final Random random = new Random();
   @SerializedName("nextDelay")
   private long nextDelay;
   @SerializedName("pendingChest")
   private BlockPos pendingChest;
   @SerializedName("attackCooldownTicks")
   private int attackCooldownTicks;
   @SerializedName("playerMoving")
   private boolean playerMoving;
   @SerializedName("lastAimRotation")
   private Rotation lastAimRotation;

   public ChestAura() {
      super(Category.World, "ChestAura");
   }

   @EventTarget
   public void onEntity(EntityEvent entityEvent) {
      this.attackCooldownTicks = 10;
   }

   @EventTarget
   public void onClientWorld(ClientWorldEvent clientWorldEvent) {
      this.playerMoving = false;
   }

   @Override
   public void onEnable() {
      this.delayTimer.reset();
      this.performActionAlt();
      this.playerMoving = false;
   }

   @Override
   public void onDisable() {
      this.playerMoving = false;
   }

   @EventTarget
   public void onDisableRotation(DisableRotationEvent disableRotationEvent) {
      if (Module.mc.player != null && Module.mc.world != null) {
         if (this.attackCooldownTicks > 0) {
            this.attackCooldownTicks--;
         } else if (this.delayTimer.hasElapsed(this.nextDelay)) {
            ModuleManager moduleManager = Southside.INSTANCE.getModuleManager();
            if ((KillAura.target == null || !moduleManager.getModule(KillAura.class).isEnabled())
               && !Module.mc.player.isUsingItem()
               && !moduleManager.getModule(Scaffold.class).isEnabled()
               && !moduleManager.getModule(Blink.class).isEnabled()
               && Module.mc.player.hurtTime == 0
               && !(Module.mc.player.fallDistance > 3.0F)
               && !NoSlow.activeValue
               && Module.mc.currentScreen == null
               && !ItemScoreUtils.isValid(Module.mc.player.getInventory())
               && (!(Boolean)this.noMove.getValue() || !this.playerMoving)) {
               BlockPos pos = this.findBlockPos();
               if (pos != null && moduleManager.getModule(Stealer.class).isEnabled() && !moduleManager.getModule(Scaffold.class).isEnabled()) {
                  this.updateBlock(pos);
                  this.pendingChest = pos;
               }
            }
         }
      }
   }

   private BlockPos findBlockPos() {
      BlockPos blockPos = Module.mc.player.getBlockPos();
      Vec3d pos = Module.mc.player.getPos();
      int n = (int)Math.ceil(((Number)this.range.getValue()).floatValue());
      BlockPos pos2 = null;
      double max_value = Double.MAX_VALUE;

      for (int i = -n; i <= n; i++) {
         for (int j = -n; j <= n; j++) {
            for (int k = -n; k <= n; k++) {
               BlockPos pos3 = blockPos.add(i, j, k);
               double dist = pos.distanceTo(Vec3d.ofCenter(pos3));
               if (!Southside.INSTANCE.getChestTracker().chestPositions.contains(pos3)
                  && this.isValidBlock(pos3)
                  && !this.isValidBlockEx(pos3)
                  && dist < max_value) {
                  pos2 = pos3;
                  max_value = dist;
               }
            }
         }
      }

      return pos2;
   }

   @EventTarget
   public void onForward(ForwardEvent forwardEvent) {
      this.playerMoving = (Boolean)this.noMove.getValue()
         && (forwardEvent.isForward() || forwardEvent.isBackward() || forwardEvent.isLeft() || forwardEvent.isRight() || forwardEvent.isJump() || forwardEvent.isSneak());
   }

   private boolean isValidBlock(BlockPos pos) {
      if (Module.mc.world == null) {
         return false;
      }

      BlockState blockState = Module.mc.world.getBlockState(pos);
      return blockState.getBlock() instanceof ChestBlock
         || blockState.getBlock() instanceof EnderChestBlock && (Boolean)this.enderChest.getValue()
         || blockState.getBlock() instanceof ShulkerBoxBlock;
   }

   private boolean isValidBlock(BlockState state) {
      return !(state.getBlock() instanceof ChestBlock) && !(state.getBlock() instanceof EnderChestBlock) && !(state.getBlock() instanceof ShulkerBoxBlock);
   }

   private boolean isValidBlockEx(BlockPos pos) {
      if (Module.mc.world == null) {
         return false;
      }

      BlockState blockState = Module.mc.world.getBlockState(pos.up());
      return !(blockState.getBlock() instanceof LeavesBlock) && ChestWreck.isValidBlockEx(pos);
   }

   private void performActionAlt() {
      int n = ((Number)this.minDelay.getValue()).intValue();
      int n2 = ((Number)this.maxDelay.getValue()).intValue();
      this.nextDelay = n > n2 ? n : n + this.random.nextInt(n2 - n + 1);
   }

   @EventTarget
   public void onMinecraftClient(MinecraftClientEvent minecraftClientEvent) {
      if (this.pendingChest != null) {
         this.performActionImpl();
         this.playerMoving = false;
         this.delayTimer.reset();
         this.performActionAlt();
         this.pendingChest = null;
      }
   }

   private void performActionImpl() {
      if (Module.mc.player != null && Module.mc.getNetworkHandler() != null) {
         ModuleManager moduleManager = Southside.INSTANCE.getModuleManager();
         if (moduleManager.getModule(Stealer.class).isEnabled() && !moduleManager.getModule(Scaffold.class).isEnabled()) {
            Rotation currentRotation = RotationManager.getCurrentRotation();
            if (currentRotation != null) {
               BlockHitResult hitResult = BlockLookUtils.raycast(currentRotation.yaw, currentRotation.pitch, this::isValidBlock);
               if (hitResult != null && (hitResult.getBlockPos().equals(this.pendingChest) || !(Boolean)this.rayCast.getValue())) {
                  PacketUtils.sendSequencedPacket(n -> new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hitResult, n));
                  Southside.INSTANCE.getChestTracker().openedChests.offer(this.pendingChest);
               }
            }
         }
      }
   }

   private void updateBlock(BlockPos pos) {
      if (Module.mc.player != null) {
         ClientPlayerEntity player = RotationManager.mc.player;
         if (pos != null && player != null) {
            Vec3d eyePos = player.getEyePos();
            Vec3d vec = RotationManager.computePosition(pos, Direction.UP);
            Vec3d delta = vec.subtract(eyePos);
            float f = (float)Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0F;
            float f2 = (float)Math.toDegrees(-Math.atan2(delta.y, Math.sqrt(delta.x * delta.x + delta.z * delta.z)));
            Rotation rotation = new Rotation(f, f2);
            BlockHitResult hitResult = BlockLookUtils.raycast(rotation.yaw, rotation.pitch, this::isValidBlock);
            if (hitResult != null && hitResult.getBlockPos().equals(pos)) {
               this.lastAimRotation = rotation;
               RotationManager.rotationRequests.add(new ValuePair<>(this.lastAimRotation, 1));
            }
         }
      }
   }
}
