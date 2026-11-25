package dev.southside.module.impl.combat;

import cc.polyfrost.oneconfig.config.core.OneColor;
import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.CameraEvent;
import dev.southside.event.events.HandEvent;
import dev.southside.event.events.StateEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.values.BooleanValue;
import dev.southside.render.ProgressBarOverlay;
import dev.southside.utils.BedTracker;
import dev.southside.utils.BlockBreakHelper;
import dev.southside.utils.ColorValue;
import dev.southside.utils.ValuePair;
import dev.southside.utils.BoxMeshRenderer;
import dev.southside.utils.NumberValue;
import dev.southside.utils.ModeValue;
import dev.southside.utils.RotationManager;
import dev.southside.utils.BlockLookUtils;
import dev.southside.utils.Rotation;
import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BedPart;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

public class BedAura extends Module {
   private static final long DEFAULT_LONG_VALUE = 250L;
   @SerializedName("range")
   public final NumberValue range = new NumberValue("Range", 4.5, 1.0, 8.0, 0.1);
   @SerializedName("miningDelay")
   public final NumberValue miningDelay = new NumberValue("Rate", 0.2, 0.05, 3.0, 0.05);
   @SerializedName("fov")
   public final NumberValue fov = new NumberValue("FOV", 360, 30, 360, 5);
   @SerializedName("breakMode")
   public final ModeValue breakMode = new ModeValue("Break Mode", "BreakNearBlock", "BreakNearBlock", "Full", "OnlyBed");
   @SerializedName("autoTool")
   public final BooleanValue autoTool = new BooleanValue("Auto Tool", true);
   @SerializedName("ignoreSelf")
   public final BooleanValue ignoreSelf = new BooleanValue("Ignore Self", false);
   @SerializedName("pauseForKillAura")
   public final BooleanValue pauseForKillAura = new BooleanValue("Pause KillAura", true);
   @SerializedName("renderBox")
   public final BooleanValue renderBox = new BooleanValue("Render Box", true);
   @SerializedName("bedSideColor")
   public final ColorValue bedSideColor = new ColorValue("Bed Side Color", true, new OneColor(80, 255, 80, 60));
   @SerializedName("bedLineColor")
   public final ColorValue bedLineColor = new ColorValue("Bed Line Color", true, new OneColor(80, 255, 80, 170));
   @SerializedName("currentBreakSideColor")
   public final ColorValue currentBreakSideColor = new ColorValue("Current Break Side Color", true, new OneColor(255, 90, 90, 90));
   @SerializedName("currentBreakLineColor")
   public final ColorValue currentBreakLineColor = new ColorValue("Current Break Line Color", true, new OneColor(255, 90, 90, 180));
   @SerializedName("pathSideColor")
   public final ColorValue pathSideColor = new ColorValue("Path Side Color", true, new OneColor(80, 220, 255, 55));
   @SerializedName("pathLineColor")
   public final ColorValue pathLineColor = new ColorValue("Path Line Color", true, new OneColor(80, 220, 255, 160));
   @SerializedName("targetBed")
   private BedAura.Entry targetBed;
   @SerializedName("currentBreakPos")
   private BlockPos currentBreakPos;
   @SerializedName("lastMinedPos")
   private BlockPos lastMinedPos;
   @SerializedName("breakProgress")
   private float breakProgress;
   @SerializedName("breaking")
   private boolean breaking;
   @SerializedName("nextTargetTime")
   private long nextTargetTime;
   @SerializedName("lastTargetSearchTime")
   private long lastTargetSearchTime;
   @SerializedName("nextMineTime")
   private long nextMineTime;
   @SerializedName("bestToolSlot")
   private int bestToolSlot;
   @SerializedName("previousToolSlot")
   private int previousToolSlot = -1;
   @SerializedName("toolSwitched")
   private boolean toolSwitched;
   @SerializedName("progressHandle")
   private ProgressBarOverlay.Inner progressHandle;
   @SerializedName("breakPath")
   private final List<BlockPos> breakPath = new ArrayList<>();

   public BedAura() {
      super(Category.Combat, "BedAura", "Breaks enemy beds");
   }

