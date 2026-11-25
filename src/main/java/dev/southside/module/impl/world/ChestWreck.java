package dev.southside.module.impl.world;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.events.DirectionEvent;
import dev.southside.event.events.DisableRotationEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.ModuleManager;
import dev.southside.module.impl.combat.KillAura;
import dev.southside.module.impl.movement.StrafeFix;
import dev.southside.module.impl.player.Blink;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.ValuePair;
import dev.southside.utils.RaytraceUtils;
import dev.southside.utils.SlotIndexMapper;
import dev.southside.utils.MillisTimer;
import dev.southside.utils.NumberValue;
import dev.southside.utils.ChestTracker;
import dev.southside.utils.RotationManager;
import dev.southside.utils.Rotation;
import dev.southside.utils.math.RandomUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.BlockEventS2CPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.EmptyBlockView;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public class ChestWreck extends Module {
   private static final List<Block> invalidBlocks = Arrays.asList(
      Blocks.ENCHANTING_TABLE,
      Blocks.CHEST,
      Blocks.ENDER_CHEST,
      Blocks.TRAPPED_CHEST,
      Blocks.TORCH,
      Blocks.STONE_PRESSURE_PLATE,
      Blocks.BAMBOO_PRESSURE_PLATE,
      Blocks.TNT,
      Blocks.REDSTONE_TORCH
   );
   @SerializedName("range")
   public final NumberValue range = new NumberValue("Range", 4.0, 1.0, 8.0, 0.1);
   @SerializedName("minDelay")
   public final NumberValue minDelay = new NumberValue("Min Delay", 50, 0, 1000, 10);
   @SerializedName("maxDelay")
   public final NumberValue maxDelay = new NumberValue("Max Delay", 150, 0, 1000, 10);
   @SerializedName("blockAllOpened")
   public final BooleanValue blockAllOpened = new BooleanValue("Block All Opened", false);
   @SerializedName("delayTimer")
   private final MillisTimer delayTimer = new MillisTimer();
   @SerializedName("observedOpenedChests")
   private final List<BlockPos> observedOpenedChests = new ArrayList<>();
   @SerializedName("placementSlot")
   private ChestWreck.Entry placementSlot;
   @SerializedName("previousSneakState")
   private boolean previousSneakState;
   @SerializedName("placingPending")
   private boolean placingPending;
   @SerializedName("pendingChest")
   private BlockPos pendingChest;
   @SerializedName("previousSlot")
   private int previousSlot = -1;
   @SerializedName("nextDelay")
   private long nextDelay;

   public ChestWreck() {
      super(Category.World, "ChestWreck", "esu");
   }

   @Override
   public void onEnable() {
      this.placingPending = false;
      this.previousSneakState = false;
      this.observedOpenedChests.clear();
      this.pendingChest = null;
      this.previousSlot = -1;
      this.placementSlot = null;
      this.performActionImpl();
   }

   @EventTarget
   public void onDisableRotation(DisableRotationEvent disableRotationEvent) {
      if (Module.mc.currentScreen != null) {
         this.delayTimer.reset();
      } else if (this.delayTimer.hasElapsed(this.nextDelay) && Module.mc.player != null && Module.mc.world != null && Module.mc.interactionManager != null
         )
       {
         ModuleManager moduleManager = Southside.INSTANCE.getModuleManager();
         if ((KillAura.target == null || !moduleManager.getModule(KillAura.class).isEnabled())
            && !Module.mc.player.isUsingItem()
            && !moduleManager.getModule(Scaffold.class).isEnabled()
            && !moduleManager.getModule(Blink.class).isEnabled()
            && Module.mc.player.hurtTime == 0) {
            if (this.placingPending && Module.mc.player.lastSneaking) {
               this.performActionAlt();
            } else {
               BlockPos pos = this.findBlockPos();
               if (pos == null) {
                  this.delayTimer.reset();
               } else {
                  this.updateBlock(pos);
               }
            }
         } else {
            this.delayTimer.reset();
         }
      }
   }

   private void performActionAlt() {
      if (!(this.pendingChest.getY() + 1 > Module.mc.player.getY() + Module.mc.player.getEyeHeight(Module.mc.player.getPose()))) {
         BlockHitResult hitResult = new BlockHitResult(Scaffold.computePosition(this.pendingChest, Direction.UP), Direction.UP, this.pendingChest, false);
         ActionResult actionResult = Module.mc.interactionManager.interactBlock(Module.mc.player, this.placementSlot.getHand(), hitResult);
         if (actionResult == ActionResult.SUCCESS) {
            Module.mc.player.swingHand(this.placementSlot.getHand());
         }

         if (this.placementSlot.getHand() != Hand.OFF_HAND) {
            Module.mc.player.getInventory().selectedSlot = this.previousSlot;
         }
      }

      this.delayTimer.reset();
      this.performActionImpl();
      Module.mc.options.sneakKey.setPressed(this.previousSneakState);
      Module.mc.player.setSneaking(this.previousSneakState);
      this.placingPending = false;
   }

   private void updateBlock(BlockPos pos) {
      this.previousSlot = Module.mc.player.getInventory().selectedSlot;
      this.placementSlot = null;
      this.placementSlot = this.getEntry();
      if (this.placementSlot != null && this.placementSlot.isValid()) {
         if (pos.getY() + 1 < Module.mc.player.getY() + Module.mc.player.getEyeHeight(Module.mc.player.getPose())) {
            Vec3d vec = this.computePosition(pos);
            if (vec != null) {
               Rotation rotationToPos = RotationManager.getRotationToPos(vec);
               BlockHitResult hitResult = this.raycast(rotationToPos);
               if (hitResult != null && hitResult.getBlockPos().equals(pos)) {
                  RotationManager.rotationRequests.add(new ValuePair<>(rotationToPos, 0));
                  RotationManager.moveFixMode = Southside.INSTANCE.getModuleManager().getModule(StrafeFix.class).getMoveFixMode();
                  if (this.placementSlot.getHand() != Hand.OFF_HAND) {
                     Module.mc.player.getInventory().selectedSlot = this.placementSlot.getInventorySlot();
                  }

                  this.pendingChest = pos;
                  this.placingPending = true;
                  this.previousSneakState = Module.mc.options.sneakKey.isPressed();
                  Module.mc.options.sneakKey.setPressed(true);
                  Module.mc.player.setSneaking(true);
               }
            }
         }
      }
   }

   private ChestWreck.Entry getEntry() {
      ItemStack offHandStack = Module.mc.player.getOffHandStack();
      if (offHandStack.getItem() instanceof BlockItem && isValid(Module.mc.player.getOffHandStack().getItem())) {
         return new ChestWreck.Entry(45, Hand.OFF_HAND);
      }

      ItemStack mainHandStack = Module.mc.player.getMainHandStack();
      if (mainHandStack.getItem() instanceof BlockItem && isValid(Module.mc.player.getMainHandStack().getItem())) {
         return new ChestWreck.Entry(Module.mc.player.getInventory().selectedSlot, Hand.MAIN_HAND);
      }

      ChestWreck.Entry entry = null;

      for (int i = 0; i < 9; i++) {
         ItemStack stack = Module.mc.player.getInventory().getStack(i);
         if (stack.getItem() instanceof BlockItem && isValid(stack.getItem())) {
            entry = new ChestWreck.Entry(i, Hand.MAIN_HAND);
         }
      }

      return entry;
   }

   private Vec3d computePosition(BlockPos pos) {
      Vec3d vec = null;
      double max_value = Double.MAX_VALUE;

      for (double i = 0.1; i <= 0.9; i += 0.1) {
         for (double j = 0.1; j <= 0.9; j += 0.1) {
            Vec3d vec2 = new Vec3d(pos.getX() + i, pos.getY() + 1, pos.getZ() + j);
            double dist = Module.mc.player.getCameraPosVec(1.0F).squaredDistanceTo(vec2);
            if (dist < max_value) {
               vec = vec2;
               max_value = dist;
            }
         }
      }

      return vec;
   }

   private BlockHitResult raycast(Rotation rotation) {
      Entity cameraEntity = RaytraceUtils.mc.cameraEntity;
      if (cameraEntity == null) {
         return null;
      }

      Vec3d cameraPosVec = RaytraceUtils.mc.player.getCameraPosVec(1.0F);
      Vec3d vec = RaytraceUtils.computePosition(rotation.pitch, rotation.yaw);
      Vec3d vec2 = cameraPosVec.add(vec.x * 4.5, vec.y * 4.5, vec.z * 4.5);
      return RaytraceUtils.mc.world.raycast(new RaycastContext(cameraPosVec, vec2, ShapeType.OUTLINE, FluidHandling.NONE, cameraEntity));
   }

   private BlockPos findBlockPos() {
      if (Module.mc.player != null && Module.mc.world != null) {
         BlockPos blockPos = Module.mc.player.getBlockPos();
         Vec3d pos = Module.mc.player.getPos();
         int n = (int)Math.ceil(((Number)this.range.getValue()).floatValue());
         BlockPos pos2 = null;
         double max_value = Double.MAX_VALUE;
         ChestTracker chestTracker = Southside.INSTANCE.getChestTracker();

         for (int i = -n; i <= n; i++) {
            for (int j = -n; j <= n; j++) {
               for (int k = -n; k <= n; k++) {
                  BlockPos pos3 = blockPos.add(i, j, k);
                  double dist = pos.distanceTo(Vec3d.ofCenter(pos3));
                  if (!(dist > ((Number)this.range.getValue()).floatValue())
                     && (this.blockAllOpened.getValue() ? chestTracker.chestPositions.contains(pos3) : chestTracker.openedChestHistory.contains(pos3))
                     && this.observedOpenedChests.contains(pos3)
                     && this.isValidBlockAlt(pos3)
                     && !isValidBlockEx(pos3)
                     && this.isValidBlock(pos3.up())
                     && dist < max_value) {
                     pos2 = pos3;
                     max_value = dist;
                  }
               }
            }
         }

         return pos2;
      } else {
         return null;
      }
   }

   private boolean isValidBlock(BlockPos pos) {
      return Module.mc.world != null && Module.mc.world.isSpaceEmpty(Module.mc.player, new Box(pos));
   }

   public static boolean isValidBlockEx(BlockPos pos) {
      return Module.mc.world != null && isValidBlock(Module.mc.world.getBlockState(pos.up()).getBlock());
   }

   @EventTarget
   public void onDirection(DirectionEvent directionEvent) {
      if (Module.mc.world != null && directionEvent.getPacket() instanceof BlockEventS2CPacket blockEventS2CPacket) {
         BlockState blockState = Module.mc.world.getBlockState(blockEventS2CPacket.getPos());
         if (blockState.getBlock() instanceof ChestBlock && blockEventS2CPacket.getType() == 1 && blockEventS2CPacket.getData() == 1) {
            this.observedOpenedChests.add(blockEventS2CPacket.getPos());
         }
      }
   }

   private boolean isValidBlockAlt(BlockPos pos) {
      if (Module.mc.world == null) {
         return false;
      }

      BlockState blockState = Module.mc.world.getBlockState(pos);
      return blockState.getBlock() instanceof ChestBlock || blockState.getBlock() instanceof EnderChestBlock || blockState.getBlock() instanceof ShulkerBoxBlock;
   }

   private static boolean isValidBlock(Block block) {
      return !invalidBlocks.contains(block) && SlotIndexMapper.isValidBlock(block);
   }

   private static boolean isValid(Item item) {
      return item instanceof BlockItem blockItem && !invalidBlocks.contains(blockItem.getBlock()) && SlotIndexMapper.isValid(item);
   }

   private void performActionImpl() {
      int n = ((Number)this.minDelay.getValue()).intValue();
      int n2 = ((Number)this.maxDelay.getValue()).intValue();
      if (n > n2) {
         this.nextDelay = n;
      } else {
         int n3 = n2 - n + 1;
         int n4 = n3 <= 1 ? 0 : RandomUtils.RAND.nextInt(1, n3);
         this.nextDelay = n + n4;
      }
   }

   private record Entry(int inventorySlot, Hand hand) {
      private static boolean isValidBlock(Block block) {
         BlockState defaultState = block.getDefaultState();
         VoxelShape outlineShape = defaultState.getOutlineShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN);
         return Block.isFaceFullSquare(outlineShape, Direction.DOWN);
      }

      public boolean isValid() {
         if (this.hand == Hand.OFF_HAND) {
            ItemStack offHandStack = Module.mc.player.getOffHandStack();
            if (!offHandStack.isEmpty() && offHandStack.getItem() instanceof BlockItem) {
               return isValidBlock(Block.getBlockFromItem(offHandStack.getItem()));
            }
         }

         ItemStack stack = Module.mc.player.getInventory().getStack(this.inventorySlot);
         return !stack.isEmpty() && stack.getItem() instanceof BlockItem && isValidBlock(Block.getBlockFromItem(stack.getItem()));
      }

      public int getInventorySlot() {
         return this.inventorySlot;
      }

      public Hand getHand() {
         return this.hand;
      }
   }
}
