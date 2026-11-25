package dev.southside.module.impl.render;

import cc.polyfrost.oneconfig.config.core.OneColor;
import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.events.CameraEvent;
import dev.southside.event.events.ClientWorldEvent;
import dev.southside.event.events.DrawContextEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.values.BooleanValue;
import dev.southside.render.ProjectionUtils;
import dev.southside.utils.BoxMeshRenderer;
import dev.southside.utils.ColorValue;
import dev.southside.utils.EnchantmentUtils;
import dev.southside.utils.NumberValue;
import dev.southside.utils.ObjectPool;
import dev.southside.utils.ProjectileSimulator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
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
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Direction.Axis;
import org.joml.Vector3d;

public class Trajectories extends Module {
   private static final double DEFAULT_DOUBLE = Math.toRadians(10.0);
   @SerializedName("otherPlayers")
   public final BooleanValue otherPlayers = new BooleanValue("Other Players", true);
   @SerializedName("firedProjectiles")
   public final BooleanValue firedProjectiles = new BooleanValue("Fired Projectiles", false);
   @SerializedName("firedProjectilesOnlyPearl")
   public final BooleanValue firedProjectilesOnlyPearl = new BooleanValue("Fired Projectiles Only Pearl", false);
   @SerializedName("pearlLandingTag")
   public final BooleanValue pearlLandingTag = new BooleanValue("Pearl Landing Tag", true);
   @SerializedName("pearlTagScale")
   public final NumberValue pearlTagScale = new NumberValue("Pearl Tag Scale", 0.8, 0.2, 2.0, 0.1);
   @SerializedName("pearlTagBgAlpha")
   public final NumberValue pearlTagBgAlpha = new NumberValue("Pearl Tag Bg Alpha", 0.25, 0.05, 1.0, 0.05);
   @SerializedName("accurate")
   public final BooleanValue accurate = new BooleanValue("Accurate", false);
   @SerializedName("tntTrajectories")
   public final BooleanValue tntTrajectories = new BooleanValue("Tnt", true);
   @SerializedName("simulationSteps")
   public final NumberValue simulationSteps = new NumberValue("Simulation Steps", 500, 50, 5000, 1);
   @SerializedName("sideColor")
   public final ColorValue sideColor = new ColorValue("Side Color", true, new OneColor(255, 0, 0, 35));
   @SerializedName("lineColor")
   public final ColorValue lineColor = new ColorValue("Line Color", true, new OneColor(255, 0, 0, 35));
   @SerializedName("simulator")
   public final ProjectileSimulator simulator = new ProjectileSimulator();
   @SerializedName("vectorPool")
   public final ObjectPool<Vector3d> vectorPool = new ObjectPool<>(Vector3d::new);
   @SerializedName("paths")
   private final List<Trajectories.Inner> paths = new ArrayList<>();
   @SerializedName("landingMarkers")
   private final List<Trajectories.Entry> landingMarkers = new ArrayList<>();
   @SerializedName("pearlMarkers")
   private final Map<Integer, Trajectories.Entry> pearlMarkers = new HashMap<>();

   public Trajectories() {
      super(Category.Render, "Trajectories", "Projectile trajectory prediction.");
      this.hideIf(this.firedProjectilesOnlyPearl.getLabel(), () -> !(Boolean)this.firedProjectiles.getValue());
      this.hideIf(this.pearlTagScale.getLabel(), () -> !(Boolean)this.pearlLandingTag.getValue());
      this.hideIf(this.pearlTagBgAlpha.getLabel(), () -> !(Boolean)this.pearlLandingTag.getValue());
   }

