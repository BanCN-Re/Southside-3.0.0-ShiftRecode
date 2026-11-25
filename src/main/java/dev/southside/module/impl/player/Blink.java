package dev.southside.module.impl.player;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.TransferOrigin;
import dev.southside.event.events.ClientWorldEvent;
import dev.southside.event.events.DirectionEvent;
import dev.southside.event.events.StateEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.PacketUtils;
import dev.southside.utils.NumberValue;
import dev.southside.utils.FakePlayerEntity;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookAndOnGround;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public class Blink extends Module {
   @SerializedName("maxPackets")
   public final NumberValue maxPackets = new NumberValue("Max Packets", 1, 1, 200, 1);
   public static final BooleanValue slowRelease = new BooleanValue("Slow Release", false);
   @SerializedName("ignoreKeepAlive")
   public final BooleanValue ignoreKeepAlive = new BooleanValue("Ignore KeepAlive", false);
   @SerializedName("antiAim")
   public final BooleanValue antiAim = new BooleanValue("Anti Aim", true);
   @SerializedName("arrow")
   public final BooleanValue arrow = new BooleanValue("Arrow", true);
   @SerializedName("projectile")
   public final BooleanValue projectile = new BooleanValue("Projectile", true);
   @SerializedName("tnt")
   public final BooleanValue tnt = new BooleanValue("TNT", true);
   @SerializedName("player")
   public final BooleanValue player = new BooleanValue("Player", true);
   @SerializedName("packetGroups")
   private final LinkedList<List<Packet<?>>> packetGroups = new LinkedList<>();
   @SerializedName("positionHistory")
   private final List<Vec3d> positionHistory = new ArrayList<>();
   @SerializedName("fakePlayer")
   private FakePlayerEntity fakePlayer;
   @SerializedName("bufferedTicks")
   private int bufferedTicks;

   public Blink() {
      super(Category.Player, "Blink", "666开瞬移了。。");
   }

   @Override
   public void onEnable() {
      if (mc2.player != null && mc2.world != null) {
         this.packetGroups.clear();
         this.packetGroups.add(new ArrayList<>());
         mc2.player.getAbilities().allowModifyWorld = true;
         mc2.player.onGameModeChanged(GameMode.SURVIVAL);
         this.fakePlayer = new FakePlayerEntity(mc2.player, mc2.player.getGameProfile().getName(), 20.0F, true);
         this.fakePlayer.spawned = true;
         this.fakePlayer.targeted = true;
         this.fakePlayer.addEntity();
         this.bufferedTicks = 0;
         this.positionHistory.add(mc2.player.getPos());
         super.onEnable();
      } else {
         this.setEnabled(false);
      }
   }

   @Override
   public void onDisable() {
      while (!this.packetGroups.isEmpty()) {
         this.performActionAlt();
      }

      this.positionHistory.clear();
      this.bufferedTicks = 0;
      if (this.fakePlayer != null) {
         if (mc2.world != null) {
            mc2.world.removeEntity(this.fakePlayer.getId(), RemovalReason.DISCARDED);
         }

         this.fakePlayer = null;
      }

      super.onDisable();
   }

   @EventTarget
   public void onDirection(DirectionEvent directionEvent) {
      if (directionEvent.getDirection() == TransferOrigin.SEND && (!(Boolean)this.ignoreKeepAlive.getValue() || !(directionEvent.getPacket() instanceof KeepAliveC2SPacket))) {
         if (this.packetGroups.isEmpty()) {
            this.packetGroups.add(new ArrayList<>());
         }

         this.packetGroups.getLast().add(directionEvent.getPacket());
         directionEvent.setCancelled(true);
      }
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      if (mc2.player != null && mc2.world != null) {
         this.bufferedTicks++;
         this.packetGroups.add(new ArrayList<>());
         this.positionHistory.add(mc2.player.getPos());
         if (this.packetGroups.size() >= ((Number)this.maxPackets.getValue()).intValue() || this.bufferedTicks % 10 == 0 && (Boolean)slowRelease.getValue()) {
            this.performActionAlt();
         }

         if ((Boolean)this.antiAim.getValue()) {
            while (this.packetGroups.size() >= 3 && this.poll()) {
               this.performActionAlt();
            }
         }
      } else {
         this.setEnabled(false);
      }
   }

   @EventTarget
   public void onClientWorld(ClientWorldEvent clientWorldEvent) {
      this.setEnabled(false);
   }

   private void performActionAlt() {
      if (!this.packetGroups.isEmpty()) {
         for (Packet packet : this.packetGroups.removeFirst()) {
            PacketUtils.sendPacketDirect(packet);
            this.handlePacket(packet);
         }

         if (!this.positionHistory.isEmpty()) {
            this.positionHistory.removeFirst();
         }
      }
   }

   private boolean poll() {
      if (this.positionHistory.isEmpty()) {
         return false;
      }

      for (Entity entity : mc2.world.getEntities()) {
         if (this.isValidTarget(entity) && this.isValidTargetEx(entity)) {
            return true;
         }
      }

      return false;
   }

   private boolean isValidTarget(Entity entity) {
      return (Boolean)this.arrow.getValue() && entity instanceof ArrowEntity arrowEntity && !arrowEntity.isInGround()
         || (Boolean)this.projectile.getValue() && (entity instanceof SnowballEntity || entity instanceof EggEntity)
         || (Boolean)this.tnt.getValue() && entity instanceof TntEntity
         || (Boolean)this.player.getValue()
            && entity instanceof PlayerEntity
            && this.fakePlayer != null
            && entity.getId() != this.fakePlayer.getId()
            && entity.getId() != mc2.player.getId();
   }

   private boolean isValidTargetEx(Entity entity) {
      if (entity instanceof ProjectileEntity projectileEntity) {
         return this.isBlockVisible(projectileEntity);
      } else {
         Vec3d first = this.positionHistory.getFirst();
         return entity instanceof TntEntity tntEntity
            ? tntEntity.squaredDistanceTo(first) <= 30.0 && tntEntity.getFuse() <= 10
            : entity instanceof PlayerEntity player2 && player2.squaredDistanceTo(first) <= 25.0 && !player2.getUuid().equals(mc2.player.getUuid());
      }
   }

   private boolean isBlockVisible(ProjectileEntity projectileEntity) {
      double d = 1.2F;
      double d2 = 0.05F;
      if (!(projectileEntity instanceof ArrowEntity) && (projectileEntity instanceof SnowballEntity || projectileEntity instanceof EggEntity)) {
         d = 0.25;
         d2 = 1.2F;
      }

      double x = projectileEntity.getX();
      double y = projectileEntity.getY();
      double z = projectileEntity.getZ();
      double x2 = projectileEntity.getVelocity().x;
      double y2 = projectileEntity.getVelocity().y;
      double z2 = projectileEntity.getVelocity().z;
      int n = 0;
      int size = this.positionHistory.size();

      for (int i = 0; y > -60.0 && i < this.positionHistory.size() && n < this.positionHistory.size(); i++) {
         Vec3d vec = new Vec3d(x, y, z);
         Vec3d vec2 = new Vec3d(x + x2, y + y2, z + z2);
         BlockHitResult hitResult = mc2.world.raycast(new RaycastContext(vec, vec2, ShapeType.COLLIDER, FluidHandling.NONE, mc2.player));
         if (hitResult != null && hitResult.getType() == Type.BLOCK) {
            return false;
         }

         Box box = new Box(x - d, y - d, z - d, x + d, y + d, z + d);
         Vec3d vec3 = this.positionHistory.get(n);
         float f = 2.2F;
         float f2 = 0.6F;
         Box box2 = new Box(vec3.x - f2, vec3.y - f / 2.0F, vec3.z - f2, vec3.x + f2, vec3.y + f, vec3.z + f2);
         if (box.intersects(box2)) {
            return true;
         }

         x += x2;
         y += y2;
         z += z2;
         BlockPos pos = new BlockPos((int)x, (int)y, (int)z);
         double d3 = mc2.world.getBlockState(pos).getBlock().getDefaultState().isLiquid() ? 0.6 : 0.99F;
         x2 *= d3;
         y2 *= d3;
         z2 *= d3;
         y2 -= d2;
         size++;
         n += getReleaseSpeedModifier(i + 1, size - n);
      }

      return false;
   }

   private static int getReleaseSpeedModifier(int n, int n2) {
      return n2 < 195 && (!slowRelease.getValue() || n % 10 != 0) ? 0 : 1;
   }

   private void handlePacket(Packet<?> packet) {
      if (this.fakePlayer != null) {
         if (packet instanceof PositionAndOnGround positionAndOnGround) {
            this.fakePlayer.setPosition(positionAndOnGround.x, positionAndOnGround.y, positionAndOnGround.z);
            this.fakePlayer.setOnGround(positionAndOnGround.isOnGround());
         } else if (packet instanceof LookAndOnGround lookAndOnGround) {
            this.fakePlayer.setHeadYaw(lookAndOnGround.getYaw(this.fakePlayer.getYaw()));
            this.fakePlayer.setYaw(lookAndOnGround.getYaw(this.fakePlayer.getYaw()));
            this.fakePlayer.setPitch(lookAndOnGround.pitch);
            this.fakePlayer.setOnGround(lookAndOnGround.isOnGround());
         } else if (packet instanceof Full full) {
            this.fakePlayer.setPosition(full.x, full.y, full.z);
            this.fakePlayer.setYaw(full.yaw);
            this.fakePlayer.setHeadYaw(full.getYaw(this.fakePlayer.getYaw()));
            this.fakePlayer.setPitch(full.pitch);
            this.fakePlayer.setOnGround(full.isOnGround());
         } else if (packet instanceof ClientCommandC2SPacket clientCommandC2SPacket) {
            switch (clientCommandC2SPacket.getMode()) {
               case START_SPRINTING:
                  this.fakePlayer.setSprinting(true);
                  break;
               case STOP_SPRINTING:
                  this.fakePlayer.setSprinting(false);
                  break;
               case PRESS_SHIFT_KEY:
                  this.fakePlayer.setSneaking(true);
                  break;
               case RELEASE_SHIFT_KEY:
                  this.fakePlayer.setSneaking(false);
            }
         } else if (packet instanceof HandSwingC2SPacket handSwingC2SPacket) {
            this.fakePlayer.swingHand(handSwingC2SPacket.getHand());
         }
      }
   }
}
