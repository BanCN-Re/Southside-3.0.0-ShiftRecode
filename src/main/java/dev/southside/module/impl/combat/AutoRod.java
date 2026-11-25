package dev.southside.module.impl.combat;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.events.DirectionEvent;
import dev.southside.event.events.DisableRotationEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.movement.StrafeFix;
import dev.southside.module.impl.world.Scaffold;
import dev.southside.utils.ValuePair;
import dev.southside.utils.EntityTargetUtils;
import dev.southside.utils.MillisTimer;
import dev.southside.utils.NumberValue;
import dev.southside.utils.RotationManager;
import dev.southside.utils.Rotation;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public class AutoRod extends Module {
   private static final double DEFAULT_DOUBLE = 1.5;
   private static final double DEFAULT_DOUBLE_VALUE = 0.03;
   private static final int DEFAULT_INT = -1;
   private static final int DEFAULT_INT_VALUE = -2;
   @SerializedName("delay")
   public final NumberValue delay = new NumberValue("Delay", 500, 100, 1000, 10);
   @SerializedName("minRange")
   public final NumberValue minRange = new NumberValue("MinRange", 3.5, 3, 20, 0.1);
   @SerializedName("maxRange")
   public final NumberValue maxRange = new NumberValue("MaxRange", 12, 3, 20, 0.1);
   @SerializedName("fov")
   public final NumberValue fov = new NumberValue("Fov", 180.0F, 0.0F, 180.0F, 1.0F);
   @SerializedName("predictTicks")
   public final NumberValue predictTicks = new NumberValue("Predict Ticks", 2, 0, 5, 1);
   @SerializedName("keepTime")
   public final NumberValue keepTime = new NumberValue("KeepTime", 500, 100, 2000, 10);
   @SerializedName("targetRotation")
   private Rotation targetRotation;
   @SerializedName("rodTimer")
   private final MillisTimer rodTimer = new MillisTimer();
   @SerializedName("rodThrown")
   public boolean rodThrown;
   @SerializedName("retractAt")
   private long retractAt;
   @SerializedName("previousSlot")
   private int previousSlot = -1;
   @SerializedName("rodHand")
   private Hand rodHand;
   @SerializedName("target")
   private PlayerEntity target;
   @SerializedName("castPending")
   private boolean castPending;

   public AutoRod() {
      super(Category.Combat, "AutoRod", "Automatically uses fishing rods");
   }

   @Override
   public void postInit() {
      this.addRangedValueRestrict(this.minRange, this.maxRange);
   }

   @Override
   public void onEnable() {
      this.rodTimer.reset();
      this.targetRotation = null;
      this.rodHand = null;
      this.target = null;
      this.castPending = false;
      this.rodThrown = false;
   }

   @EventTarget
   public void onDirection(DirectionEvent directionEvent) {
      if (directionEvent.getPacket() instanceof PlayerInteractEntityC2SPacket) {
         this.performActionAlt();
      } else if (directionEvent.getPacket() instanceof PlayerInteractItemC2SPacket playerInteractItemC2SPacket) {
         ItemStack stack = playerInteractItemC2SPacket.getHand() == Hand.MAIN_HAND ? Module.mc.player.getMainHandStack() : Module.mc.player.getOffHandStack();
         if (!(stack.getItem() instanceof FishingRodItem)) {
            this.performActionAlt();
         }
      } else if (directionEvent.getPacket() instanceof PlayerActionC2SPacket playerActionC2SPacket && playerActionC2SPacket.getAction() == Action.START_DESTROY_BLOCK) {
         this.performActionAlt();
      }
   }

   private void performActionAlt() {
      this.castPending = false;
      this.targetRotation = null;
   }

   @EventTarget
   public void onDisableRotation(DisableRotationEvent disableRotationEvent) {
      if (MinecraftClient.getInstance() != null
         && MinecraftClient.getInstance().player != null
         && MinecraftClient.getInstance().world != null
         && Module.mc.currentScreen == null
         && !Module.mc.player.isUsingItem()
         && !Southside.INSTANCE.getModuleManager().getModule(Scaffold.class).isEnabled()) {
         long now = System.currentTimeMillis();
         if (this.rodThrown) {
            if (this.target != null && Module.mc.player.getPos().distanceTo(this.target.getPos()) < ((Number)this.minRange.getValue()).floatValue()
               || now >= this.retractAt) {
               this.performActionImpl();
            }
         } else if (this.castPending) {
            this.initPostRunnable();
         } else {
            this.performAction(now);
         }
      }
   }

   private void performActionImpl() {
      Module.mc.interactionManager.interactItem(Module.mc.player, this.rodHand);
      if (this.previousSlot != -1) {
         Module.mc.player.getInventory().selectedSlot = this.previousSlot;
         this.previousSlot = -1;
      }

      this.rodThrown = false;
      this.performActionAlt();
      this.rodHand = null;
      this.target = null;
   }

   private void initPostRunnable() {
      int n = this.computeInt();
      if (n == -1) {
         this.castPending = false;
      } else {
         Hand hand = n == -2 ? Hand.OFF_HAND : Hand.MAIN_HAND;
         this.rodHand = hand;
         if (n != -2) {
            this.previousSlot = Module.mc.player.getInventory().selectedSlot;
            Module.mc.player.getInventory().selectedSlot = n;
         }

         Module.mc.interactionManager.interactItem(Module.mc.player, hand);
         this.rodThrown = true;
         this.performActionAlt();
      }
   }

   private int computeInt() {
      if (Module.mc.player == null) {
         return -1;
      }

      ItemStack offHandStack = Module.mc.player.getOffHandStack();
      if (!offHandStack.isEmpty() && offHandStack.getItem() instanceof FishingRodItem) {
         return -2;
      }

      for (int i = 0; i < 9; i++) {
         ItemStack stack = Module.mc.player.getInventory().getStack(i);
         if (!stack.isEmpty() && stack.getItem() instanceof FishingRodItem) {
            return i;
         }
      }

      return -1;
   }

   private void performAction(long l) {
      if (this.rodTimer.hasElapsed(((Number)this.delay.getValue()).intValue())) {
         List<Entity> list = KillAura.target != null
            ? KillAura.targets
            : EntityTargetUtils.collectItems(((Number)this.minRange.getValue()).floatValue(), ((Number)this.maxRange.getValue()).floatValue());
         List<Entity> list2 = list.stream().filter(this::isValidTarget).toList();
         if (!list2.isEmpty() && this.computeInt() != -1) {
            if (list2.get(0) instanceof PlayerEntity player) {
               this.rodTimer.reset();
               double d = ((Number)this.keepTime.getValue()).doubleValue() / 50.0;
               this.retractAt = l + ((Number)this.keepTime.getValue()).intValue();
               this.target = player;
               Vec3d vec = this.computePosition(player, d);
               if (vec != null && !this.isBlockVisible(vec, d)) {
                  this.targetRotation = RotationManager.getRotationToPos(vec);
                  RotationManager.rotationRequests.add(new ValuePair<>(this.targetRotation, 0));
                  RotationManager.moveFixMode = Southside.INSTANCE.getModuleManager().getModule(StrafeFix.class).getMoveFixMode();
                  this.castPending = true;
               }
            }
         }
      }
   }

   private Vec3d computePosition(Entity entity, double d) {
      if (Module.mc.world == null) {
         return null;
      }

      Vec3d delta = entity.getPos().subtract(entity.prevX, entity.prevY, entity.prevZ);
      double d2 = entity.getX() + delta.x * d;
      double d3 = entity.getY() + delta.y * d;
      double d4 = entity.getZ() + delta.z * d;
      return new Vec3d(d2, d3 + entity.getStandingEyeHeight(), d4);
   }

   private boolean isBlockVisible(Vec3d vec, double d) {
      if (Module.mc.player != null && Module.mc.world != null) {
         List list = this.collectItems(vec, d);

         for (int i = 0; i < list.size() - 1; i++) {
            BlockHitResult hitResult = Module.mc
               .world
               .raycast(new RaycastContext((Vec3d)list.get(i), (Vec3d)list.get(i + 1), ShapeType.COLLIDER, FluidHandling.NONE, Module.mc.player));
            if (hitResult.getType() == Type.BLOCK) {
               BlockPos blockPos = hitResult.getBlockPos();
               if (!Module.mc.world.getBlockState(blockPos).isAir() && !Module.mc.world.getBlockState(blockPos).getCollisionShape(Module.mc.world, blockPos).isEmpty()) {
                  return true;
               }
            }
         }

         return false;
      } else {
         return true;
      }
   }

   public boolean isValidTarget(Entity entity) {
      float f = ((Number)this.fov.getValue()).floatValue();
      return RotationManager.computeFloat(entity) <= f;
   }

   public double computeDistance(Entity entity) {
      double dist = entity.getPos().distanceTo(Module.mc.player.getPos());
      return Math.max(dist / 8.0, 1.0);
   }

   public List<Vec3d> collectItems(Vec3d vec, double d) {
      if (Module.mc.player == null) {
         return List.of();
      }

      ArrayList list = new ArrayList();
      Vec3d vec2 = new Vec3d(Module.mc.player.getX(), Module.mc.player.getEyeY(), Module.mc.player.getZ());
      Vec3d dir = vec.subtract(vec2).normalize();
      Vec3d vec3 = dir.multiply(1.5);
      Vec3d vec4 = vec2;
      list.add(vec2);
      int n = (int)Math.ceil(d);

      for (int i = 0; i < n; i++) {
         vec4 = vec4.add(vec3);
         vec3 = vec3.add(0.0, -0.03, 0.0);
         list.add(vec4);
      }

      return list;
   }
}