   @EventTarget
   public void onCamera(CameraEvent cameraEvent) {
      this.landingMarkers.clear();
      if (mc2.world != null && mc2.player != null) {
         for (PlayerEntity player : mc2.world.getPlayers()) {
            if ((Boolean)this.otherPlayers.getValue() || player == mc2.player) {
               this.performAction(player, cameraEvent.tickDelta);
               this.update(cameraEvent);
            }
         }

         if (!(Boolean)this.firedProjectiles.getValue() && !(Boolean)this.pearlLandingTag.getValue()) {
            this.pearlMarkers.clear();
         } else {
            HashSet set = new HashSet();

            for (Entity entity : mc2.world.getEntities()) {
               if (entity instanceof ProjectileEntity) {
                  boolean flag = entity instanceof EnderPearlEntity;
                  boolean flag2 = (Boolean)this.firedProjectiles.getValue() && (!(Boolean)this.firedProjectilesOnlyPearl.getValue() || flag);
                  boolean flag3 = (Boolean)this.pearlLandingTag.getValue() && flag;
                  if (flag2 || flag3) {
                     if (flag) {
                        set.add(entity.getId());
                     }

                     Trajectories.Entry entry = flag3 ? this.pearlMarkers.get(entity.getId()) : null;
                     if (entry != null) {
                        this.landingMarkers.add(entry);
                        if (!flag2) {
                           continue;
                        }
                     }

                     this.updateEntity(entity, cameraEvent.tickDelta);
                     if (flag2) {
                        this.update(cameraEvent);
                     }

                     if (flag3 && entry == null) {
                        this.performAction((EnderPearlEntity)entity);
                     }
                  }
               }
            }

            this.pearlMarkers.keySet().removeIf(integer -> !set.contains(integer));
         }
      } else {
         this.clear();
      }
   }

   private void updateEntity(Entity entity, float f) {
      this.forEach();
      if (this.simulator.isValid(entity, (Boolean)this.tntTrajectories.getValue(), f)) {
         this.getInner().performAction();
      }
   }

   private void performAction(PlayerEntity player, float f) {
      this.forEach();
      ItemStack mainHandStack = player.getMainHandStack();
      if (mainHandStack.isEmpty() || !isValid(mainHandStack.getItem())) {
         mainHandStack = player.getOffHandStack();
      }

      if (!mainHandStack.isEmpty() && isValid(mainHandStack.getItem()) && this.isValid(player, mainHandStack, 0.0, f)) {
         if (mainHandStack.getItem() instanceof CrossbowItem
            && EnchantmentUtils.computeInt(Enchantments.MULTISHOT, mainHandStack) > 0
            && this.isValid(player, mainHandStack, DEFAULT_DOUBLE, f)) {
            this.isValid(player, mainHandStack, -DEFAULT_DOUBLE, f);
         }
      }
   }

   private boolean isValid(PlayerEntity player, ItemStack stack, double d, float f) {
      if (!this.simulator.isValid(player, stack, d, (Boolean)this.accurate.getValue(), (Boolean)this.tntTrajectories.getValue(), false, f)) {
         return false;
      }

      this.getInner().performAction();
      return true;
   }

   private static boolean isValid(Item item) {
      return item instanceof BowItem
         || item instanceof CrossbowItem
         || item instanceof FishingRodItem
         || item instanceof TridentItem
         || item instanceof SnowballItem
         || item instanceof EggItem
         || item instanceof EnderPearlItem
         || item instanceof ExperienceBottleItem
         || item instanceof ThrowablePotionItem
         || item == Items.TNT;
   }

   private Trajectories.Inner getInner() {
      for (Trajectories.Inner inner : this.paths) {
         if (inner.points.isEmpty()) {
            return inner;
         }
      }

      Trajectories.Inner inner2 = new Trajectories.Inner();
      this.paths.add(inner2);
      return inner2;
   }

   private void update(CameraEvent cameraEvent) {
      for (Trajectories.Inner inner : this.paths) {
         inner.setProgress(cameraEvent.tickDelta);
      }
   }

   private void forEach() {
      this.paths.forEach(Trajectories.Inner::performActionAlt);
   }

   private void performAction(EnderPearlEntity enderPearlEntity) {
      if (!this.paths.isEmpty() && this.paths.getFirst().impactPosition != null) {
         Vector3d impactPosition = this.paths.getFirst().impactPosition;
         Entity owner = enderPearlEntity.getOwner();
         Trajectories.Entry entry = new Trajectories.Entry(
            owner == null ? "Unknown" : owner.getName().getString(),
            new Vec3d(impactPosition.x, impactPosition.y, impactPosition.z),
            MathHelper.floor(impactPosition.x),
            MathHelper.floor(impactPosition.y),
            MathHelper.floor(impactPosition.z)
         );
         this.pearlMarkers.put(enderPearlEntity.getId(), entry);
         this.landingMarkers.add(entry);
      }
   }