   @Override
   public void postInit() {
      super.postInit();
      this.hideIf(this.bedSideColor.getLabel(), this::isValidEx);
      this.hideIf(this.bedLineColor.getLabel(), this::isValidEx);
      this.hideIf(this.currentBreakSideColor.getLabel(), this::isValidEx);
      this.hideIf(this.currentBreakLineColor.getLabel(), this::isValidEx);
      this.hideIf(this.pathSideColor.getLabel(), this::isValidEx);
      this.hideIf(this.pathLineColor.getLabel(), this::isValidEx);
   }

   private boolean isValidEx() {
      return !(Boolean)this.renderBox.getValue();
   }

   @Override
   public void onEnable() {
      this.performActionAlt();
      this.performActionDirect();
      this.targetBed = null;
      this.lastTargetSearchTime = 0L;
      this.nextMineTime = 0L;
   }

   @Override
   public void onDisable() {
      this.performActionImpl();
      this.previousToolSlot = -1;
      this.toolSwitched = false;
      this.targetBed = null;
      super.onDisable();
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      if (stateEvent.getState() != Stage.POST && Module.mc.player != null && Module.mc.world != null && Module.mc.interactionManager != null) {
         this.performActionSafe();
         if ((Boolean)this.pauseForKillAura.getValue() && KillAura.target != null) {
            this.performActionImpl();
         } else if (this.currentBreakPos != null && !this.isValidBlockAlt(this.currentBreakPos)) {
            this.performActionImpl();
            this.targetBed = null;
         } else {
            if (this.targetBed == null) {
               long now = System.currentTimeMillis();
               if (!this.breaking && now - this.lastTargetSearchTime >= (long)(((Number)this.miningDelay.getValue()).doubleValue() * 1000.0)) {
                  this.lastTargetSearchTime = now;
                  this.targetBed = this.getEntry();
                  this.breakPath.clear();
               }

               if (this.targetBed == null) {
                  this.performActionDirect();
                  return;
               }
            } else if (!(Module.mc.world.getBlockState(this.targetBed.getFoot()).getBlock() instanceof BedBlock)) {
               this.performActionImpl();
               this.targetBed = null;
               return;
            }

            if (!this.breaking && System.currentTimeMillis() < this.nextTargetTime) {
               this.performActionDirect();
            } else {
               BlockPos pos = switch (this.breakMode.getDisplayTextEx()) {
                  case "OnlyBed" -> this.targetBed.getFoot();
                  case "Full" -> this.findBlockPosEx();
                  default -> this.findBlockPos();
               };
               if (pos == null) {
                  this.performActionImpl();
               } else {
                  this.updateBlock(pos);
               }
            }
         }
      }
   }

   private BedAura.Entry getEntry() {
      if (Module.mc.player != null && Module.mc.world != null) {
         BlockPos blockPos = Module.mc.player.getBlockPos();
         int n = (int)Math.ceil(((Number)this.range.getValue()).doubleValue());
         BlockPos pos = this.ignoreSelf.getValue() ? BedTracker.INSTANCE.getBedPosition() : null;
         ArrayList list = new ArrayList();

         for (int i = n; i >= -n; i--) {
            for (int j = -n; j <= n; j++) {
               for (int k = -n; k <= n; k++) {
                  BlockPos pos2 = blockPos.add(j, i, k);
                  if (this.isValidBlockAlt(pos2)) {
                     BlockState blockState = Module.mc.world.getBlockState(pos2);
                     if (blockState.getBlock() instanceof BedBlock
                        && blockState.get(BedBlock.PART) == BedPart.FOOT
                        && (pos == null || !this.isSameBed(pos2, pos, blockState))
                        && (!(((Number)this.fov.getValue()).doubleValue() < 360.0) || this.isValidBlockImpl(pos2))) {
                        list.add(pos2.toImmutable());
                     }
                  }
               }
            }
         }

         list.sort(Comparator.comparingDouble(BedAura::computeDistance));
         if (list.isEmpty()) {
            return null;
         }

         BlockPos first = (BlockPos)list.getFirst();
         BlockPos pos3 = this.findBlockPos(first, Module.mc.world.getBlockState(first));
         return pos3 == null ? null : new BedAura.Entry(first, pos3);
      } else {
         return null;
      }
   }

   private BlockPos findBlockPos(BlockPos pos, BlockState state) {
      if (!(state.getBlock() instanceof BedBlock)) {
         return null;
      }

      Direction direction = (Direction)state.get(BedBlock.FACING);
      return state.get(BedBlock.PART) == BedPart.HEAD ? pos.toImmutable() : pos.offset(direction).toImmutable();
   }

