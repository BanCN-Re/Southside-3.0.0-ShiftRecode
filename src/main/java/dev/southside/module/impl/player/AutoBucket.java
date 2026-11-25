package dev.southside.module.impl.player;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.events.RightClickEvent;
import dev.southside.event.events.DisableRotationEvent;
import dev.southside.event.events.MinecraftClientEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.ModuleManager;
import dev.southside.module.impl.combat.KillAura;
import dev.southside.module.impl.combat.Velocity;
import dev.southside.module.impl.movement.LongJump;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.InteractionStateFlags;
import dev.southside.utils.HotbarItemFinder;
import dev.southside.utils.NumberValue;
import dev.southside.utils.RotationManager;
import dev.southside.utils.MovementUtils;
import dev.southside.utils.BlockLookUtils;
import dev.southside.utils.Rotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.ShortPlantBlock;
import net.minecraft.block.SnowBlock;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Items;
import net.minecraft.item.MaceItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Direction.Axis;

public class AutoBucket extends Module {
   @SerializedName("antiPause")
   public final BooleanValue antiPause = new BooleanValue("AntiPause", true);
   @SerializedName("minDistance")
   public final NumberValue minDistance = new NumberValue("MinDistance", 3, 3, 10);
   @SerializedName("autoMLG")
   public final BooleanValue autoMLG = new BooleanValue("AutoMLG", false);
   @SerializedName("antiFire")
   public final BooleanValue antiFire = new BooleanValue("AntiFire", false);
   @SerializedName("macePriority")
   public final BooleanValue macePriority = new BooleanValue("Mace Priority", true);
   @SerializedName("fireballCooldown")
   public final BooleanValue fireballCooldown = new BooleanValue("Fireball Cooldown", true);
   @SerializedName("fireballCooldownTicks")
   private int fireballCooldownTicks;
   @SerializedName("rotationPending")
   private boolean rotationPending;
   @SerializedName("collectWaterPending")
   private boolean collectWaterPending;
   @SerializedName("previousSlot")
   private int previousSlot = -1;
   @SerializedName("pendingActionTicks")
   private int pendingActionTicks;

   public AutoBucket() {
      super(Category.Player, "AutoBucket", "我会落地水，");
   }

   @Override
   public int getHeightEx() {
      return 495;
   }

   @Override
   public int getHeightAlt() {
      return this.macePriority.getValue()
            && mc2.player != null
            && mc2.player.getMainHandStack().getItem() instanceof MaceItem
            && MaceItem.shouldDealAdditionalDamage(mc2.player)
         ? Southside.INSTANCE.getModuleManager().getModule(KillAura.class).getHeightAlt() - 1
         : 2 + this.getHeightEx();
   }

   public boolean isValidEx() {
      return this.rotationPending || this.collectWaterPending;
   }

   public boolean isValidAlt() {
      if (!this.isValidEx()) {
         this.pendingActionTicks = 0;
         return false;
      }

      if (++this.pendingActionTicks <= 20) {
         return true;
      }

      this.collectWaterPending = false;
      this.rotationPending = false;
      this.pendingActionTicks = 0;
      this.performActionImpl();
      return false;
   }

   @EventTarget(priority = -200)
   public void onCustomEvent2(RightClickEvent rightClickEvent) {
      if ((Boolean)this.fireballCooldown.getValue()
         && !rightClickEvent.isCancelled()
         && mc2.player != null
         && (mc2.player.getOffHandStack().isOf(Items.FIRE_CHARGE) || mc2.player.getMainHandStack().isOf(Items.FIRE_CHARGE))) {
         this.fireballCooldownTicks = 10;
      }
   }

