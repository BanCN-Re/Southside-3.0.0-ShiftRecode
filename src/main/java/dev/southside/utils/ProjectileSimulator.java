package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.DragonFireballEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.EggItem;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.ExperienceBottleItem;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SnowballItem;
import net.minecraft.item.ThrowablePotionItem;
import net.minecraft.item.TridentItem;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import org.joml.Quaterniond;
import org.joml.Vector3d;

public class ProjectileSimulator {
   public static final Mutable mutable = new Mutable();
   public static final Vector3d vector = new Vector3d();
   public static final Vector3d vectorValue = new Vector3d();
   @SerializedName("position")
   public final Vector3d position = new Vector3d();
   @SerializedName("velocity")
   public final Vector3d velocity = new Vector3d();
   @SerializedName("gravity")
   public double gravity;
   @SerializedName("drag")
   public double drag;
   @SerializedName("waterDrag")
   public double waterDrag;

   public Vector3d getVector3d(Vec3d vec) {
      return new Vector3d(vec.x, vec.y, vec.z);
   }

   public Vec3d computePosition(Vector3d vec) {
      return new Vec3d(vec.x, vec.y, vec.z);
   }

   public boolean isValid(Entity entity, boolean flag, float f) {
      if (entity instanceof PersistentProjectileEntity persistentProjectileEntity && persistentProjectileEntity.isInGround()) {
         return false;
      } else {
         if (entity instanceof ArrowEntity) {
            this.updateEntity(entity, 0.05F, 0.6, f);
         } else if (entity instanceof EnderPearlEntity || entity instanceof SnowballEntity || entity instanceof EggEntity) {
            this.updateEntity(entity, 0.03, 0.8, f);
         } else if (entity instanceof TridentEntity) {
            this.updateEntity(entity, 0.05F, 0.99, f);
         } else if (entity instanceof ExperienceBottleEntity) {
            this.updateEntity(entity, 0.07, 0.8, f);
         } else if (entity instanceof ThrownEntity) {
            this.updateEntity(entity, 0.05, 0.8, f);
         } else if (!(entity instanceof WitherSkullEntity) && !(entity instanceof FireballEntity) && !(entity instanceof DragonFireballEntity)) {
            if (!flag || !(entity instanceof TntEntity)) {
               return false;
            }

            this.updateEntity(entity, 0.05F, 0.8, f);
         } else {
            this.updateEntity(entity, 0.0, 0.8, f);
         }

         if (entity.hasNoGravity()) {
            this.gravity = 0.0;
         }

         return true;
      }
   }

   public void updateEntity(Entity entity, double d, double d2, double d3, double d4, double d5, boolean flag, boolean flag2, float f) {
      this.position.set(this.getVector3d(entity.getLerpedPos(f))).add(0.0, entity.getEyeHeight(entity.getPose()), 0.0);
      double interpolated = MathHelper.lerp(f, entity.prevYaw, entity.getYaw());
      double interpolated2 = MathHelper.lerp(f, entity.prevPitch, entity.getPitch());
      double d6;
      double d7;
      double d8;
      if (d3 == 0.0) {
         d6 = -Math.sin(interpolated * 0.017453292) * Math.cos(interpolated2 * 0.017453292);
         d7 = -Math.sin((interpolated2 + d) * 0.017453292);
         d8 = Math.cos(interpolated * 0.017453292) * Math.cos(interpolated2 * 0.017453292);
      } else {
         Vec3d oppositeRotationVector = entity.getOppositeRotationVector(1.0F);
         Quaterniond quaterniond = new Quaterniond().setAngleAxis(d3, oppositeRotationVector.x, oppositeRotationVector.y, oppositeRotationVector.z);
         Vec3d rotationVec = entity.getRotationVec(1.0F);
         Vector3d vec = new Vector3d(rotationVec.x, rotationVec.y, rotationVec.z);
         vec.rotate(quaterniond);
         d6 = vec.x;
         d7 = vec.y;
         d8 = vec.z;
      }

      this.velocity.set(d6, d7, d8).normalize().mul(d2);
      if (flag) {
         Vec3d velocity2 = entity.getVelocity();
         double d9 = !entity.isOnGround() && !flag2 ? velocity2.y : 0.0;
         this.velocity.add(velocity2.x, d9, velocity2.z);
      }

      this.gravity = d4;
      this.drag = 0.99;
      this.waterDrag = d5;
   }

   public void updateEntity(Entity entity, float f) {
      double interpolated = MathHelper.lerp(f, entity.prevYaw, entity.getYaw());
      double interpolated2 = MathHelper.lerp(f, entity.prevPitch, entity.getPitch());
      double d = Math.cos(-interpolated * (float) (Math.PI / 180.0) - (float) Math.PI);
      double d2 = Math.sin(-interpolated * (float) (Math.PI / 180.0) - (float) Math.PI);
      double d3 = -Math.cos(-interpolated2 * (float) (Math.PI / 180.0));
      double d4 = Math.sin(-interpolated2 * (float) (Math.PI / 180.0));
      this.position.set(this.getVector3d(entity.getLerpedPos(f))).sub(d2 * 0.3, 0.0, d * 0.3).add(0.0, entity.getEyeHeight(entity.getPose()), 0.0);
      this.velocity.set(-d2, MathHelper.clamp(-(d4 / d3), -5.0, 5.0), -d);
      double len = this.velocity.length();
      this.velocity.mul(0.6 / len + 0.5, 0.6 / len + 0.5, 0.6 / len + 0.5);
      this.gravity = 0.03;
      this.drag = 0.92;
      this.waterDrag = 0.0;
   }

