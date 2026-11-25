package dev.southside.module.impl.player;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.events.DisableRotationEvent;
import dev.southside.event.events.ForwardEvent;
import dev.southside.event.events.MinecraftClientEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.movement.StrafeFix;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.MillisTimer;
import dev.southside.utils.NumberValue;
import dev.southside.utils.RotationManager;
import dev.southside.utils.BlockLookUtils;
import dev.southside.utils.Rotation;
import dev.southside.utils.rotation.MoveFixMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class BlockIn extends Module {
   private static final double DEFAULT_DOUBLE = 0.15;
   private static final int DEFAULT_INT = 6;
   private static final List<Direction> CARDINAL = List.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);
   private static final List<Direction> columnTargets = List.of(Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);
   @SerializedName("delay")
   public final NumberValue delay = new NumberValue("Delay", 50.0, 0.0, 500.0, 10.0);
   @SerializedName("autoCenter")
   public final BooleanValue autoCenter = new BooleanValue("AutoCenter", false);
   @SerializedName("placeTimer")
   private final MillisTimer placeTimer = new MillisTimer();
   @SerializedName("wallPositions")
   private final List<BlockPos> wallPositions = new ArrayList<>();
   @SerializedName("state")
   private BlockIn.Mode state = BlockIn.Mode.DONE;
   @SerializedName("origin")
   private BlockPos origin;
   @SerializedName("roofPosition")
   private BlockPos roofPosition;
   @SerializedName("roofSupportDirection")
   private Direction roofSupportDirection;
   @SerializedName("placement")
   private BlockIn.Entry placement;
   @SerializedName("placementAttempts")
   private int placementAttempts;
   @SerializedName("jumpStarted")
   private boolean jumpStarted;
   @SerializedName("jumpKeyHeld")
   private boolean jumpKeyHeld;
   @SerializedName("roofAlreadyPresent")
   private boolean roofAlreadyPresent;
   private static final long n = 150L;

   public BlockIn() {
      super(Category.Player, "BlockIn");
   }

   @Override
   public void onEnable() {
      super.onEnable();
      if (mc2.player != null) {
         this.origin = mc2.player.getBlockPos();
         this.roofPosition = this.origin.up(2);
         this.performActionAlt();
         this.roofSupportDirection = null;
         this.placement = null;
         this.placementAttempts = 0;
         this.jumpStarted = false;
         this.placeTimer.reset();
         this.roofAlreadyPresent = mc2.world != null && !this.isValidBlock(this.roofPosition);
         this.state = this.autoCenter.getValue() ? BlockIn.Mode.CENTER : BlockIn.Mode.BUILD_WALLS;
      }
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.state = BlockIn.Mode.DONE;
      this.origin = null;
      this.roofPosition = null;
      this.roofSupportDirection = null;
      this.wallPositions.clear();
      this.placement = null;
      this.placementAttempts = 0;
      this.jumpStarted = false;
      this.performActionCustom();
      this.roofAlreadyPresent = false;
      this.placeTimer.reset();
   }

   @EventTarget
   public void onDisableRotation(DisableRotationEvent disableRotationEvent) {
      if (mc2.player != null && mc2.world != null && mc2.currentScreen == null && this.state != BlockIn.Mode.DONE) {
         if (this.origin == null) {
            this.origin = mc2.player.getBlockPos();
            this.roofPosition = this.origin.up(2);
         }

         switch (this.state) {
            case CENTER:
               this.performActionImpl();
               break;
            case BUILD_WALLS:
               this.performActionDirect();
               break;
            case JUMP:
               this.performActionSafe();
               break;
            case RAISE_SUPPORT:
               this.performActionWithOffset();
               break;
            case PLACE_ROOF:
               this.performActionByIndex();
            case DONE:
         }
      }
   }

   private void performActionAlt() {
      this.wallPositions.clear();
      Direction horizontalFacing = mc2.player.getHorizontalFacing();

      for (int i = 0; i < 2; i++) {
         this.wallPositions.add(this.origin.offset(horizontalFacing).up(i));

         for (Direction direction : CARDINAL) {
            if (direction != horizontalFacing) {
               this.wallPositions.add(this.origin.offset(direction).up(i));
            }
         }
      }
   }

   private double computeDistance() {
      double d = this.origin.getX() + 0.5 - mc2.player.getX();
      double d2 = this.origin.getZ() + 0.5 - mc2.player.getZ();
      return Math.sqrt(d * d + d2 * d2);
   }

   private void performActionImpl() {
      if (this.computeDistance() <= 0.15) {
         this.performAction(BlockIn.Mode.BUILD_WALLS);
      } else {
         Vec3d eyePos = mc2.player.getEyePos();
         double d = this.origin.getX() + 0.5 - eyePos.x;
         double d2 = this.origin.getZ() + 0.5 - eyePos.z;
         float f = (float)(Math.atan2(d2, d) * 180.0 / Math.PI) - 90.0F;
         RotationManager.requestRotationWithMoveFix(new Rotation(f, 0.0F), 0, MoveFixMode.Strict);
      }
   }

   @EventTarget
   public void onForward(ForwardEvent forwardEvent) {
      if (mc2.player != null && this.state == BlockIn.Mode.CENTER) {
         forwardEvent.setForward(false);
         forwardEvent.setBackward(false);
         forwardEvent.setLeft(false);
         forwardEvent.setRight(false);
         forwardEvent.setJump(false);
         forwardEvent.setSneak(false);
         forwardEvent.setSprint(false);
         if (this.computeDistance() > 0.15) {
            forwardEvent.setForward(true);
         }
      }
   }

   private void performActionDirect() {
      if (this.isValidEx()) {
         while (!this.wallPositions.isEmpty()) {
            BlockPos first = this.wallPositions.getFirst();
            BlockIn.Entry entry = this.isValidBlock(first) ? this.buildPlan(first, null, null) : null;
            if (entry != null) {
               this.performAction(entry);
               return;
            }

            this.wallPositions.removeFirst();
         }

         this.performAction(this.roofAlreadyPresent ? BlockIn.Mode.DONE : BlockIn.Mode.JUMP);
      }
   }

   private void performActionSafe() {
      if (!this.jumpStarted && mc2.player.isOnGround()) {
         mc2.options.jumpKey.setPressed(true);
         this.jumpStarted = true;
         this.jumpKeyHeld = true;
         this.placeTimer.reset();
      } else {
         if (this.jumpKeyHeld && (!mc2.player.isOnGround() || this.placeTimer.hasElapsed(n))) {
            this.performActionCustom();
         }

         if (this.jumpStarted && !mc2.player.isOnGround()) {
            this.performAction(BlockIn.Mode.RAISE_SUPPORT);
         }
      }
   }

   private void performActionCustom() {
      if (this.jumpKeyHeld) {
         mc2.options.jumpKey.setPressed(false);
         this.jumpKeyHeld = false;
      }
   }

   private void performActionWithOffset() {
      if (this.isValidEx() && this.isValidAlt()) {
         BlockPos pos = this.origin.offset(this.roofSupportDirection).up(2);
         if (!this.isValidBlock(pos)) {
            this.performAction(BlockIn.Mode.PLACE_ROOF);
         } else {
            BlockIn.Entry entry = this.buildPlan(pos, this.origin.offset(this.roofSupportDirection).up(), Direction.UP);
            if (entry == null) {
               this.performAction(BlockIn.Mode.PLACE_ROOF);
            } else {
               this.performAction(entry);
            }
         }
      }
   }

   private void performActionByIndex() {
      if (this.roofPosition == null) {
         this.roofPosition = this.origin.up(2);
      }

      if (!this.isValidBlock(this.roofPosition)) {
         this.performActionV2();
      } else if (mc2.player.isOnGround() && this.jumpStarted && this.isValidEx() && this.isValidAlt()) {
         BlockIn.Entry entry = this.buildPlan(this.roofPosition, this.origin.offset(this.roofSupportDirection).up(2), this.roofSupportDirection.getOpposite());
         if (entry == null) {
            this.performActionV2();
         } else {
            this.performAction(entry);
         }
      }
   }

   private boolean isValidEx() {
      if (this.computeInt() == -1) {
         this.performActionV2();
         return false;
      } else if (this.placement != null) {
         this.performActionInternal();
         return false;
      } else {
         return this.placeTimer.hasElapsed(((Number)this.delay.getValue()).longValue());
      }
   }

   private boolean isValidAlt() {
      if (this.roofSupportDirection == null) {
         this.roofSupportDirection = this.getDirection();
      }

      if (this.roofSupportDirection == null) {
         this.performActionV2();
         return false;
      } else {
         return true;
      }
   }

   private Direction getDirection() {
      Direction horizontalFacing = mc2.player.getHorizontalFacing();

      for (int i = 2; i >= 1; i--) {
         if (this.isValid(horizontalFacing, i)) {
            return horizontalFacing;
         }

         for (Direction direction : CARDINAL) {
            if (direction != horizontalFacing && this.isValid(direction, i)) {
               return direction;
            }
         }
      }

      return horizontalFacing;
   }

   private boolean isValid(Direction direction, int n2) {
      BlockPos pos = this.origin.offset(direction);
      return !mc2.world.getBlockState(pos).isAir() && (n2 == 1 || !mc2.world.getBlockState(pos.up()).isAir());
   }

   private BlockIn.Entry buildPlan(BlockPos pos, BlockPos pos2, Direction direction) {
      if (!this.isValidBlock(pos)) {
         return null;
      }

      if (pos2 != null && direction != null && this.isValidBlockEx(pos2)) {
         return new BlockIn.Entry(pos, pos2, direction, RotationManager.getRotationToBlockFace(pos2, direction));
      }

      for (Direction direction2 : columnTargets) {
         BlockPos pos3 = pos.offset(direction2.getOpposite());
         if (this.isValidBlockEx(pos3)) {
            return new BlockIn.Entry(pos, pos3, direction2, RotationManager.getRotationToBlockFace(pos3, direction2));
         }
      }

      return null;
   }

   private void performAction(BlockIn.Entry entry) {
      this.placement = entry;
      this.placementAttempts = 0;
      this.performActionInternal();
   }

   private void performActionInternal() {
      MoveFixMode moveFixMode = Southside.INSTANCE.getModuleManager().getModule(StrafeFix.class).getMoveFixMode();
      RotationManager.requestRotationWithMoveFix(this.placement.rotation, 0, moveFixMode);
   }

   @EventTarget
   public void onMinecraftClient(MinecraftClientEvent minecraftClientEvent) {
      if (this.placement != null && mc2.player != null && mc2.world != null && mc2.interactionManager != null) {
         int n2 = this.computeInt();
         if (n2 == -1) {
            this.performActionV2();
         } else {
            Rotation currentRotation = RotationManager.getCurrentRotation();
            if (currentRotation != null) {
               BlockLookUtils.vector = mc2.player.getEyePos();
               BlockHitResult hitResult = BlockLookUtils.raycast(currentRotation.yaw, currentRotation.pitch, BlockLookUtils::isValidBlock);
               if (hitResult != null && this.placement.support.equals(hitResult.getBlockPos()) && this.placement.face == hitResult.getSide()) {
                  int selectedSlot = mc2.player.getInventory().selectedSlot;
                  BlockPos target = this.placement.target;
                  mc2.player.getInventory().selectedSlot = n2;

                  boolean flag;
                  try {
                     flag = mc2.interactionManager.interactBlock(mc2.player, Hand.MAIN_HAND, hitResult).isAccepted();
                     if (flag) {
                        mc2.player.swingHand(Hand.MAIN_HAND);
                     }
                  } finally {
                     mc2.player.getInventory().selectedSlot = selectedSlot;
                  }

                  if (flag) {
                     this.updateBlock(target);
                  } else {
                     this.performActionHelper();
                  }
               } else {
                  this.performActionHelper();
               }
            }
         }
      }
   }

   private void performActionHelper() {
      if (++this.placementAttempts >= 6) {
         this.updateBlock(this.placement.target);
      }
   }

   private void updateBlock(BlockPos pos) {
      this.placement = null;
      this.placementAttempts = 0;
      this.placeTimer.reset();
      switch (this.state) {
         case BUILD_WALLS:
            this.wallPositions.remove(pos);
            if (this.wallPositions.isEmpty()) {
               this.performAction(this.roofAlreadyPresent ? BlockIn.Mode.DONE : BlockIn.Mode.JUMP);
            }
         case JUMP:
         default:
            break;
         case RAISE_SUPPORT:
            this.performAction(BlockIn.Mode.PLACE_ROOF);
            break;
         case PLACE_ROOF:
            this.performActionV2();
      }
   }

   private void performAction(BlockIn.Mode mode) {
      this.placement = null;
      this.placementAttempts = 0;
      this.placeTimer.reset();
      if (mode == BlockIn.Mode.DONE) {
         this.performActionV2();
      } else {
         this.state = mode;
      }
   }

   private void performActionV2() {
      this.setEnabled(false);
   }

   private int computeInt() {
      for (int i = 0; i < 9; i++) {
         if (isValidItem(mc2.player.getInventory().getStack(i))) {
            return i;
         }
      }

      return -1;
   }

   private static boolean isValidItem(ItemStack stack) {
      if (!stack.isEmpty() && stack.getItem() instanceof BlockItem blockItem) {
         Block block = blockItem.getBlock();
         return block != Blocks.TNT
            && block != Blocks.SAND
            && block != Blocks.GRAVEL
            && block != Blocks.WATER
            && block != Blocks.LAVA
            && !block.toString().toLowerCase(Locale.ROOT).contains("door");
      } else {
         return false;
      }
   }

   private boolean isValidBlock(BlockPos pos) {
      BlockState blockState = mc2.world.getBlockState(pos);
      return blockState.isAir() || blockState.isOf(Blocks.WATER) || blockState.isOf(Blocks.LAVA);
   }

   private boolean isValidBlockEx(BlockPos pos) {
      BlockState blockState = mc2.world.getBlockState(pos);
      return !blockState.isAir() && !blockState.isOf(Blocks.WATER) && !blockState.isOf(Blocks.LAVA);
   }

   private record Entry(BlockPos target, BlockPos support, Direction face, Rotation rotation) {

      public BlockPos getTarget() {
         return this.target;
      }

      public BlockPos getSupport() {
         return this.support;
      }

      public Direction getFace() {
         return this.face;
      }

      public Rotation getRotation() {
         return this.rotation;
      }
   }

   private enum Mode {
      CENTER,
      BUILD_WALLS,
      JUMP,
      RAISE_SUPPORT,
      PLACE_ROOF,
      DONE;

      private static final long DEFAULT_LONG = -6931854938301530106L;

      // $VF: synthetic method
      private static BlockIn.Mode[] getModeList() {
         BlockIn.Mode[] modeArray = new BlockIn.Mode[(int)DEFAULT_LONG];
         modeArray[0] = CENTER;
         modeArray[1] = BUILD_WALLS;
         modeArray[2] = JUMP;
         modeArray[3] = RAISE_SUPPORT;
         modeArray[4] = PLACE_ROOF;
         modeArray[5] = DONE;
         return modeArray;
      }


   }
}