   @EventTarget
   public void onMinecraftClient(MinecraftClientEvent minecraftClientEvent) {
      Rotation currentRotation = RotationManager.getCurrentRotation();
      if (currentRotation != null && this.rotationPending && mc2.player != null && mc2.interactionManager != null) {
         BlockHitResult hitResult = BlockLookUtils.raycast(currentRotation.yaw, currentRotation.pitch, AutoBucket::isValidBlock);
         if (hitResult != null) {
            this.rotationPending = false;
            mc2.crosshairTarget = hitResult;
            ActionResult actionResult = mc2.interactionManager.interactItem(mc2.player, Hand.MAIN_HAND);
            InteractionStateFlags.active = true;
            if (actionResult.isAccepted()) {
               mc2.player.swingHand(Hand.MAIN_HAND);
            }
         }

         if (mc2.player.getInventory().getStack(mc2.player.getInventory().selectedSlot).isOf(Items.BUCKET)) {
            this.collectWaterPending = true;
         }

         this.performActionImpl();
      }
   }

   @EventTarget
   public void onDisableRotation(DisableRotationEvent disableRotationEvent) {
      if (mc2.player != null && mc2.world != null && MovementUtils.countPrimary >= 3) {
         if (this.fireballCooldownTicks > 0) {
            this.fireballCooldownTicks--;
         }

         if (!mc2.player.isCreative()
            && !Southside.INSTANCE.getModuleManager().getModule(LongJump.class).isEnabled()
            && !mc2.player.isUsingItem()
            && mc2.currentScreen == null) {
            List list = this.isValidImpl() ? collectItems() : List.of();
            boolean isValidDirect = this.isValidDirect();
            if (!list.isEmpty() || isValidDirect) {
               int n = HotbarItemFinder.computeInt(0, 9, Items.WATER_BUCKET);
               if (n != -1) {
                  if (list.isEmpty()) {
                     list = collectItems();
                  }

                  if (list.isEmpty()) {
                     return;
                  }

                  AutoBucket.Entry entry = getEntry(list);
                  this.previousSlot = mc2.player.getInventory().selectedSlot;
                  this.performAction(n, entry.position);
                  this.collectWaterPending = true;
                  return;
               }
            }

            this.performActionAlt();
         }
      }
   }

   private boolean isValidImpl() {
      if (!(Boolean)this.autoMLG.getValue()) {
         return false;
      }

      float f = ((Number)this.minDistance.getValue()).floatValue();
      if (mc2.player.hasStatusEffect(StatusEffects.JUMP_BOOST)) {
         StatusEffectInstance statusEffect = mc2.player.getStatusEffect(StatusEffects.JUMP_BOOST);
         if (statusEffect != null) {
            f += statusEffect.getAmplifier() + 1;
         }
      }

      return mc2.player.fallDistance > f && !mc2.player.isInsideWaterOrBubbleColumn();
   }