   private boolean isSameBed(BlockPos pos, BlockPos pos2, BlockState state) {
      if (pos.equals(pos2)) {
         return true;
      }

      BlockPos pos3 = this.findBlockPos(pos2, Module.mc.world.getBlockState(pos2));
      BlockPos pos4 = this.findBlockPos(pos, state);
      return pos3 != null && pos4 != null && (pos.equals(pos3) || pos4.equals(pos2) || pos4.equals(pos3));
   }

   private boolean isValidBlock(BlockPos pos) {
      if (Module.mc.world == null) {
         return false;
      }

      for (Direction direction : Direction.values()) {
         if (Module.mc.world.getBlockState(pos.offset(direction)).isAir()) {
            return false;
         }
      }

      return true;
   }

   private BlockPos findBlockPos() {
      if (this.targetBed != null && Module.mc.world != null && Module.mc.player != null) {
         if (this.isValidBlock(this.targetBed.getFoot()) && this.isValidBlock(this.targetBed.head())) {
            BlockPos pos = null;
            double max_value = Double.MAX_VALUE;

            for (BlockPos pos2 : List.of(this.targetBed.getFoot(), this.targetBed.head())) {
               for (Direction direction : Direction.values()) {
                  if (direction != Direction.DOWN) {
                     BlockPos pos3 = pos2.offset(direction);
                     if (!pos3.equals(this.targetBed.getFoot())
                        && !pos3.equals(this.targetBed.head())
                        && this.isValidBlockAlt(pos3)
                        && !Module.mc.world.getBlockState(pos3).isAir()) {
                        double dist = Module.mc.player.getEyePos().distanceTo(Vec3d.ofCenter(pos3));
                        if (pos3.equals(this.lastMinedPos)) {
                           dist--;
                        }

                        if (dist < max_value) {
                           max_value = dist;
                           pos = pos3.toImmutable();
                        }
                     }
                  }
               }
            }

            return pos;
         } else {
            return this.targetBed.getFoot();
         }
      } else {
         return null;
      }
   }

   private BlockPos findBlockPosEx() {
      if (this.targetBed == null || Module.mc.world == null || Module.mc.player == null) {
         return null;
      }

      if (this.isValidBlockEx(this.targetBed.getFoot())) {
         this.breakPath.clear();
         return this.targetBed.getFoot();
      }

      if (this.breakPath.isEmpty()) {
         this.breakPath.addAll(this.collectItems(this.targetBed.getFoot()));
      }

      while (!this.breakPath.isEmpty() && Module.mc.world.getBlockState(this.breakPath.getFirst()).isAir()) {
         this.breakPath.removeFirst();
      }

      return this.breakPath.isEmpty() ? this.findBlockPos() : this.breakPath.getFirst();
   }

   private List<BlockPos> collectItems(BlockPos pos) {
      if (Module.mc.player != null && Module.mc.world != null) {
         BlockHitResult hitResult = this.raycastHit(pos);
         if (hitResult != null && !hitResult.getBlockPos().equals(pos)) {
            BlockPos blockPos = hitResult.getBlockPos();
            ArrayDeque arrayDeque = new ArrayDeque();
            HashMap map = new HashMap();
            HashSet set = new HashSet();
            arrayDeque.add(blockPos);
            set.add(blockPos);
            map.put(blockPos, null);

            while (!arrayDeque.isEmpty()) {
               BlockPos pos2 = (BlockPos)arrayDeque.poll();
               if (this.isValid(pos2, pos)) {
                  ArrayDeque arrayDeque2 = new ArrayDeque();

                  for (BlockPos i = pos2; i != null; i = (BlockPos)map.get(i)) {
                     arrayDeque2.push(i.toImmutable());
                  }

                  return new ArrayList<>(arrayDeque2);
               }

               for (Direction direction : Direction.values()) {
                  if (direction != Direction.DOWN) {
                     BlockPos pos3 = pos2.offset(direction);
                     if (set.add(pos3) && this.isValidBlockAlt(pos3)) {
                        BlockState blockState = Module.mc.world.getBlockState(pos3);
                        if (!blockState.isAir() && !(blockState.getBlock() instanceof BedBlock)) {
                           map.put(pos3, pos2);
                           arrayDeque.add(pos3);
                        }
                     }
                  }
               }
            }

            return List.of(blockPos.toImmutable());
         } else {
            return List.of();
         }
      } else {
         return List.of();
      }
   }