   public boolean isValid(Entity entity, ItemStack stack, double d, boolean flag, boolean flag2, boolean flag3, float f) {
      Item item = stack.getItem();
      if (item instanceof BowItem) {
         double pullProgress = BowItem.getPullProgress(IMinecraft.mc.player.getItemUseTime());
         if (pullProgress <= 0.0) {
            return false;
         }

         this.updateEntity(entity, 0.0, pullProgress * 3.0, d, 0.05F, 0.6, flag, flag3, f);
      } else if (item instanceof CrossbowItem) {
         if (!CrossbowItem.isCharged(stack)) {
            return false;
         }

         double d2 = CrossbowItem.isCharged(stack) ? 0.0 : 0.05F;
         this.updateEntity(entity, 0.0, CrossbowItem.getSpeed(ChargedProjectilesComponent.of(stack)), d, d2, 0.6, flag, flag3, f);
      } else if (item instanceof FishingRodItem) {
         this.updateEntity(entity, f);
      } else if (item instanceof TridentItem) {
         this.updateEntity(entity, 0.0, 2.5, d, 0.05F, 0.99, flag, flag3, f);
      } else if (item instanceof SnowballItem || item instanceof EggItem || item instanceof EnderPearlItem) {
         this.updateEntity(entity, 0.0, 1.5, d, 0.03, 0.8, flag, flag3, f);
      } else if (item instanceof ExperienceBottleItem) {
         this.updateEntity(entity, -20.0, 0.7, d, 0.07, 0.8, flag, flag3, f);
      } else if (item instanceof ThrowablePotionItem) {
         this.updateEntity(entity, -20.0, 0.5, d, 0.05, 0.8, flag, flag3, f);
      } else {
         if (!flag2 || !item.equals(Items.TNT)) {
            return false;
         }

         this.updateEntity(entity, 0.0, 1.5, d, 0.05, 0.8, flag, flag3, f);
      }

      return true;
   }

   public void updateEntity(Entity entity, double d, double d2, float f) {
      this.position.set(entity.getLerpedPos(f).toVector3f());
      Vec3d velocity2 = entity.getVelocity();
      this.velocity.set(velocity2.x, velocity2.y, velocity2.z);
      this.gravity = d;
      this.drag = 0.99;
      this.waterDrag = d2;
   }

   public boolean isValid() {
      mutable.set(this.position.x, this.position.y, this.position.z);
      FluidState fluidState = IMinecraft.mc.world.getFluidState(mutable);
      return fluidState.getFluid() != Fluids.WATER && fluidState.getFluid() != Fluids.FLOWING_WATER ? false : this.position.y - (int)this.position.y <= fluidState.getHeight();
   }

   public HitResult raycast() {
      Vec3d vec = this.computePosition(vectorValue);
      ClientWorld world = IMinecraft.mc.world;
      Vec3d vec2 = this.computePosition(vector);
      FluidHandling fluidHandling = this.waterDrag != 0.0 ? FluidHandling.NONE : FluidHandling.ANY;
      BlockHitResult hitResult = world.raycast(new RaycastContext(vec, vec2, ShapeType.COLLIDER, fluidHandling, IMinecraft.mc.player));
      if (hitResult.getType() != Type.MISS) {
         vec = hitResult.getPos();
      }

      EntityHitResult entityCollision = ProjectileUtil.getEntityCollision(
         IMinecraft.mc.world,
         IMinecraft.mc.player,
         vec,
         this.computePosition(vector),
         new Box(this.position.x, this.position.y, this.position.z, this.position.x, this.position.y, this.position.z)
            .stretch(IMinecraft.mc.player.getVelocity())
            .expand(1.0),
         ProjectileSimulator::isValidTarget
      );
      return (HitResult)(entityCollision == null ? hitResult : entityCollision);
   }

   public static boolean isValidTarget(Entity entity) {
      return !entity.isSpectator() && entity.isAlive() && entity.canHit();
   }

   public HitResult raycastHit() {
      vectorValue.set(this.position);
      this.position.add(this.velocity);
      this.velocity.mul(this.isValid() ? this.waterDrag : this.drag);
      this.velocity.sub(0.0, this.gravity, 0.0);
      if (this.position.y < IMinecraft.mc.world.getBottomY()) {
         return new MissHitResult(this.computePosition(this.position));
      }

      int n = (int)(this.position.x / 16.0);
      int n2 = (int)(this.position.z / 16.0);
      if (!IMinecraft.mc.world.getChunkManager().isChunkLoaded(n, n2)) {
         return new MissHitResult(this.computePosition(this.position));
      }

      vector.set(this.position);
      HitResult hitResult = this.raycast();
      return hitResult.getType() == Type.MISS ? null : hitResult;
   }
}
