package dev.southside.utils;

import dev.southside.Southside;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ItemGroup.StackVisibility;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class HiddenItemsKit {
   private static final String DEFAULT_TEXT = "/assets/southside/items/two_b_op_kit.nbt";
   private static final long DEFAULT_LONG = 1048576L;

   private HiddenItemsKit() {
   }

   public static void performAction() {
      NbtCompound nbtCompound = getNbtCompound();
      Registry.register(
         Registries.ITEM_GROUP,
         Identifier.of("southside", "special_items"),
         FabricItemGroup.builder()
            .displayName(Text.translatable("itemGroup.southside.special_items"))
            .icon(Items.BARRIER::getDefaultStack)
            .entries(
               (displayContext, entries) -> {
                  for (Item item : new Item[]{
                     Items.BARRIER,
                     Items.LIGHT,
                     Items.STRUCTURE_VOID,
                     Items.STRUCTURE_BLOCK,
                     Items.JIGSAW,
                     Items.COMMAND_BLOCK,
                     Items.CHAIN_COMMAND_BLOCK,
                     Items.REPEATING_COMMAND_BLOCK,
                     Items.COMMAND_BLOCK_MINECART,
                     Items.DEBUG_STICK,
                     Items.KNOWLEDGE_BOOK,
                     Items.PETRIFIED_OAK_SLAB,
                     Items.SPAWNER,
                     Items.TRIAL_SPAWNER,
                     Items.VAULT,
                     Items.BEDROCK,
                     Items.END_PORTAL_FRAME
                  }) {
                     entries.add(item, StackVisibility.PARENT_TAB_ONLY);
                  }

                  if (nbtCompound != null) {
                     ItemStack.fromNbt(displayContext.lookup(), nbtCompound)
                        .filter(stack -> !stack.isEmpty())
                        .ifPresent(stack2 -> entries.add(stack2.copyWithCount(1), StackVisibility.PARENT_TAB_ONLY));
                  }
               }
            )
            .build()
      );
   }

   private static NbtCompound getNbtCompound() {
      try (InputStream resourceAsStream = HiddenItemsKit.class.getResourceAsStream("/assets/southside/items/two_b_op_kit.nbt")) {
         if (resourceAsStream == null) {
            throw new IOException("Missing resource: /assets/southside/items/two_b_op_kit.nbt");
         } else {
            return NbtIo.readCompound(new DataInputStream(new BufferedInputStream(resourceAsStream)), NbtSizeTracker.of(DEFAULT_LONG));
         }
      } catch (IOException e) {
         Southside.LOGGER.warn("Could not load the hidden items kit", e);
         return null;
      }
   }
}
