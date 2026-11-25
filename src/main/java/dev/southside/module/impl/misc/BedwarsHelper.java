package dev.southside.module.impl.misc;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.ClientWorldEvent;
import dev.southside.event.events.StateEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.BedTracker;
import dev.southside.utils.IText;
import dev.southside.utils.player.ReportUtils;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BedPart;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public class BedwarsHelper extends Module {
   private static final int DEFAULT_INT = 3;
   private static final double DEFAULT_DOUBLE = 16.0;
   @SerializedName("debug")
   public final BooleanValue debug = new BooleanValue("Debug", false);
   @SerializedName("autoINC")
   public final BooleanValue autoINC = new BooleanValue("AutoINC", false);
   @SerializedName("antiBreakBed")
   public final BooleanValue antiBreakBed = new BooleanValue("AntiBreakBed", false);
   @SerializedName("reportedIntruders")
   private final Set<String> reportedIntruders = new HashSet<>();
   @SerializedName("reportedBedBreakers")
   private final Set<String> reportedBedBreakers = new HashSet<>();

   public BedwarsHelper() {
      super(Category.Misc, "BedwarsHelper", "Bedwars helper");
   }

   private Set<BlockPos> getSet(BlockPos pos) {
      LinkedHashSet linkedHashSet = new LinkedHashSet();
      BlockState blockState = mc2.world.getBlockState(pos);
      if (!(blockState.getBlock() instanceof BedBlock)) {
         return linkedHashSet;
      }

      BlockPos pos2;
      BlockPos pos3;
      if (blockState.get(BedBlock.PART) == BedPart.HEAD) {
         pos2 = pos;
         pos3 = pos.offset(((Direction)blockState.get(BedBlock.FACING)).getOpposite());
      } else {
         pos3 = pos;
         pos2 = pos.offset((Direction)blockState.get(BedBlock.FACING));
      }

      List list = List.of(pos2.toImmutable(), pos3.toImmutable());

      for (Object obj : list) {
         BlockPos pos4 = (BlockPos)obj;
         short s = 10048;
         linkedHashSet.add(pos4.up());
         long l = 3423401855522993184L;

         for (int i = -3; i <= 3; i++) {
            for (int j = -3; j <= 3; j++) {
               if (i != 0 || j != 0) {
                  BlockPos pos5 = pos4.add(i, 0, j);
                  if (!list.contains(pos5)) {
                     linkedHashSet.add(pos5);
                  }
               }
            }
         }
      }

      linkedHashSet.removeIf(obj2 -> mc2.world.getBlockState((BlockPos)obj2).isAir());
      return linkedHashSet;
   }

   private BlockPos raycast(PlayerEntity player) {
      Vec3d eyePos = player.getEyePos();
      Vec3d vec = eyePos.add(player.getRotationVec(1.0F).multiply(4.5));
      BlockHitResult hitResult = mc2.world.raycast(new RaycastContext(eyePos, vec, ShapeType.OUTLINE, FluidHandling.NONE, player));
      return hitResult.getType() == Type.BLOCK ? hitResult.getBlockPos() : null;
   }

   private boolean isValid(BlockPos pos, String text) {
      PlayerEntity player = mc2.world.getPlayers().stream().filter(player2 -> player2.getGameProfile().getName().equals(text)).findFirst().orElse(null);
      return player == null || !player.getBlockPos().isWithinDistance(pos, 16.0);
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      if (stateEvent.getState() == Stage.PRE
         && mc2.player != null
         && mc2.world != null
         && mc2.getNetworkHandler() != null
         && BedTracker.INSTANCE.isValid()) {
         BlockPos bedPosition = BedTracker.INSTANCE.getBedPosition();
         if (bedPosition != null) {
            if ((Boolean)this.autoINC.getValue()) {
               this.updateBlock(bedPosition);
            }

            if ((Boolean)this.antiBreakBed.getValue()) {
               this.performAction(this.getSet(bedPosition));
            }
         }
      }
   }

   private void updateBlock(BlockPos pos) {
      for (PlayerEntity player : mc2.world.getPlayers()) {
         if (player != mc2.player && !Teams.isValidTarget(player) && player.getBlockPos().isWithinDistance(pos, 16.0)) {
            String name = player.getGameProfile().getName();
            if (this.reportedIntruders.add(name)) {
               mc2.getNetworkHandler().sendChatMessage("inc! " + name + " 来我们家了!");
               if ((Boolean)this.debug.getValue()) {
                  IText.applyText("[AutoINC] " + name + " entered bed range");
               }
            }
         }
      }

      this.reportedIntruders.removeIf(text -> this.isValid(pos, text));
   }

   private void performAction(Set<BlockPos> set) {
      for (PlayerEntity player : mc2.world.getPlayers()) {
         if (player != mc2.player && !Teams.isValidTarget(player) && player.handSwinging) {
            String name = player.getGameProfile().getName();
            if (!this.reportedBedBreakers.contains(name)) {
               BlockPos hitResult = this.raycast(player);
               if (hitResult != null && set.contains(hitResult)) {
                  this.reportedBedBreakers.add(name);
                  if ((Boolean)this.debug.getValue()) {
                     IText.applyText("[AntiBreakBed] " + name + " is breaking bed protection at " + hitResult.toShortString() + ", reporting...");
                  }

                  ReportUtils.applyText(name);
               }
            }
         }
      }
   }

   @Override
   public void onDisable() {
      this.reportedIntruders.clear();
      this.reportedBedBreakers.clear();
   }

   @EventTarget
   public void onClientWorld(ClientWorldEvent clientWorldEvent) {
      this.reportedIntruders.clear();
      this.reportedBedBreakers.clear();
   }
}
