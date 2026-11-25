package dev.southside.module.impl.movement;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.events.ClientWorldEvent;
import dev.southside.event.events.DisableRotationEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.LoadedBlockEntityIterator;
import dev.southside.utils.PacketUtils;
import dev.southside.utils.IText;
import dev.southside.utils.misc.REDType;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.BedPart;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

public class TP2Bed extends Module {
   private static final int DEFAULT_INT = 5;
   private static TP2Bed INSTANCE;
   @SerializedName("autoDisable")
   public final BooleanValue autoDisable = new BooleanValue("Auto Disable", true);
   @SerializedName("ignoreOwnBed")
   public final BooleanValue ignoreOwnBed = new BooleanValue("Ignore Own Bed", true);
   @SerializedName("debug")
   public final BooleanValue debug = new BooleanValue("Debug", false);
   @SerializedName("session")
   private TP2Bed.Entry session;
   @SerializedName("targetPosition")
   private Vec3d targetPosition;
   @SerializedName("graceTicks")
   private int graceTicks;
   @SerializedName("wasFlying")
   private boolean wasFlying;
   @SerializedName("wasSpectating")
   private boolean wasSpectating;
   @SerializedName("awaitingRespawnPosition")
   private boolean awaitingRespawnPosition;
   @SerializedName("skippedOwnBeds")
   private int skippedOwnBeds;
   @SerializedName("lastOwnTeam")
   private REDType lastOwnTeam;

   public TP2Bed() {
      super(Category.Movement, "TP2Bed", "旁观或飞行结束后传送到已加载区块中最近的床");
   }

   @Override
   public void onEnable() {
      INSTANCE = this;
      this.performActionAlt();
      this.performActionImpl();
      this.awaitingRespawnPosition = Module.mc.player == null || Module.mc.player.isDead();
   }

   @Override
   public void onDisable() {
      if (INSTANCE == this) {
         INSTANCE = null;
      }

      this.performActionAlt();
   }

   private void performActionAlt() {
      this.session = null;
      this.targetPosition = null;
      this.graceTicks = 0;
      this.wasFlying = false;
      this.wasSpectating = false;
   }

   private void performActionImpl() {
      ClientPlayNetworkHandler networkHandler = Module.mc.getNetworkHandler();
      if (networkHandler != null && Module.mc.player != null && Module.mc.world != null) {
         this.wasFlying = Module.mc.player.getAbilities().flying;
         this.wasSpectating = Module.mc.player.isSpectator();
         this.session = new TP2Bed.Entry(networkHandler, Module.mc.player, Module.mc.world);
      }
   }

   @EventTarget
   public void onClientWorld(ClientWorldEvent clientWorldEvent) {
      this.performActionAlt();
      this.awaitingRespawnPosition = true;
   }

   public BlockPos findBlockPos() {
      TP2Bed.Entry session2 = this.session;
      if (session2 != null && session2.isValid()) {
         boolean flag = (Boolean)this.ignoreOwnBed.getValue();
         REDType redtype = flag ? REDType.getRedtype2() : null;
         this.lastOwnTeam = redtype;
         this.skippedOwnBeds = 0;
         Vec3d pos = session2.player.getPos();
         BlockPos pos2 = null;
         double positive_infinity = Double.POSITIVE_INFINITY;
         LoadedBlockEntityIterator loadedBlockEntityIterator = new LoadedBlockEntityIterator();

         while (loadedBlockEntityIterator.hasNext()) {
            BlockEntity blockEntity = loadedBlockEntityIterator.next();
            if (blockEntity instanceof BedBlockEntity) {
               BlockState blockState = session2.world.getBlockState(blockEntity.getPos());
               if (blockState.getBlock() instanceof BedBlock) {
                  BlockPos pos3 = blockState.get(BedBlock.PART) == BedPart.HEAD
                     ? blockEntity.getPos().toImmutable()
                     : blockEntity.getPos().offset((Direction)blockState.get(BedBlock.FACING));
                  BlockState blockState2 = session2.world.getBlockState(pos3);
                  int n = pos3.getY() + 1;
                  if (blockState2.getBlock() instanceof BedBlock
                     && blockState2.get(BedBlock.PART) == BedPart.HEAD
                     && n >= session2.world.getBottomY()
                     && n < session2.world.getBottomY() + session2.world.getHeight()
                     && session2.world.getWorldBorder().contains(pos3)) {
                     if (redtype != null && REDType.getRedtype(blockState2) == redtype) {
                        this.skippedOwnBeds++;
                     } else {
                        double dist = pos.squaredDistanceTo(Vec3d.ofCenter(pos3));
                        if (dist < positive_infinity) {
                           positive_infinity = dist;
                           pos2 = pos3;
                        }
                     }
                  }
               }
            }
         }

         return pos2;
      } else {
         return null;
      }
   }

