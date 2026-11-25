package dev.southside.module.impl.player;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.events.DisableRotationEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.ModuleManager;
import dev.southside.module.impl.combat.KillAura;
import dev.southside.module.impl.world.Scaffold;
import dev.southside.utils.BoxRaytraceUtils;
import dev.southside.utils.EntityTargetUtils;
import dev.southside.utils.NumberValue;
import dev.southside.utils.BlockRotationHelper;
import dev.southside.utils.RotationManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Blocks;
import net.minecraft.block.CobwebBlock;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoTrap extends Module {
   private static final List<Item> TRAPS = List.of(Items.COBWEB, Items.LAVA_BUCKET);
   @SerializedName("range")
   public final NumberValue range = new NumberValue("Range", 3.0, 3.0, 4.0, 0.01);
   @SerializedName("lavaRetrieve")
   public final NumberValue lavaRetrieve = new NumberValue("LavaRetrieve", 30, 2, 30, 1);
   @SerializedName("placedLavaTicks")
   private final Map<BlockPos, Integer> placedLavaTicks = new HashMap<>();

   public AutoTrap() {
      super(Category.Player, "AutoTrap", "我会trap别人。");
   }

   @Override
   public int getHeightAlt() {
      return -2;
   }

   @EventTarget
   public void onDisableRotation(DisableRotationEvent disableRotationEvent) {
      if (mc2.player != null && mc2.world != null && mc2.interactionManager != null) {
         ModuleManager moduleManager = Southside.INSTANCE.getModuleManager();
         if (!moduleManager.getModule(Scaffold.class).isEnabled() && !moduleManager.getModule(KillAura.class).isEnabled()) {
            this.performActionAlt();
            Entity entity = this.getEntity();
            if (entity != null && entity.isAlive() && !entity.isRemoved() && !(mc2.player.distanceTo(entity) > ((Number)this.range.getValue()).doubleValue())) {
               int n = this.computeInt();
               if (n != -1) {
                  this.updateEntity(entity, n);
               }
            }
         }
      }
   }

   private Entity getEntity() {
      ArrayList<Entity> list = new ArrayList<>();
      Vec3d eyePos = mc2.player.getEyePos();

      for (Entity entity : mc2.world.getEntities()) {
         if (EntityTargetUtils.isLivingEntity(entity)
            && BoxRaytraceUtils.computePosition(eyePos, entity.getBoundingBox()).distanceTo(eyePos) <= ((Number)this.range.getValue()).doubleValue()
            && !entity.isInLava()
            && !(mc2.world.getBlockState(entity.getBlockPos()).getBlock() instanceof CobwebBlock)
            && mc2.world.getBlockState(entity.getBlockPos().down()).isSolid()) {
            list.add(entity);
         }
      }

      list.sort((entity2, entity3) -> (int)(entity2.distanceTo(mc2.player) - entity3.distanceTo(mc2.player)));
      return list.isEmpty() ? null : (Entity)list.getFirst();
   }

   private int computeInt() {
      for (int i = 0; i < 9; i++) {
         if (TRAPS.contains(mc2.player.getInventory().getStack(i).getItem())) {
            return i;
         }
      }

      return -1;
   }

   private void updateEntity(Entity entity, int n) {
      int selectedSlot = mc2.player.getInventory().selectedSlot;
      mc2.player.getInventory().selectedSlot = n;

      try {
         BlockPos pos = entity.getBlockPos().down();
         RotationManager.requestRotation(BlockRotationHelper.computeRotationAlt(pos, Direction.UP), this.getHeightAlt());
         if (mc2.player.getInventory().getStack(n).getItem() == Items.COBWEB) {
            Vec3d vec = new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
            BlockHitResult hitResult = new BlockHitResult(vec, Direction.UP, pos, false);
            mc2.interactionManager.interactBlock(mc2.player, Hand.MAIN_HAND, hitResult);
            mc2.player.swingHand(Hand.MAIN_HAND);
         } else {
            mc2.interactionManager.interactItem(mc2.player, Hand.MAIN_HAND);
            this.placedLavaTicks.put(pos.up(), 0);
         }
      } finally {
         mc2.player.getInventory().selectedSlot = selectedSlot;
      }
   }

   private void performActionAlt() {
      if (!this.placedLavaTicks.isEmpty()) {
         ArrayList list = new ArrayList();
         this.placedLavaTicks.entrySet().removeIf(entry -> {
            BlockPos key = entry.getKey();
            if (mc2.world.getBlockState(key).isOf(Blocks.LAVA) && mc2.world.getFluidState(key).isStill()) {
               if (entry.getValue() >= ((Number)this.lavaRetrieve.getValue()).intValue()) {
                  list.add(key);
                  return true;
               } else {
                  entry.setValue(entry.getValue() + 1);
                  return false;
               }
            } else {
               return true;
            }
         });
         if (!list.isEmpty()) {
            int n = this.computeIntEx();
            if (n != -1) {
               int selectedSlot = mc2.player.getInventory().selectedSlot;
               mc2.player.getInventory().selectedSlot = n;

               try {
                  RotationManager.requestRotation(BlockRotationHelper.computeRotationAlt((BlockPos)list.getFirst(), Direction.UP), 0);
                  ActionResult actionResult = mc2.interactionManager.interactItem(mc2.player, Hand.MAIN_HAND);
                  if (actionResult.isAccepted()) {
                     mc2.player.swingHand(Hand.MAIN_HAND);
                  }
               } finally {
                  mc2.player.getInventory().selectedSlot = selectedSlot;
               }
            }
         }
      }
   }

   private int computeIntEx() {
      for (int i = 0; i < 9; i++) {
         if (mc2.player.getInventory().getStack(i).getItem() == Items.BUCKET) {
            return i;
         }
      }

      return -1;
   }
}
