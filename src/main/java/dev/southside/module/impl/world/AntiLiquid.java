package dev.southside.module.impl.world;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.events.ClientWorldEvent;
import dev.southside.event.events.DisableRotationEvent;
import dev.southside.event.events.MinecraftClientEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.player.AutoBucket;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.ValuePair;
import dev.southside.utils.SlotIndexMapper;
import dev.southside.utils.NumberValue;
import dev.southside.utils.RotationManager;
import dev.southside.utils.BlockLookUtils;
import dev.southside.utils.Rotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AntiLiquid extends Module {
   private static final int DEFAULT_INT = 15;
   private static final Direction[] DIRECTION_ARRAY = new Direction[]{
      Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP
   };
   @SerializedName("range")
   public final NumberValue range = new NumberValue("Range", 4.0, 3.0, 5.0, 0.5);
   @SerializedName("water")
   public final BooleanValue water = new BooleanValue("Water", true);
   @SerializedName("lava")
   public final BooleanValue lava = new BooleanValue("Lava", true);
   @SerializedName("cooldowns")
   private final Map<BlockPos, Integer> cooldowns = new HashMap<>();
   @SerializedName("state")
   private AntiLiquid.Mode state = AntiLiquid.Mode.SCAN;
   @SerializedName("liquidSource")
   private BlockPos liquidSource;
   @SerializedName("supportBlock")
   private BlockPos supportBlock;
   @SerializedName("supportFace")
   private Direction supportFace;
   @SerializedName("delayTicks")
   private int delayTicks;
   @SerializedName("previousSlot")
   private int previousSlot = -1;
   @SerializedName("placePending")
   private boolean placePending;

   public AntiLiquid() {
      super(Category.World, "AntiLiquid", "Automatically places blocks on liquid sources in view.");
   }

   @Override
   public int getHeightAlt() {
      return 2;
   }

   @Override
   public void onEnable() {
      this.cooldowns.clear();
      this.performActionAlt();
   }

   @Override
   public void onDisable() {
      if (this.previousSlot != -1 && Module.mc.player != null) {
         Module.mc.player.getInventory().selectedSlot = this.previousSlot;
      }

      this.performActionAlt();
   }

   @EventTarget
   public void onClientWorld(ClientWorldEvent clientWorldEvent) {
      this.cooldowns.clear();
      this.performActionAlt();
   }

   @Override
   public void resetToDefaults() {
      this.performActionAlt();
   }

   private void performActionAlt() {
      this.state = AntiLiquid.Mode.SCAN;
      this.liquidSource = null;
      this.supportBlock = null;
      this.supportFace = null;
      this.delayTicks = 0;
      this.previousSlot = -1;
      this.placePending = false;
   }

   @EventTarget
   public void onDisableRotation(DisableRotationEvent disableRotationEvent) {
      if (Module.mc.player != null && Module.mc.world != null && Module.mc.interactionManager != null) {
         switch (this.state) {
            case SCAN:
               this.performActionCustom();
               break;
            case DELAY:
               if (--this.delayTicks <= 0) {
                  this.state = AntiLiquid.Mode.ROTATE;
               }
               break;
            case ROTATE:
               this.performActionImpl();
            case PLACE:
            default:
               break;
            case RESTORE:
               if (this.previousSlot != -1) {
                  Module.mc.player.getInventory().selectedSlot = this.previousSlot;
                  this.previousSlot = -1;
               }

               this.state = AntiLiquid.Mode.SCAN;
         }
      }
   }

   private int computeInt() {
      for (int i = 0; i < 9; i++) {
         ItemStack stack = Module.mc.player.getInventory().getStack(i);
         if (!stack.isEmpty() && stack.getItem() instanceof BlockItem && SlotIndexMapper.isValid(stack.getItem())) {
            return i;
         }
      }

      return -1;
   }

   private boolean isValidBlock(BlockPos pos) {
      for (Direction direction : DIRECTION_ARRAY) {
         BlockPos pos2 = pos.offset(direction);
         BlockState blockState = Module.mc.world.getBlockState(pos2);
         if (!blockState.isAir() && !blockState.isLiquid() && !blockState.isReplaceable()) {
            this.supportBlock = pos2;
            this.supportFace = direction.getOpposite();
            return true;
         }
      }

      return false;
   }

   private void performActionImpl() {
      if (this.liquidSource != null && this.supportBlock != null) {
         int n = this.computeInt();
         if (n == -1) {
            this.state = AntiLiquid.Mode.SCAN;
         } else {
            Rotation rotationToBlockFace = RotationManager.getRotationToBlockFace(this.supportBlock, this.supportFace);
            if (rotationToBlockFace == null) {
               this.cooldowns.put(this.liquidSource, 15);
               this.state = AntiLiquid.Mode.SCAN;
            } else {
               this.previousSlot = Module.mc.player.getInventory().selectedSlot;
               Module.mc.player.getInventory().selectedSlot = n;
               RotationManager.rotationRequests.add(new ValuePair<>(rotationToBlockFace, this.getHeightAlt()));
               this.placePending = true;
               this.state = AntiLiquid.Mode.PLACE;
            }
         }
      } else {
         this.state = AntiLiquid.Mode.SCAN;
      }
   }

   @EventTarget
   public void onMinecraftClient(MinecraftClientEvent minecraftClientEvent) {
      if (Module.mc.player != null
         && Module.mc.world != null
         && Module.mc.interactionManager != null
         && this.state == AntiLiquid.Mode.PLACE
         && this.placePending) {
         this.placePending = false;
         this.performActionSafe();
         Rotation currentRotation = RotationManager.getCurrentRotation();
         if (currentRotation == null) {
            this.state = AntiLiquid.Mode.RESTORE;
         } else if (!BlockLookUtils.isValid(currentRotation, this.supportBlock, this.supportFace, false, BlockLookUtils::isValidBlock)) {
            this.performActionDirect();
         } else {
            BlockHitResult hitResult = BlockLookUtils.raycast(currentRotation.yaw, currentRotation.pitch, BlockLookUtils::isValidBlock);
            if (hitResult == null) {
               this.performActionDirect();
            } else {
               ActionResult actionResult = Module.mc.interactionManager.interactBlock(Module.mc.player, Hand.MAIN_HAND, hitResult);
               if (actionResult.isAccepted()) {
                  Module.mc.player.swingHand(Hand.MAIN_HAND);
               }

               this.performActionDirect();
            }
         }
      }
   }

   private void performActionDirect() {
      this.cooldowns.put(this.liquidSource, 15);
      this.state = AntiLiquid.Mode.RESTORE;
   }

   private static boolean isValid(Entry<BlockPos, Integer> entry) {
      int n = entry.setValue((Integer)entry.getValue() - 1);
      return n <= 0;
   }

   private void performActionSafe() {
      if (MinecraftClient.getInstance().player != null) {
         BlockLookUtils.vector = MinecraftClient.getInstance().player.getEyePos();
      }
   }

   private void performActionCustom() {
      this.cooldowns.entrySet().removeIf(AntiLiquid::isValid);
      this.performActionSafe();
      BlockPos blockPos = Module.mc.player.getBlockPos();
      int n = (int)Math.ceil(((Number)this.range.getValue()).floatValue());
      Vec3d eyePos = Module.mc.player.getEyePos();

      for (int i = -n; i <= n; i++) {
         for (int j = -n; j <= n; j++) {
            for (int k = -n; k <= n; k++) {
               BlockPos pos = blockPos.add(i, j, k);
               if (!this.cooldowns.containsKey(pos) && !(eyePos.distanceTo(Vec3d.ofCenter(pos)) > ((Number)this.range.getValue()).floatValue())) {
                  FluidState fluidState = Module.mc.world.getFluidState(pos);
                  if (fluidState.isStill()) {
                     boolean flag = Module.mc.world.getBlockState(pos).getBlock() == Blocks.WATER;
                     boolean flag2 = Module.mc.world.getBlockState(pos).getBlock() == Blocks.LAVA;
                     if ((!flag || (Boolean)this.water.getValue())
                        && (!flag2 || (Boolean)this.lava.getValue())
                        && (flag || flag2)
                        && (!flag || !this.isValidEx())
                        && !this.isValidBlockEx(pos)
                        && this.canSeeFluid(pos, eyePos)
                        && this.isValidBlock(pos)) {
                        this.liquidSource = pos;
                        this.delayTicks = 1;
                        this.state = AntiLiquid.Mode.DELAY;
                        return;
                     }
                  }
               }
            }
         }
      }
   }

   private boolean isValidBlockEx(BlockPos pos) {
      for (Direction direction : Direction.values()) {
         if (direction != Direction.UP && direction != Direction.DOWN && Module.mc.world.getFluidState(pos.offset(direction)).isStill()) {
            return true;
         }
      }

      return false;
   }

   private boolean isValidEx() {
      AutoBucket autoBucket = Southside.INSTANCE.getModuleManager().getModule(AutoBucket.class);
      return autoBucket.isEnabled() && autoBucket.isValidEx();
   }

   private boolean canSeeFluid(BlockPos pos, Vec3d vec) {
      Vec3d vec2 = Vec3d.ofCenter(pos);
      Rotation rotationToPos = RotationManager.getRotationToPos(vec2);
      double dist = vec.squaredDistanceTo(vec2);
      BlockHitResult hitResult = BlockLookUtils.raycast(rotationToPos.yaw, rotationToPos.pitch, BlockLookUtils::isValidBlock);
      return hitResult == null || vec.squaredDistanceTo(hitResult.getPos()) >= dist;
   }

   private enum Mode {
      SCAN,
      DELAY,
      ROTATE,
      PLACE,
      RESTORE;


      // $VF: synthetic method
      private static AntiLiquid.Mode[] createValues() {
         return new AntiLiquid.Mode[]{SCAN, DELAY, ROTATE, PLACE, RESTORE};
      }


   }
}