   private boolean isValidDirect() {
      return (Boolean)this.antiFire.getValue()
         && mc2.player.isOnFire()
         && !mc2.player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)
         && !mc2.player.isInsideWaterOrBubbleColumn()
         && !this.isValidSafe()
         && this.fireballCooldownTicks <= 0;
   }

   private boolean isValidSafe() {
      if (!(Boolean)this.antiPause.getValue()) {
         return false;
      }

      ModuleManager moduleManager = Southside.INSTANCE.getModuleManager();
      boolean flag = KillAura.target != null && mc2.player.distanceTo(KillAura.target) <= moduleManager.getModule(KillAura.class).range.getCurrentValueEx();
      boolean flag2 = moduleManager.getModule(Velocity.class).isEnabled() && Velocity.activeValue;
      return flag || flag2;
   }

   private static List<AutoBucket.Entry> collectItems() {
      ArrayList list = new ArrayList();
      Vec3d velocity = mc2.player.getVelocity();
      int n = (int)Math.ceil(velocity.x * velocity.x);
      int n2 = (int)Math.ceil(velocity.y * velocity.y);
      int n3 = (int)Math.ceil(velocity.z * velocity.z);
      BlockPos blockPos = mc2.player.getBlockPos();
      Box boundingBox = mc2.player.getBoundingBox();

      for (int i = -n; i <= n; i++) {
         for (int j = -n3; j <= n3; j++) {
            for (int k = 0; k <= n2; k++) {
               BlockPos pos = blockPos.add(i, 0, j).down(k);
               BlockState blockState = mc2.world.getBlockState(pos);
               Box box = new Box(pos).withMaxY(pos.getY() + 3);
               if (box.intersects(boundingBox) && !blockState.isAir()) {
                  list.add(new AutoBucket.Entry(pos, box));
               }
            }
         }
      }

      return list;
   }

   private static AutoBucket.Entry getEntry(List<AutoBucket.Entry> list) {
      list.sort(Comparator.comparingInt(entry -> entry.position.getY()));
      Collections.reverse(list);
      int y = ((AutoBucket.Entry)list.getFirst()).position.getY();
      list.removeIf(entry2 -> entry2.position.getY() != y);
      Box boundingBox = mc2.player.getBoundingBox();
      Vec3d vec = boundingBox.getCenter().withAxis(Axis.Y, boundingBox.minY);
      list.sort(Comparator.comparingDouble(entry3 -> Vec3d.of(entry3.position).squaredDistanceTo(vec)));
      list.sort(Comparator.comparing(entry4 -> !entry4.bounds.contains(mc2.player.getPos())));
      return (AutoBucket.Entry)list.getFirst();
   }

   private void performActionAlt() {
      int n = HotbarItemFinder.computeInt(0, 9, Items.BUCKET);
      if (n != -1 && this.collectWaterPending) {
         this.previousSlot = mc2.player.getInventory().selectedSlot;
         int n2 = (int)Math.ceil(mc2.player.getBlockInteractionRange());
         BlockPos blockPos = mc2.player.getBlockPos();
         ArrayList<BlockPos> list = new ArrayList<>();

         for (int i = -n2; i <= n2; i++) {
            for (int j = -n2; j <= n2; j++) {
               for (int k = -n2; k <= n2; k++) {
                  BlockPos pos = blockPos.add(i, j, k);
                  FluidState fluidState = mc2.player.getWorld().getFluidState(pos);
                  if (fluidState.isStill() && fluidState.getFluid() == Fluids.WATER) {
                     list.add(pos);
                  }
               }
            }
         }

         list.removeIf(AutoBucket::isValidBlock);
         if (!list.isEmpty()) {
            this.performAction(n, (BlockPos)list.getFirst());
            this.collectWaterPending = false;
         }
      }
   }

   private void performAction(int n, BlockPos pos) {
      Rotation rotation = computeRotation(pos);
      mc2.player.getInventory().selectedSlot = n;
      RotationManager.requestRotation(rotation, this.getHeightAlt());
      this.rotationPending = true;
   }

   private static Rotation computeRotation(BlockPos pos) {
      Vec3d delta = RotationManager.computePosition(pos, Direction.UP).subtract(mc2.player.getEyePos());
      float f = (float)Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0F;
      float f2 = (float)Math.toDegrees(-Math.atan2(delta.y, Math.sqrt(delta.x * delta.x + delta.z * delta.z)));
      return new Rotation(f, f2);
   }

   private static boolean isValidBlock(BlockPos pos) {
      Rotation rotation = computeRotation(pos);
      BlockHitResult hitResult = BlockLookUtils.raycast(rotation.yaw, rotation.pitch, AutoBucket::isValidBlock);
      return hitResult == null || !pos.equals(hitResult.getBlockPos());
   }

   private static boolean isValidBlock(BlockState state) {
      Block block = state.getBlock();
      return block instanceof FluidBlock
         ? false
         : block instanceof PlantBlock || block instanceof SnowBlock || block instanceof AirBlock || block instanceof ShortPlantBlock;
   }

   private void performActionImpl() {
      if (this.previousSlot != -1 && mc2.player != null) {
         mc2.player.getInventory().selectedSlot = this.previousSlot;
         this.previousSlot = -1;
      }
   }

   private record Entry(BlockPos position, Box bounds) {

      public BlockPos getPosition() {
         return this.position;
      }

      public Box getBounds() {
         return this.bounds;
      }
   }
}