   @EventTarget
   public void onDrawContext(DrawContextEvent drawContextEvent) {
      if ((Boolean)this.pearlLandingTag.getValue() && mc2.world != null && mc2.player != null) {
         for (Trajectories.Entry entry : this.landingMarkers) {
            Vec3d position = entry.position;
            Vec2f vec2f = ProjectionUtils.getVec2f(position.x, position.y + 0.25, position.z, 0.0, 0.0);
            if (vec2f != null) {
               this.renderPearlLandingTag(drawContextEvent.getDrawContext(), vec2f, entry);
            }
         }
      }
   }

   private void renderPearlLandingTag(DrawContext context, Vec2f vec2, Trajectories.Entry entry) {
      String text = entry.ownerName + " Pearl";
      short s = -8714;
      int blockZ = entry.blockZ;
      int blockY = entry.blockY;
      int blockX = entry.blockX;
      short s2 = -5743;
      String text2 = "XYZ: " + blockX + ", " + blockY + ", " + blockZ;
      int n = Math.max(mc2.textRenderer.getWidth(text), mc2.textRenderer.getWidth(text2)) + 8;
      int n2 = -n / 2;
      float clamped = MathHelper.clamp(((Number)this.pearlTagBgAlpha.getValue()).floatValue(), 0.0F, 1.0F);
      int n3 = (int)(clamped * 255.0F) << 24;
      float f = ((Number)this.pearlTagScale.getValue()).floatValue();
      MatrixStack matrices = context.getMatrices();
      matrices.push();

      try {
         matrices.translate(vec2.x, vec2.y, 0.0F);
         matrices.scale(f, f, 1.0F);
         context.fill(n2, -28, n2 + n, -2, n3);
         context.drawText(mc2.textRenderer, text, n2 + (n - mc2.textRenderer.getWidth(text)) / 2, -26, -1, true);
         context.drawText(mc2.textRenderer, text2, n2 + (n - mc2.textRenderer.getWidth(text2)) / 2, -15, -1, true);
      } finally {
         matrices.pop();
      }
   }

   @EventTarget
   public void onClientWorld(ClientWorldEvent clientWorldEvent) {
      this.clear();
   }

   @Override
   public void onDisable() {
      this.clear();
      super.onDisable();
   }

   private void clear() {
      this.forEach();
      this.landingMarkers.clear();
      this.pearlMarkers.clear();
   }

   private record Entry(String ownerName, Vec3d position, int blockX, int blockY, int blockZ) {

      public String getOwnerName() {
         return this.ownerName;
      }

      public Vec3d getPosition() {
         return this.position;
      }

      public int getBlockX() {
         return this.blockX;
      }

      public int getBlockY() {
         return this.blockY;
      }

      public int getBlockZ() {
         return this.blockZ;
      }
   }

   private final class Inner {
      @SerializedName("points")
      public final List<Vector3d> points = new ArrayList<>();
      @SerializedName("impactFace")
      private Vec3d[] impactFace;
      @SerializedName("hitEntity")
      private Entity hitEntity;
      @SerializedName("impactPosition")
      public Vector3d impactPosition;

      public Inner() {
      }

      public void performAction() {
         this.performActionEx();
         int clamped = MathHelper.clamp(((Number)Trajectories.this.simulationSteps.getValue()).intValue(), 50, 5000);

         for (int i = 0; i < clamped; i++) {
            HitResult hitResult = Trajectories.this.simulator.raycastHit();
            if (hitResult != null) {
               this.performAction(hitResult);
               return;
            }

            this.performActionEx();
         }
      }

