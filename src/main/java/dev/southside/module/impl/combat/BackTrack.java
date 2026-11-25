package dev.southside.module.impl.combat;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.events.CameraEvent;
import dev.southside.event.events.DrawContextEvent;
import dev.southside.event.events.MinecraftClientEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.OneConfigFonts;
import dev.southside.utils.BoxMeshRenderer;
import dev.southside.utils.NanoVGHelper;
import dev.southside.utils.ServerPacketManager;
import java.awt.Color;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.util.math.Box;

public class BackTrack extends Module {
   @SerializedName("backtracking")
   private boolean backtracking;

   public BackTrack() {
      super(Category.Combat, "BackTrack");
   }

   public void releaseNonVelocity() {
      if (!Velocity.activeValue) {
         ServerPacketManager.performActionImpl();
      }
   }

   @EventTarget
   public void onMinecraftClient(MinecraftClientEvent minecraftClientEvent) {
      if (KillAura.target instanceof OtherClientPlayerEntity otherClientPlayerEntity
         && Module.mc.player != null
         && Module.mc.player.getPos().distanceTo(otherClientPlayerEntity.getPos()) <= 4.7
         && Southside.INSTANCE.getModuleManager().getModule(KillAura.class).isValidTarget(KillAura.target)) {
         if (!this.backtracking) {
            ServerPacketManager.performAction();
            this.backtracking = true;
         }
      } else {
         if (this.backtracking) {
            this.backtracking = false;
            this.releaseNonVelocity();
         }
      }
   }

   @EventTarget
   public void onCamera(CameraEvent cameraEvent) {
      if (this.backtracking && ServerPacketManager.active && KillAura.target != null) {
         Box boxAt = KillAura.target.dimensions.getBoxAt(KillAura.target.getPos());
         BoxMeshRenderer.renderMeshValue.renderBoxEx(boxAt, Color.BLUE);
      }
   }

   @EventTarget
   public void onDrawContext(DrawContextEvent drawContextEvent) {
      if (this.backtracking && ServerPacketManager.active && KillAura.target != null && Module.mc.player != null) {
         NanoVGHelper nvg = NanoVGHelper.INSTANCE;
         nvg.performAction(true, l -> performAction(nvg, l));
      }
   }

   private static void performAction(NanoVGHelper nvg, long l) {
      double dist = Module.mc.player.getPos().distanceTo(KillAura.target.getPos());
      String text = "Tracking Ticks: 0 | dist: " + dist;
      float textWidth = nvg.getTextWidth(l, text, 8.0F, OneConfigFonts.REGULAR);
      float f = (Module.mc.getWindow().getScaledWidth() - textWidth) / 2.0F;
      float f2 = Module.mc.getWindow().getScaledHeight() / 2.0F + 50.0F;
      nvg.draw(l, text, f, f2, Color.YELLOW.getRGB(), 8.0F, OneConfigFonts.REGULAR);
   }
}
