package dev.southside.utils;

import dev.southside.mixin.accessors.ICreativeInventoryScreen;
import dev.southside.mixin.accessors.IHorseScreenHandler;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CactusBlock;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.CarpetBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.ScaffoldingBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.SugarCaneBlock;
import net.minecraft.block.TntBlock;
import net.minecraft.block.TorchBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen.CreativeScreenHandler;
import net.minecraft.entity.mob.SkeletonHorseEntity;
import net.minecraft.entity.mob.ZombieHorseEntity;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.BeaconScreenHandler;
import net.minecraft.screen.BlastFurnaceScreenHandler;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.screen.CartographyTableScreenHandler;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.FurnaceScreenHandler;
import net.minecraft.screen.Generic3x3ContainerScreenHandler;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.HopperScreenHandler;
import net.minecraft.screen.HorseScreenHandler;
import net.minecraft.screen.LecternScreenHandler;
import net.minecraft.screen.LoomScreenHandler;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.SmokerScreenHandler;
import net.minecraft.screen.StonecutterScreenHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.EmptyBlockView;

public class SlotIndexMapper implements IMinecraft {
   public static final int DEFAULT_INT = 0;
   public static final int DEFAULT_INT_VALUE = 8;
   public static final int DEFAULT_INT_DATA = 45;
   public static final int DEFAULT_INT_REF = 9;
   public static final int DEFAULT_INT_PRIMARY = 35;
   public static final int DEFAULT_INT_SECONDARY = 36;
   public static final int DEFAULT_INT_TERTIARY = 39;

   public static int computeInt(int n) {
      if (mc.player == null) {
         return -1;
      } else {
         ScreenHandler currentScreenHandler = mc.player.currentScreenHandler;
         if (currentScreenHandler instanceof PlayerScreenHandler) {
            return computeIntAlt(n);
         } else if (currentScreenHandler instanceof CreativeScreenHandler) {
            return computeIntV2(n);
         } else if (currentScreenHandler instanceof GenericContainerScreenHandler genericContainerScreenHandler) {
            return computeIntEx(n, genericContainerScreenHandler.getRows());
         } else if (currentScreenHandler instanceof CraftingScreenHandler) {
            return computeIntDirect(n);
         } else if (currentScreenHandler instanceof FurnaceScreenHandler || currentScreenHandler instanceof BlastFurnaceScreenHandler || currentScreenHandler instanceof SmokerScreenHandler) {
            return computeIntV3(n);
         } else if (currentScreenHandler instanceof Generic3x3ContainerScreenHandler) {
            return computeIntWithOffset(n);
         } else if (currentScreenHandler instanceof EnchantmentScreenHandler) {
            return computeIntHelper(n);
         } else if (currentScreenHandler instanceof BrewingStandScreenHandler) {
            return computeIntV5(n);
         } else if (currentScreenHandler instanceof MerchantScreenHandler) {
            return computeIntV4(n);
         } else if (currentScreenHandler instanceof BeaconScreenHandler) {
            return computeIntImpl(n);
         } else if (currentScreenHandler instanceof AnvilScreenHandler) {
            return computeIntCustom(n);
         } else if (currentScreenHandler instanceof HopperScreenHandler) {
            return computeIntInternal(n);
         } else if (currentScreenHandler instanceof ShulkerBoxScreenHandler) {
            return computeIntEx(n, 3);
         } else if (currentScreenHandler instanceof HorseScreenHandler) {
            return computeInt(currentScreenHandler, n);
         } else if (currentScreenHandler instanceof CartographyTableScreenHandler) {
            return computeIntV6(n);
         } else if (currentScreenHandler instanceof GrindstoneScreenHandler) {
            return computeIntSafe(n);
         } else if (currentScreenHandler instanceof LecternScreenHandler) {
            return -1;
         } else if (currentScreenHandler instanceof LoomScreenHandler) {
            return computeIntByIndex(n);
         } else {
            return currentScreenHandler instanceof StonecutterScreenHandler ? computeIntEx(n) : -1;
         }
      }
   }