   public void performActionDirect() {
      TP2Bed.Entry session2 = this.session;
      if (session2 != null && session2.isValid() && !this.awaitingRespawnPosition && this.graceTicks <= 0 && !session2.player.isDead()) {
         BlockPos pos = this.findBlockPos();
         if (pos == null) {
            if (this.skippedOwnBeds > 0) {
               IText.performActionEx(Text.literal("[TP2Bed] 只找到本方队伍颜色的床, 已跳过 " + this.skippedOwnBeds + " 张"));
            } else {
               IText.performActionEx(Text.literal("[TP2Bed] 已加载区块中未找到床"));
            }
         } else {
            if ((Boolean)this.debug.getValue()) {
               String text = this.lastOwnTeam == null ? "未知" : this.lastOwnTeam.getChineseName();
               int n = REDType.computeInt();
               String text2 = n < 0 ? "无" : String.format("#%06X", n & 16777215);
               REDType redtype = REDType.getRedtype(session2.world.getBlockState(pos));
               String text3 = redtype == null ? "未知" : redtype.getChineseName();
               String text4 = text3;
               String text5 = text2;
               String text6 = text;
               this.applyText("本方床色: " + text6 + "(皮甲 " + text5 + ") / 目标床色: " + text4);
            }

            Vec3d vec = new Vec3d(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
            this.targetPosition = vec;
            this.graceTicks = 5;
            performAction(vec);
            performAction(session2, vec);
            int x = pos.getX();
            int n2 = pos.getY() + 1;
            int z = pos.getZ();
            int n3 = n2;
            int n4 = x;
            this.applyText("传送到 " + n4 + ", " + n3 + ", " + z);
         }
      }
   }

   public static void performAction(ClientPlayNetworkHandler networkHandler, boolean flag) {
      TP2Bed tp2bed = getTp2bed(networkHandler);
      if (tp2bed != null) {
         boolean flag2 = tp2bed.wasFlying && !flag;
         tp2bed.wasFlying = flag;
         if (flag2) {
            tp2bed.applyText("服务器结束飞行，尝试传送");
            tp2bed.performActionDirect();
         }
      }
   }

   public static void performAction(ClientPlayNetworkHandler networkHandler, GameStateChangeS2CPacket gameStateChangeS2CPacket) {
      if (gameStateChangeS2CPacket.getReason() == GameStateChangeS2CPacket.GAME_MODE_CHANGED) {
         TP2Bed tp2bed = getTp2bed(networkHandler);
         if (tp2bed != null) {
            boolean flag = (int)gameStateChangeS2CPacket.getValue() == GameMode.SPECTATOR.getId();
            boolean flag2 = tp2bed.wasSpectating && !flag;
            tp2bed.wasSpectating = flag;
            if (flag2) {
               tp2bed.applyText("服务器结束旁观，尝试传送");
               tp2bed.performActionDirect();
            }
         }
      }
   }

   public static void performAction(ClientPlayNetworkHandler networkHandler) {
      TP2Bed tp2bed = getTp2bed(networkHandler);
      if (tp2bed != null) {
         tp2bed.performActionAlt();
         tp2bed.awaitingRespawnPosition = true;
      }
   }

   public static boolean isValid(ClientPlayNetworkHandler networkHandler, PlayerPositionLookS2CPacket playerPositionLookS2CPacket) {
      TP2Bed tp2bed = getTp2bed(networkHandler);
      if (tp2bed != null && tp2bed.graceTicks > 0 && tp2bed.targetPosition != null && !tp2bed.session.player.isDead()) {
         PacketUtils.sendPacketDirect(new TeleportConfirmC2SPacket(playerPositionLookS2CPacket.teleportId()));
         performAction(tp2bed.targetPosition);
         performAction(tp2bed.session, tp2bed.targetPosition);
         tp2bed.applyText("保持目标位置，确认传送 " + playerPositionLookS2CPacket.teleportId());
         return true;
      } else {
         return false;
      }
   }

   public static void performActionEx(ClientPlayNetworkHandler networkHandler) {
      TP2Bed tp2bed = getTp2bed(networkHandler);
      if (tp2bed != null) {
         tp2bed.awaitingRespawnPosition = false;
      }
   }

   private static TP2Bed getTp2bed(ClientPlayNetworkHandler networkHandler) {
      TP2Bed tp2bed = INSTANCE;
      if (tp2bed != null && tp2bed.isEnabled() && Module.mc.isOnThread() && Module.mc.getNetworkHandler() == networkHandler) {
         if (tp2bed.session == null || !tp2bed.session.isValid()) {
            tp2bed.performActionAlt();
            tp2bed.performActionImpl();
         }

         return tp2bed.session != null && tp2bed.session.isValid() ? tp2bed : null;
      } else {
         return null;
      }
   }

   @EventTarget
   public void onDisableRotation(DisableRotationEvent disableRotationEvent) {
      TP2Bed.Entry session2 = this.session;
      if (session2 == null || !session2.isValid()) {
         this.performActionAlt();
         this.performActionImpl();
      } else if (session2.player.isDead()) {
         this.awaitingRespawnPosition = true;
         this.targetPosition = null;
         this.graceTicks = 0;
         this.wasFlying = false;
         this.wasSpectating = false;
      } else if (this.graceTicks > 0 && this.targetPosition != null) {
         performAction(session2, this.targetPosition);
         if (--this.graceTicks == 0) {
            this.targetPosition = null;
            this.applyText("位置保持结束");
            if ((Boolean)this.autoDisable.getValue()) {
               this.setEnabled(false);
            }
         }
      }
   }

   private static void performAction(Vec3d vec) {
      PacketUtils.sendPacketDirect(new PositionAndOnGround(vec.x, vec.y, vec.z, true, false));
   }

   private static void performAction(TP2Bed.Entry entry, Vec3d vec) {
      entry.player.setPosition(vec);
      entry.player.setVelocity(Vec3d.ZERO);
   }

   private void applyText(String text) {
      if ((Boolean)this.debug.getValue()) {
         IText.performAction(Text.literal("[TP2Bed] " + text));
      }
   }

   private record Entry(ClientPlayNetworkHandler connection, ClientPlayerEntity player, ClientWorld world) {

      public boolean isValid() {
         return this.connection == Module.mc.getNetworkHandler()
            && this.connection.getConnection().isOpen()
            && this.player == Module.mc.player
            && this.world == Module.mc.world;
      }

      public ClientPlayNetworkHandler getConnection() {
         return this.connection;
      }

      public ClientPlayerEntity getPlayer() {
         return this.player;
      }

      public ClientWorld getWorld() {
         return this.world;
      }
   }
}