      private void performAction(HitResult hitResult) {
         Vec3d pos = hitResult.getPos();
         this.impactPosition = new Vector3d(pos.x, pos.y, pos.z);
         if (hitResult instanceof BlockHitResult hitResult2 && hitResult.getType() == Type.BLOCK) {
            Axis axis = hitResult2.getSide().getAxis();
            double d = pos.x - (axis == Axis.X ? 0.0 : 0.25);
            double d2 = pos.y - (axis == Axis.Y ? 0.0 : 0.25);
            double d3 = pos.z - (axis == Axis.Z ? 0.0 : 0.25);
            double d4 = pos.x + (axis == Axis.X ? 0.0 : 0.25);
            double d5 = pos.y + (axis == Axis.Y ? 0.0 : 0.25);
            double d6 = pos.z + (axis == Axis.Z ? 0.0 : 0.25);
            this.impactFace = axis == Axis.Y
               ? new Vec3d[]{new Vec3d(d, d2, d3), new Vec3d(d, d2, d6), new Vec3d(d4, d2, d6), new Vec3d(d4, d2, d3)}
               : new Vec3d[]{new Vec3d(d, d2, d3), new Vec3d(d, d5, d3), new Vec3d(d4, d5, d6), new Vec3d(d4, d2, d6)};
            this.points.add(Trajectories.this.vectorPool.getT().set(pos.x, pos.y, pos.z));
         } else if (hitResult instanceof EntityHitResult entityHit) {
            this.hitEntity = entityHit.getEntity();
            this.points.add(Trajectories.this.vectorPool.getT().set(pos.x, pos.y + this.hitEntity.getHeight() / 2.0, pos.z));
         }
      }

      private void performActionEx() {
         this.points.add(Trajectories.this.vectorPool.getT().set(Trajectories.this.simulator.position));
      }

      public void setProgress(float f) {
         int n = ((OneColor)Trajectories.this.lineColor.getValue()).fromHsb();

         for (int i = 1; i < this.points.size(); i++) {
            Vector3d vec = this.points.get(i - 1);
            Vector3d vec2 = this.points.get(i);
            BoxMeshRenderer.renderMeshValue
               .performAction(
                  BoxMeshRenderer.renderMeshValue.getMesh(vec.x, vec.y, vec.z).getMesh(n).getHeight(),
                  BoxMeshRenderer.renderMeshValue.getMesh(vec2.x, vec2.y, vec2.z).getMesh(n).getHeight()
               );
         }

         if (this.impactFace != null) {
            this.onKeyTyped(n);
         }

         if (this.hitEntity != null && this.hitEntity != Trajectories.mc2.player) {
            Box box = this.hitEntity
               .getBoundingBox()
               .offset(
                  (this.hitEntity.getX() - this.hitEntity.prevX) * f,
                  (this.hitEntity.getY() - this.hitEntity.prevY) * f,
                  (this.hitEntity.getZ() - this.hitEntity.prevZ) * f
               );
            BoxMeshRenderer.renderBox(box, ((OneColor)Trajectories.this.lineColor.getValue()).blendColor());
            BoxMeshRenderer.renderBoxEx(box, ((OneColor)Trajectories.this.sideColor.getValue()).blendColor());
         }
      }

      private void onKeyTyped(int n) {
         int[] values = new int[4];
         int[] values2 = new int[4];
         int n2 = ((OneColor)Trajectories.this.sideColor.getValue()).fromHsb();

         for (int i = 0; i < 4; i++) {
            Vec3d vec = this.impactFace[i];
            values[i] = BoxMeshRenderer.renderMeshValue.getMesh(vec.x, vec.y, vec.z).getMesh(n).getHeight();
            values2[i] = BoxMeshRenderer.renderMesh.getMesh(vec.x, vec.y, vec.z).getMesh(n2).getHeight();
         }

         for (int j = 0; j < 4; j++) {
            BoxMeshRenderer.renderMeshValue.performAction(values[j], values[(j + 1) % 4]);
         }

         BoxMeshRenderer.renderMesh.performAction(values2[0], values2[1], values2[2], values2[3]);
      }

      public void performActionAlt() {
         Trajectories.this.vectorPool.forEach(this.points);
         this.points.clear();
         this.impactFace = null;
         this.hitEntity = null;
         this.impactPosition = null;
      }
   }
}
