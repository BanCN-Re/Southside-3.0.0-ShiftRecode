package dev.southside.module.impl.combat;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.StateEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.ValuePair;
import dev.southside.utils.EntityTargetUtils;
import dev.southside.utils.NumberValue;
import dev.southside.utils.ModeValue;
import dev.southside.utils.RotationManager;
import dev.southside.utils.PacketBlockBreaker;
import dev.southside.utils.Rotation;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public class FeetCity extends Module {
   @SerializedName("range")
   public final NumberValue range = new NumberValue("Range", 4.0, 2.5, 6.0, 0.1);
   @SerializedName("mode")
   public final ModeValue mode = new ModeValue("Mode", "Legit", "Legit", "ThroughWall");
   @SerializedName("autoTool")
   public final BooleanValue autoTool = new BooleanValue("Auto Tool", true);
   @SerializedName("swing")
   public final BooleanValue swing = new BooleanValue("Swing", true);
   @SerializedName("extraDelayMs")
   public final NumberValue extraDelayMs = new NumberValue("Extra Delay (ms)", 0.0, 0.0, 500.0, 5.0);
   @SerializedName("previousSlot")
   private int previousSlot = -1;
   @SerializedName("blockBreaker")
   private final PacketBlockBreaker blockBreaker = new PacketBlockBreaker();
   private static final long DEFAULT_LONG_VALUE = -4341282359304257527L;

   public FeetCity() {
      super(Category.Combat, "FeetCity", "挖掘敌人脚下的方块");
   }

   @Override
   public void onDisable() {
      this.performActionAlt();
      super.onDisable();
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      if (stateEvent.getState() != Stage.POST
         && MinecraftClient.getInstance() != null
         && MinecraftClient.getInstance().player != null
         && MinecraftClient.getInstance().world != null) {
         this.blockBreaker.performActionEx();
         FeetCity.Entry entry = this.getEntry();
         if (entry == null) {
            this.performActionAlt();
         } else if (!this.mode.options[this.mode.getValue()].equalsIgnoreCase("ThroughWall") && !this.isBlockVisible(entry.getBlockPos())) {
            this.performActionAlt();
         } else if (!this.isValidBlockEx(entry.getBlockPos())) {
            this.performActionAlt();
         } else {
            this.updateBlock(entry.getBlockPos());
         }
      }
   }

   public FeetCity.Entry getEntry() {
      if (Module.mc.world != null && Module.mc.player != null) {
         PlayerEntity player = null;
         BlockPos pos = null;
         double max_value = Double.MAX_VALUE;

         for (PlayerEntity player2 : Module.mc.world.getPlayers()) {
            if (player2 != Module.mc.player && player2 != null && player2.isAlive() && !player2.isSpectator() && EntityTargetUtils.isLivingEntity(player2)) {
               BlockPos pos2 = this.findBlockPos(player2);
               if (pos2 != null) {
                  double dist = Module.mc.player.getPos().squaredDistanceTo(Vec3d.ofCenter(pos2));
                  double d = ((Number)this.range.getValue()).doubleValue();
                  if (!(dist > d * d) && (player == null || dist < max_value)) {
                     player = player2;
                     pos = pos2;
                     max_value = dist;
                  }
               }
            }
         }

         return player != null && pos != null ? new FeetCity.Entry(player, pos) : null;
      } else {
         return null;
      }
   }

   public BlockPos findBlockPos(PlayerEntity player) {
      if (Module.mc.world == null) {
         return null;
      }

      BlockPos pos = BlockPos.ofFloored(player.getX(), player.getY() - 0.2, player.getZ());
      if (this.isValidBlock(pos)) {
         return pos;
      }

      BlockPos pos2 = pos.down();
      return this.isValidBlock(pos2) ? pos2 : null;
   }

   public boolean isValidBlock(BlockPos pos) {
      if (Module.mc.world == null) {
         return false;
      }

      BlockState blockState = Module.mc.world.getBlockState(pos);
      return !blockState.isAir() && blockState.getHardness(Module.mc.world, pos) >= 0.0F;
   }

   public boolean isValidBlockEx(BlockPos pos) {
      if (Module.mc.player == null) {
         return false;
      }

      double dist = Module.mc.player.getPos().squaredDistanceTo(Vec3d.ofCenter(pos));
      double d = ((Number)this.range.getValue()).doubleValue();
      return dist <= d * d;
   }

   public boolean isBlockVisible(BlockPos pos) {
      if (Module.mc.player != null && Module.mc.world != null) {
         Vec3d eyePos = Module.mc.player.getEyePos();
         BlockHitResult hitResult = Module.mc
            .world
            .raycast(new RaycastContext(eyePos, Vec3d.ofCenter(pos), ShapeType.COLLIDER, FluidHandling.NONE, Module.mc.player));
         return hitResult.getType() == Type.MISS || hitResult.getType() == Type.BLOCK && hitResult.getBlockPos().equals(pos);
      } else {
         return false;
      }
   }

   public Direction getDirection(BlockPos pos) {
      if (Module.mc.player == null) {
         return null;
      } else if (Module.mc.player.raycast(5.0, Module.mc.getRenderTickCounter().getTickDelta(false), false) instanceof BlockHitResult hitResult
         && hitResult.getBlockPos().equals(pos)) {
         return hitResult.getSide();
      } else {
         Vec3d eyePos = Module.mc.player.getEyePos();
         Vec3d delta = Vec3d.ofCenter(pos).subtract(eyePos);
         return Direction.getFacing(delta.x, delta.y, delta.z).getOpposite();
      }
   }

   public void updateBlock(BlockPos pos) {
      if (Module.mc.world != null && Module.mc.interactionManager != null && Module.mc.player != null) {
         BlockState blockState = Module.mc.world.getBlockState(pos);
         if (!blockState.isAir() && !(blockState.getHardness(Module.mc.world, pos) < 0.0F)) {
            this.performAction(blockState);
            Direction direction = this.getDirection(pos);
            if (direction == null) {
               this.performActionAlt();
            } else {
               Rotation rotation = RotationManager.computeRotation(
                  RotationManager.mc.player.getEyePos(), Vec3d.ofCenter(pos).add(Vec3d.of(direction.getVector()).multiply(0.5)), false
               );
               if (rotation != null) {
                  RotationManager.rotationRequests.add(new ValuePair<>(rotation, 1));
               }

               if (!this.blockBreaker.isValidBlock(pos)) {
                  this.blockBreaker.isValid(pos, direction, ((Number)this.extraDelayMs.getValue()).longValue(), this::TargetData);
               }

               if ((Boolean)this.swing.getValue() && Module.mc.player.handSwingProgress <= 0.0F) {
                  Module.mc.player.swingHand(Hand.MAIN_HAND);
               }
            }
         } else {
            this.performActionAlt();
         }
      }
   }

   public void performAction(BlockState state) {
      if ((Boolean)this.autoTool.getValue() && Module.mc.player != null) {
         int n = -1;
         float f = 1.0F;

         for (int i = 0; i < (int)DEFAULT_LONG_VALUE; i++) {
            ItemStack stack = Module.mc.player.getInventory().getStack(i);
            if (!stack.isEmpty()) {
               float miningSpeedMultiplier = stack.getMiningSpeedMultiplier(state);
               if (miningSpeedMultiplier > f) {
                  f = miningSpeedMultiplier;
                  n = i;
               }
            }
         }

         if (n != -1 && Module.mc.player.getInventory().selectedSlot != n) {
            if (this.previousSlot == -1) {
               this.previousSlot = Module.mc.player.getInventory().selectedSlot;
            }

            Module.mc.player.getInventory().selectedSlot = n;
         }
      }
   }

   private void performActionAlt() {
      this.blockBreaker.performAction();
      this.performActionImpl();
   }

   public void TargetData(BlockPos pos, BlockState state) {
      this.performActionImpl();
   }

   public void performActionImpl() {
      if (Module.mc.player != null && this.previousSlot != -1) {
         Module.mc.player.getInventory().selectedSlot = this.previousSlot;
         this.previousSlot = -1;
      }
   }

   public record Entry(PlayerEntity player, BlockPos blockPos) {
      public PlayerEntity getPlayer() {
         return this.player;
      }

      public BlockPos getBlockPos() {
         return this.blockPos;
      }
   }
}
