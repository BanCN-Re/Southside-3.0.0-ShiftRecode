package dev.southside.module.impl.combat;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.events.DisableRotationEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.ValuePair;
import dev.southside.utils.BoxRaytraceUtils;
import dev.southside.utils.AimPointUtils;
import dev.southside.utils.PacketUtils;
import dev.southside.utils.NumberValue;
import dev.southside.utils.RotationManager;
import dev.southside.utils.BlockLookUtils;
import dev.southside.utils.Rotation;
import dev.southside.utils.rotation.MoveFixMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class AutoCrystal extends Module {
   @SerializedName("range")
   public NumberValue range = new NumberValue("Range", 3.0, 1.0, 8.0, 0.01);
   private static final long DEFAULT_LONG_VALUE = -217141941484125708L;

   public AutoCrystal() {
      super(Category.Combat, "AutoCrystal", "自动打水晶(无搜索/放置)");
   }

   @Override
   public int getHeightEx() {
      return (int)DEFAULT_LONG_VALUE;
   }

   @Override
   public int getHeightAlt() {
      return 2 + this.getHeightEx();
   }

   @EventTarget(priority = 200)
   public void onDisableRotation(DisableRotationEvent disableRotationEvent) {
      if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().world != null) {
         for (Entity entity : Module.mc.world.getEntities()) {
            if (entity instanceof EndCrystalEntity) {
               Vec3d vec = BoxRaytraceUtils.computePosition(Module.mc.player.getEyePos(), entity.getBoundingBox());
               if (Module.mc.player.getEyePos().distanceTo(vec) <= this.range.getCurrentValue()) {
                  Rotation rotation = AimPointUtils.computeRotation(AimPointUtils.getInner(Module.mc.player, entity, 1.0F));
                  if (BlockLookUtils.isHovered(rotation.yaw, rotation.pitch, 3.0, entity.getBoundingBox())) {
                     Rotation rotation2 = RotationManager.computeRotation(Module.mc.player.getEyePos(), vec, false);
                     RotationManager.rotationRequests.add(new ValuePair<>(rotation2, this.getHeightAlt()));
                     RotationManager.moveFixMode = MoveFixMode.Silent;
                     PacketUtils.sendPacketDirect(PlayerInteractEntityC2SPacket.attack(entity, Module.mc.player.isSneaking()));
                     PacketUtils.sendPacketDirect(new HandSwingC2SPacket(Hand.MAIN_HAND));
                  }
                  break;
               }
            }
         }
      }
   }
}