   private boolean isValid(BlockPos pos, BlockPos pos2) {
      for (Direction direction : Direction.values()) {
         if (pos.offset(direction).equals(pos2)) {
            return true;
         }
      }

      return false;
   }

   private void updateBlock(BlockPos pos) {
      if (pos != null && Module.mc.player != null && Module.mc.world != null && this.isValidBlockAlt(pos)) {
         if (!(((Number)this.fov.getValue()).doubleValue() < 360.0) || this.isValidBlockImpl(pos)) {
            BlockState blockState = Module.mc.world.getBlockState(pos);
            if (blockState.isAir()) {
               this.performActionImpl();
            } else {
               this.currentBreakPos = pos;
               this.performAction(blockState);
               long now = System.currentTimeMillis();
               long l = (long)(((Number)this.miningDelay.getValue()).doubleValue() * 1000.0);
               if (!this.breaking) {
                  if (now < this.nextMineTime) {
                     return;
                  }

                  BlockHitResult hitResult = this.raycastHitEx(pos);
                  if (hitResult == null) {
                     return;
                  }

                  this.updateBlockEx(pos);
                  this.runTask(() -> BlockBreakHelper.performAction(hitResult));
                  this.breaking = true;
                  this.breakProgress = 0.0F;
                  this.nextMineTime = now + l;
               }

               this.breakProgress = this.breakProgress + this.computeFloat(blockState);
               short s = 32590;
               this.breakProgress = Math.min(1.0F, this.breakProgress);
               long l2 = 4615427720582340189L;
               int n = Math.min(9, Math.max(0, (int)(this.breakProgress * 10.0F) - 1));
               Module.mc.world.setBlockBreakingInfo(Module.mc.player.getId(), pos, n);
               this.performActionSafe();
               if ((!(this.breakProgress < 1.0F) || Module.mc.world.getBlockState(pos).isAir()) && now >= this.nextMineTime) {
                  BlockHitResult hitResult2 = this.raycastHitEx(pos);
                  if (hitResult2 != null) {
                     this.updateBlockEx(pos);
                     this.runTask(() -> BlockBreakHelper.performActionEx(hitResult2));
                     this.nextMineTime = now + l;
                     this.updateBlockAlt(pos);
                  }
               }
            }
         }
      } else {
         this.performActionImpl();
      }
   }

   private void updateBlockEx(BlockPos pos) {
      RotationManager.rotationRequests.add(new ValuePair<>(this.computeRotation(pos), 2));
   }

   private Rotation computeRotation(BlockPos pos) {
      return RotationManager.computeRotation(RotationManager.mc.player.getEyePos(), Vec3d.ofCenter(pos), false);
   }

   private BlockHitResult raycastHit(BlockPos pos) {
      Rotation rotation = this.computeRotation(pos);
      if (MinecraftClient.getInstance().player != null) {
         BlockLookUtils.vector = MinecraftClient.getInstance().player.getEyePos();
      }

      return BlockLookUtils.raycast(rotation.yaw, rotation.pitch, BlockLookUtils::isValidBlock);
   }

   private BlockHitResult raycastHitEx(BlockPos pos) {
      BlockHitResult hitResult = this.raycastHit(pos);
      return hitResult != null && hitResult.getBlockPos().equals(pos) ? hitResult : this.raycastHitAlt(pos);
   }

   private BlockHitResult raycastHitAlt(BlockPos pos) {
      if (Module.mc.player == null) {
         return null;
      }

      Vec3d eyePos = Module.mc.player.getEyePos();
      Vec3d vec = Vec3d.ofCenter(pos);
      Vec3d delta = vec.subtract(eyePos);
      Direction opposite = Direction.getFacing(delta.x, delta.y, delta.z).getOpposite();
      return new BlockHitResult(vec, opposite, pos, false);
   }

   private boolean isValidBlockEx(BlockPos pos) {
      if (Module.mc.player != null && Module.mc.world != null) {
         BlockHitResult hitResult = this.raycastHit(pos);
         return hitResult != null && hitResult.getBlockPos().equals(pos);
      } else {
         return false;
      }
   }

