package dev.southside.utils;

import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.TransferOrigin;
import dev.southside.event.events.DirectionEvent;
import dev.southside.event.events.SprintingEvent;
import dev.southside.event.events.StateEvent;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.LadderBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public class MovementUtils implements IMinecraft {
   public static int count = 0;
   public static int countValue = 0;
   public static int countData = 0;
   public static int countRef = 0;
   public static int countPrimary = 0;
   public static int countSecondary = 0;
   public static double value = Double.MIN_VALUE;
   public static double valueValue = Double.MIN_VALUE;
   public static boolean active = false;
   public static Vec3d vector = new Vec3d(0.0, 0.0, 0.0);
   public static boolean activeValue = false;
   private static int[] values;

   public static boolean isValid() {
      return mc.player != null && (mc.player.sidewaysSpeed != 0.0F || mc.player.forwardSpeed != 0.0F);
   }

   public static int computeInt() {
      return mc.player != null && mc.player.hasStatusEffect(StatusEffects.SPEED) ? mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier() + 1 : 0;
   }

   public static Block getBlock(double x, double y, double z) {
      return mc.world.getBlockState(mc.player.getBlockPos().add((int)x, (int)y, (int)z)).getBlock();
   }

   public static boolean isValidEx() {
      return mc.options.forwardKey.isPressed() && !mc.options.backKey.isPressed();
   }

   public static float computeFloat(float x, float y) {
      float yaw = MinecraftClient.getInstance().player.getYaw();
      boolean flag = x > 0.0F;
      boolean flag2 = x < 0.0F;
      boolean flag3 = y > 0.0F;
      boolean flag4 = y < 0.0F;
      boolean flag5 = flag3 || flag4;
      boolean flag6 = flag || flag2;
      if (x != 0.0F || y != 0.0F) {
         if (flag2 && !flag5) {
            return yaw + 180.0F;
         }

         if (flag && flag4) {
            return yaw + 45.0F;
         }

         if (flag && flag3) {
            return yaw - 45.0F;
         }

         if (!flag6 && flag4) {
            return yaw + 90.0F;
         }

         if (!flag6 && flag3) {
            return yaw - 90.0F;
         }

         if (flag2 && flag4) {
            return yaw + 135.0F;
         }

         if (flag2) {
            return yaw - 135.0F;
         }
      }

      return yaw;
   }

   @EventTarget(priority = 200)
   public void onSprinting(SprintingEvent sprintingEvent) {
      if (!sprintingEvent.isSprinting()) {
         countSecondary = 0;
      } else {
         countSecondary++;
      }
   }

   public static boolean isValidAlt() {
      if (mc.player == null) {
         return false;
      }

      PlayerMoveSimulator playerMoveSimulator = new PlayerMoveSimulator(mc.player);
      playerMoveSimulator.onKeyTyped(1);
      BlockPos pos = new BlockPos((int)playerMoveSimulator.getX(), (int)(mc.player.getY() - 0.1), (int)playerMoveSimulator.getZ());
      return mc.world.getBlockState(pos).getBlock() instanceof AirBlock && playerMoveSimulator.getMotion().y < 0.0;
   }

   public static Vec3d getVector() {
      return vector;
   }

   public static float evaluate(float f) {
      float f2 = f % 360.0F;
      if (f2 >= 180.0F) {
         f2 -= 360.0F;
      }

      if (f2 < -180.0F) {
         f2 += 360.0F;
      }

      return f2;
   }

   public static double[] getDoubleList(double d) {
      float movementForward = mc.player.input.movementForward;
      float movementSideways = mc.player.input.movementSideways;
      float yaw = mc.player.getYaw();
      if (movementForward != 0.0F) {
         if (movementSideways > 0.0F) {
            yaw += movementForward > 0.0F ? -45.0F : 45.0F;
         } else if (movementSideways < 0.0F) {
            yaw += movementForward > 0.0F ? 45.0F : -45.0F;
         }

         movementSideways = 0.0F;
         if (movementForward > 0.0F) {
            movementForward = 1.0F;
         } else if (movementForward < 0.0F) {
            movementForward = -1.0F;
         }
      }

      double d2 = Math.sin(Math.toRadians(yaw + 90.0F));
      double d3 = Math.cos(Math.toRadians(yaw + 90.0F));
      double d4 = movementForward * d * d3 + movementSideways * d * d2;
      double d5 = movementForward * d * d2 - movementSideways * d * d3;
      return new double[]{d4, d5};
   }

   public static boolean isValid(Entity entity, double d) {
      if (d >= 360.0) {
         return true;
      }

      Vec3d cameraPosVec = mc.player.getCameraPosVec(1.0F);
      Vec3d center = entity.getBoundingBox().getCenter();
      Vec3d delta = center.subtract(cameraPosVec);
      double horizDist = MathHelper.hypot(delta.x, delta.z);
      float f = (float)(MathHelper.atan2(delta.z, delta.x) * (180.0 / Math.PI)) - 90.0F;
      float f2 = (float)(-(MathHelper.atan2(delta.y, horizDist) * (180.0 / Math.PI)));
      Rotation rotation = new Rotation(f, f2);
      double d2 = MathHelper.angleBetween(rotation.yaw, mc.player.getYaw());
      double d3 = MathHelper.angleBetween(rotation.pitch, mc.player.getPitch());
      double horizDist2 = Math.hypot(d2, d3);
      return horizDist2 <= d;
   }

   public static boolean isValid(double d) {
      for (byte i = 0; i < d; i += 2) {
         Box box = mc.player.getBoundingBox().offset(0.0, -i, 0.0);
         if (!mc.world.canCollide(mc.player, box)) {
            return true;
         }
      }

      return false;
   }

   public static void performAction(double d) {
      ClientPlayerEntity player = mc.player;
      if (player != null && isValid()) {
         float movementSideways = mc.player.input.movementSideways;
         float movementForward = mc.player.input.movementForward;
         double d2 = Math.toRadians(computeFloat(movementForward, movementSideways));
         double d3 = -MathHelper.sin((float)d2) * d;
         double d4 = MathHelper.cos((float)d2) * d;
         player.addVelocity(d3, 0.0, d4);
      }
   }

   public static float computeFloat(PlayerEntity player) {
      return player != null && mc.world != null
         ? (float)Math.sqrt(player.getVelocity().x * player.getVelocity().x + player.getVelocity().z * player.getVelocity().z)
         : 0.0F;
   }

   public static boolean isValidImpl() {
      if (mc.player == null) {
         return false;
      }

      if (mc.player.getY() < 0.0) {
         return true;
      }

      for (int i = (int)(mc.player.getY() - 1.0); i >= 1; i--) {
         if (!mc.world.getBlockState(BlockPos.ofFloored(mc.player.getX(), i, mc.player.getZ())).isAir()) {
            return false;
         }
      }

      return !mc.player.isOnGround();
   }

   public static void setCount(int n) {
      count = n;
   }

   public static float getProgress() {
      if (mc.player == null) {
         return 0.0F;
      }

      float yaw = mc.player.getYaw();
      float f = 45.0F;
      Input input = mc.player.input;
      if (input.movementForward < 0.0F) {
         f = -45.0F;
         yaw += 180.0F;
      }

      if (input.playerInput.left()) {
         yaw -= f;
         if (input.movementForward == 0.0F) {
            yaw -= 45.0F;
         }
      } else if (input.playerInput.right()) {
         yaw += f;
         if (input.movementForward == 0.0F) {
            yaw += 45.0F;
         }
      }

      return (yaw % 360.0F + 360.0F) % 360.0F;
   }

   public static void performAction() {
      if (mc.player != null) {
         mc.player.setVelocity(0.0, mc.player.getVelocity().y, 0.0);
         mc.player.input.movementForward = 0.0F;
         mc.player.input.movementSideways = 0.0F;
      }
   }

   public void performActionEx() {
      Southside.INSTANCE.getEventBus().subscribe(this);
   }

   public static void performActionAlt() {
      if (mc.world != null) {
         for (Entity entity : mc.world.getEntities()) {
            entity.setPosition(entity.getPos());
         }
      }
   }

   public static void setActiveValue(boolean flag) {
      activeValue = flag;
   }

   public static boolean isValidDirect() {
      BlockState blockState = mc.world.getBlockState(mc.player.getBlockPos());
      return blockState.isIn(BlockTags.CLIMBABLE) || !mc.player.isOnGround() && mc.player.getVelocity().y < 0.0 && blockState.getBlock() instanceof LadderBlock;
   }

   @EventTarget
   public void onDirection(DirectionEvent directionEvent) {
      if (directionEvent.getDirection().equals(TransferOrigin.RECEIVE) && directionEvent.getPacket() instanceof PlayerPositionLookS2CPacket) {
         countPrimary = 0;
      }
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      if (stateEvent.getState().equals(Stage.POST) && mc.player != null) {
         vector = mc.player.getVelocity();
      }
   }

   public static boolean isBlockVisible(Entity entity) {
      Vec3d vec = new Vec3d(0.0, 0.0, 0.0);
      Vec3d vec2 = new Vec3d(0.0, 0.0, 0.0);
      ((IMutableVec3d)vec).set(mc.player.getX(), mc.player.getY() + mc.player.getStandingEyeHeight(), mc.player.getZ());
      ((IMutableVec3d)vec2).set(entity.getX(), entity.getY(), entity.getZ());
      boolean flag = mc.world.raycast(new RaycastContext(vec, vec2, ShapeType.COLLIDER, FluidHandling.NONE, mc.player)).getType() == Type.MISS;
      ((IMutableVec3d)vec2).set(entity.getX(), entity.getY() + entity.getStandingEyeHeight(), entity.getZ());
      boolean flag2 = mc.world.raycast(new RaycastContext(vec, vec2, ShapeType.COLLIDER, FluidHandling.NONE, mc.player)).getType() == Type.MISS;
      return flag || flag2;
   }

   public static boolean isValidSafe() {
      if (mc.player.isSubmergedInWater()) {
         return true;
      }

      boolean flag = false;
      int minY = (int)mc.player.getBoundingBox().minY;

      for (int i = MathHelper.floor(mc.player.getBoundingBox().minX); i < MathHelper.floor(mc.player.getBoundingBox().maxX) + 1; i++) {
         for (int j = MathHelper.floor(mc.player.getBoundingBox().minZ); j < MathHelper.floor(mc.player.getBoundingBox().maxZ) + 1; j++) {
            BlockPos pos = new BlockPos(i, minY, j);
            BlockState blockState = mc.world.getBlockState(pos);
            Block block = blockState.getBlock();
            if (block != Blocks.AIR) {
               if (!(block instanceof FluidBlock)) {
                  return false;
               }

               flag = true;
            }
         }
      }

      return flag;
   }

   static {
      setValues(null);
   }

   public static void setValues(int[] values2) {
      values = values2;
   }
}