   public static boolean isKeyDown(int n) {
      return n >= 36 && n <= 39;
   }

   private static int computeInt(int n, int n2) {
      if (isKeyDownEx(n)) {
         return n2 + 27 + n;
      } else {
         return isKeyDownAlt(n) ? n2 + n - 9 : -1;
      }
   }

   public static int computeIntEx(int n) {
      return computeInt(n, 2);
   }

   public static int computeIntAlt(int n) {
      if (isKeyDownEx(n)) {
         return 36 + n;
      } else {
         return isKeyDown(n) ? 5 + n - 36 : n;
      }
   }

   public static int computeIntImpl(int n) {
      return computeInt(n, 1);
   }

   public static boolean isValidBlock(Block block) {
      BlockState defaultState = block.getDefaultState();
      if (!(block instanceof FallingBlock)
         && !(block instanceof AirBlock)
         && !(block instanceof FluidBlock)
         && !(block instanceof ScaffoldingBlock)
         && !(block instanceof TorchBlock)
         && !(block instanceof CarpetBlock)
         && !(block instanceof DoorBlock)
         && !(block instanceof TrapdoorBlock)
         && !(block instanceof FenceBlock)
         && !(block instanceof WallBlock)
         && !(block instanceof PaneBlock)
         && !(block instanceof SlabBlock)
         && !(block instanceof StairsBlock)
         && !(block instanceof PlantBlock)
         && !(block instanceof SugarCaneBlock)
         && !(block instanceof TntBlock)
         && !(block instanceof CactusBlock)
         && !(block instanceof CampfireBlock)) {
         VoxelShape collisionShape = defaultState.getCollisionShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN);
         return Block.isShapeFullCube(collisionShape);
      } else {
         return false;
      }
   }

   public static int computeIntDirect(int n) {
      return computeInt(n, 10);
   }

   public static boolean isValid(Item item) {
      return item instanceof BlockItem blockItem && isValidBlock(blockItem.getBlock());
   }

   public static int computeIntEx(int n, int n2) {
      return computeInt(n, n2 * 9);
   }

   public static boolean isKeyDownEx(int n) {
      return n >= 0 && n <= 8;
   }

   public static int computeIntSafe(int n) {
      return computeInt(n, 3);
   }

   public static int computeIntCustom(int n) {
      return computeInt(n, 3);
   }

   public static int computeIntWithOffset(int n) {
      return computeInt(n, 9);
   }

   public static int computeIntByIndex(int n) {
      return computeInt(n, 4);
   }

   public static int computeIntInternal(int n) {
      return computeInt(n, 5);
   }

   public static int computeIntHelper(int n) {
      return computeInt(n, 2);
   }

   public static int computeIntV2(int n) {
      return mc.currentScreen instanceof CreativeInventoryScreen
            && ICreativeInventoryScreen.getSelectedTab() == Registries.ITEM_GROUP.get(ItemGroups.INVENTORY)
         ? computeIntAlt(n)
         : -1;
   }

   public static int computeIntV3(int n) {
      return computeInt(n, 3);
   }

   public static int computeIntV4(int n) {
      return computeInt(n, 3);
   }

   public static int computeIntV5(int n) {
      return computeInt(n, 5);
   }

   public static boolean isKeyDownAlt(int n) {
      return n >= 9 && n <= 35;
   }

   public static int computeIntV6(int n) {
      return computeInt(n, 3);
   }

   public static int computeInt(ScreenHandler handler, int n) {
      AbstractHorseEntity entity = ((IHorseScreenHandler)handler).getEntity();
      if (entity instanceof LlamaEntity llamaEntity) {
         return computeInt(n, 3 + 3 * llamaEntity.getStrength());
      } else if (entity instanceof HorseEntity || entity instanceof SkeletonHorseEntity || entity instanceof ZombieHorseEntity) {
         return computeInt(n, 2);
      } else {
         return entity instanceof AbstractDonkeyEntity abstractDonkeyEntity ? computeInt(n, abstractDonkeyEntity.hasChest() ? 17 : 2) : -1;
      }
   }
}