   private void performAction(BlockState state) {
      if ((Boolean)this.autoTool.getValue() && Module.mc.player != null) {
         int n = -1;
         float f = 1.0F;

         for (int i = 0; i < 9; i++) {
            ItemStack stack = Module.mc.player.getInventory().getStack(i);
            if (!stack.isEmpty()) {
               float miningSpeedMultiplier = stack.getMiningSpeedMultiplier(state);
               if (stack.isSuitableFor(state)) {
                  miningSpeedMultiplier += 5.0F;
               }

               if (miningSpeedMultiplier > f) {
                  f = miningSpeedMultiplier;
                  n = i;
               }
            }
         }

         if (this.previousToolSlot == -1) {
            this.previousToolSlot = Module.mc.player.getInventory().selectedSlot;
         }

         if (n != -1) {
            this.bestToolSlot = n;
            this.toolSwitched = n != this.previousToolSlot;
         } else {
            this.bestToolSlot = Module.mc.player.getInventory().selectedSlot;
            this.toolSwitched = false;
         }
      } else {
         this.bestToolSlot = Module.mc.player == null ? 0 : Module.mc.player.getInventory().selectedSlot;
         this.toolSwitched = false;
      }
   }

   private void runTask(Runnable action) {
      if (Module.mc.player != null
         && Module.mc.interactionManager != null
         && (Boolean)this.autoTool.getValue()
         && this.bestToolSlot != Module.mc.player.getInventory().selectedSlot) {
         int selectedSlot = Module.mc.player.getInventory().selectedSlot;
         Module.mc.player.getInventory().selectedSlot = this.bestToolSlot;
         Module.mc.interactionManager.syncSelectedSlot();

         try {
            action.run();
         } finally {
            Module.mc.player.getInventory().selectedSlot = selectedSlot;
            Module.mc.interactionManager.syncSelectedSlot();
         }
      } else {
         action.run();
      }
   }

   private float computeFloat(BlockState state) {
      if (Module.mc.player != null && Module.mc.world != null) {
         BlockPos pos = this.currentBreakPos != null ? this.currentBreakPos : BlockPos.ORIGIN;
         if (this.bestToolSlot >= 0 && this.bestToolSlot < 9) {
            ItemStack stack = Module.mc.player.getInventory().getStack(this.bestToolSlot);
            if (!stack.isEmpty()) {
               float miningSpeedMultiplier = stack.getMiningSpeedMultiplier(state);
               if (miningSpeedMultiplier > 1.0F && stack.isSuitableFor(state)) {
                  float hardness = state.getHardness(Module.mc.world, pos);
                  if (hardness > 0.0F) {
                     return miningSpeedMultiplier / hardness / 30.0F;
                  }
               }
            }
         }

         return state.calcBlockBreakingDelta(Module.mc.player, Module.mc.world, pos);
      } else {
         return 0.0F;
      }
   }

   @EventTarget
   public void onHand(HandEvent handEvent) {
      if ((Boolean)this.autoTool.getValue() && this.toolSwitched && Module.mc.player != null && handEvent.getHand() == Hand.MAIN_HAND && this.previousToolSlot != -1
         )
       {
         ItemStack stack = Module.mc.player.getInventory().getStack(this.previousToolSlot);
         if (!stack.isEmpty()) {
            handEvent.setItemStack(stack);
         }
      }
   }

   private void updateBlockAlt(BlockPos pos) {
      this.lastMinedPos = pos != null ? pos.toImmutable() : null;
      if (Module.mc.player != null && Module.mc.world != null && pos != null) {
         Module.mc.world.setBlockBreakingInfo(Module.mc.player.getId(), pos, -1);
      }

      if (this.breakMode.getDisplayTextEx().equalsIgnoreCase("Full") && !this.breakPath.isEmpty() && this.breakPath.getFirst().equals(pos)) {
         this.breakPath.removeFirst();
      }

      this.performActionDirect();
      this.performActionAlt();
      if (!this.breakMode.getDisplayTextEx().equalsIgnoreCase("Full") || this.breakPath.isEmpty()) {
         this.targetBed = null;
      }

      this.nextTargetTime = System.currentTimeMillis() + 250L;
   }

   private void performActionAlt() {
      this.breakProgress = 0.0F;
      this.currentBreakPos = null;
      this.breaking = false;
      this.bestToolSlot = Module.mc.player == null ? 0 : Module.mc.player.getInventory().selectedSlot;
      this.toolSwitched = false;
   }

   private void performActionImpl() {
      if (this.currentBreakPos != null && Module.mc.world != null && Module.mc.player != null) {
         Module.mc.world.setBlockBreakingInfo(Module.mc.player.getId(), this.currentBreakPos, -1);
      }

      BlockBreakHelper.performActionEx();
      this.performActionDirect();
      this.breakPath.clear();
      this.performActionAlt();
   }

   private void performActionDirect() {
      if (this.progressHandle != null) {
         this.progressHandle.performActionAlt();
         this.progressHandle = null;
      }
   }

   private void performActionSafe() {
      if (this.breaking && !(this.breakProgress <= 0.0F)) {
         ProgressBarOverlay progressBarOverlay = Southside.INSTANCE.getProgressBarOverlay();
         if (progressBarOverlay != null) {
            if (this.progressHandle == null || !this.progressHandle.isValid()) {
               this.progressHandle = progressBarOverlay.getInner(1000, "BedAura");
            }

            this.progressHandle.onKeyTyped((int)(Math.min(1.0F, this.breakProgress) * 1000.0F));
         }
      } else {
         this.performActionDirect();
      }
   }

   private boolean isValidBlockAlt(BlockPos pos) {
      return Module.mc.player != null && computeDistance(pos) <= ((Number)this.range.getValue()).doubleValue();
   }

   private static double computeDistance(BlockPos pos) {
      return Module.mc.player.getEyePos().distanceTo(Vec3d.ofCenter(pos));
   }

   private boolean isValidBlockImpl(BlockPos pos) {
      if (Module.mc.player == null) {
         return false;
      }

      Vec3d delta = Vec3d.ofCenter(pos).subtract(Module.mc.player.getEyePos());
      double d = Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0;
      double d2 = Math.abs(MathHelper.wrapDegrees(d - Module.mc.player.getYaw()));
      return d2 <= ((Number)this.fov.getValue()).doubleValue() / 2.0;
   }

   @EventTarget
   public void onCamera(CameraEvent cameraEvent) {
      if ((Boolean)this.renderBox.getValue() && Module.mc.world != null) {
         if (this.targetBed != null) {
            this.updateBlockImpl(this.targetBed.getFoot());
            this.updateBlockImpl(this.targetBed.head());
         }

         for (BlockPos pos : this.breakPath) {
            if (this.currentBreakPos == null || !this.currentBreakPos.equals(pos)) {
               BlockState blockState = Module.mc.world.getBlockState(pos);
               if (!blockState.isAir()) {
                  this.renderStateBox(pos, blockState, ((OneColor)this.pathSideColor.getValue()).blendColor(), ((OneColor)this.pathLineColor.getValue()).blendColor());
               }
            }
         }

         if (this.currentBreakPos != null) {
            BlockState blockState2 = Module.mc.world.getBlockState(this.currentBreakPos);
            if (!blockState2.isAir()) {
               this.renderStateBox(
                  this.currentBreakPos,
                  blockState2,
                  ((OneColor)this.currentBreakSideColor.getValue()).blendColor(),
                  ((OneColor)this.currentBreakLineColor.getValue()).blendColor()
               );
            }
         }
      }
   }

   private void updateBlockImpl(BlockPos pos) {
      BlockState blockState = Module.mc.world.getBlockState(pos);
      if (blockState.getBlock() instanceof BedBlock) {
         this.renderStateBox(pos, blockState, ((OneColor)this.bedSideColor.getValue()).blendColor(), ((OneColor)this.bedLineColor.getValue()).blendColor());
      }
   }

   private void renderStateBox(BlockPos pos, BlockState state, Color color, Color color2) {
      VoxelShape outlineShape = state.getOutlineShape(Module.mc.world, pos);
      Box box = outlineShape.isEmpty() ? new Box(pos) : outlineShape.getBoundingBox().offset(pos);
      BoxMeshRenderer.renderMesh.renderBox(box, color);
      BoxMeshRenderer.renderMeshValue.renderBoxEx(box, color2);
   }

   private record Entry(BlockPos foot, BlockPos head) {
      public BlockPos getFoot() {
         return this.foot;
      }
   }
}
