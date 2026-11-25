package dev.southside.utils.heypixel;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.data.DataTracker.SerializedEntry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.SetCursorItemS2CPacket;
import net.minecraft.network.packet.s2c.play.SetPlayerInventoryS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;

public final class HeypixelViaversionItemTransfer {
   private HeypixelViaversionItemTransfer() {
   }

   public static Packet<?> transferStack(Packet<?> packet) {
      if (packet instanceof InventoryS2CPacket inventoryS2CPacket) {
         return transferStack(inventoryS2CPacket);
      } else {
         if (packet instanceof ScreenHandlerSlotUpdateS2CPacket screenHandlerSlotUpdateS2CPacket) {
            ItemStack stack = transferStack(screenHandlerSlotUpdateS2CPacket.getStack());
            if (stack != screenHandlerSlotUpdateS2CPacket.getStack()) {
               return new ScreenHandlerSlotUpdateS2CPacket(screenHandlerSlotUpdateS2CPacket.getSyncId(), screenHandlerSlotUpdateS2CPacket.getRevision(), screenHandlerSlotUpdateS2CPacket.getSlot(), stack);
            }
         }

         if (packet instanceof SetPlayerInventoryS2CPacket setPlayerInventoryS2CPacket) {
            ItemStack stack2 = transferStack(setPlayerInventoryS2CPacket.contents());
            if (stack2 != setPlayerInventoryS2CPacket.contents()) {
               return new SetPlayerInventoryS2CPacket(setPlayerInventoryS2CPacket.slot(), stack2);
            }
         }

         if (packet instanceof SetCursorItemS2CPacket setCursorItemS2CPacket) {
            ItemStack stack3 = transferStack(setCursorItemS2CPacket.contents());
            if (stack3 != setCursorItemS2CPacket.contents()) {
               return new SetCursorItemS2CPacket(stack3);
            }
         }

         if (packet instanceof EntityEquipmentUpdateS2CPacket entityEquipmentUpdateS2CPacket) {
            return transferStack(entityEquipmentUpdateS2CPacket);
         } else {
            return packet instanceof EntityTrackerUpdateS2CPacket entityTrackerUpdateS2CPacket ? transferStack(entityTrackerUpdateS2CPacket) : packet;
         }
      }
   }

   public static Packet<?> transferStack(EntityTrackerUpdateS2CPacket entityTrackerUpdateS2CPacket) {
      boolean flag = false;
      ArrayList list = new ArrayList(entityTrackerUpdateS2CPacket.trackedValues().size());

      for (SerializedEntry serializedEntry : entityTrackerUpdateS2CPacket.trackedValues()) {
         if (serializedEntry.value() instanceof ItemStack stack) {
            ItemStack stack2 = transferStack(stack);
            if (stack2 != stack) {
               list.add(getSerializedEntry(serializedEntry, stack2));
               flag = true;
               continue;
            }
         }

         list.add(serializedEntry);
      }

      return flag ? new EntityTrackerUpdateS2CPacket(entityTrackerUpdateS2CPacket.id(), list) : entityTrackerUpdateS2CPacket;
   }

   public static SerializedEntry<ItemStack> getSerializedEntry(SerializedEntry<?> serializedEntry, ItemStack stack) {
      SerializedEntry serializedEntry2 = serializedEntry;
      return new SerializedEntry(serializedEntry2.id(), serializedEntry2.handler(), stack);
   }

   public static Packet<?> transferStack(EntityEquipmentUpdateS2CPacket entityEquipmentUpdateS2CPacket) {
      boolean flag = false;
      ArrayList list = new ArrayList(entityEquipmentUpdateS2CPacket.getEquipmentList().size());

      for (Pair pair : entityEquipmentUpdateS2CPacket.getEquipmentList()) {
         ItemStack second = (ItemStack)pair.getSecond();
         ItemStack stack = transferStack(second);
         list.add(Pair.of((EquipmentSlot)pair.getFirst(), stack));
         flag |= stack != second;
      }

      return flag ? new EntityEquipmentUpdateS2CPacket(entityEquipmentUpdateS2CPacket.getEntityId(), list) : entityEquipmentUpdateS2CPacket;
   }

   public static Packet<?> transferStack(InventoryS2CPacket inventoryS2CPacket) {
      boolean flag = false;
      List contents = inventoryS2CPacket.getContents();
      DefaultedList defaultedList = DefaultedList.ofSize(contents.size(), ItemStack.EMPTY);

      for (int i = 0; i < contents.size(); i++) {
         ItemStack stack = (ItemStack)contents.get(i);
         ItemStack stack2 = transferStack(stack);
         defaultedList.set(i, stack2);
         flag |= stack2 != stack;
      }

      ItemStack cursorStack = inventoryS2CPacket.getCursorStack();
      ItemStack stack3 = transferStack(cursorStack);
      flag |= stack3 != cursorStack;
      return flag ? new InventoryS2CPacket(inventoryS2CPacket.getSyncId(), inventoryS2CPacket.getRevision(), defaultedList, stack3) : inventoryS2CPacket;
   }

   public static ItemStack transferStack(ItemStack stack) {
      if (stack == null || stack.isEmpty()) {
         return stack;
      } else if (isValid(stack, "1.21 Mace")) {
         return getItemStack(stack, Items.MACE);
      } else {
         return isValid(stack, "1.21 Wind Charge") ? getItemStack(stack, Items.WIND_CHARGE) : stack;
      }
   }

   public static boolean isValid(ItemStack stack, String text) {
      NbtComponent nbtComponent = (NbtComponent)stack.get(DataComponentTypes.CUSTOM_DATA);
      return nbtComponent != null && !nbtComponent.isEmpty() && nbtComponent.copyNbt().toString().contains(text);
   }

   public static ItemStack getItemStack(ItemStack stack, Item item) {
      ItemStack stack2 = new ItemStack(item, stack.getCount());
      performAction(stack, stack2);
      return stack2;
   }

   public static void performAction(ItemStack stack, ItemStack stack2) {
      copyComponent(stack, stack2, DataComponentTypes.CUSTOM_DATA);
      copyComponent(stack, stack2, DataComponentTypes.LORE);
      copyComponent(stack, stack2, DataComponentTypes.ENCHANTMENTS);
      copyComponent(stack, stack2, DataComponentTypes.UNBREAKABLE);
      copyComponent(stack, stack2, DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE);
      copyComponent(stack, stack2, DataComponentTypes.REPAIR_COST);
      performActionEx(stack, stack2);
   }

   public static <T> void copyComponent(ItemStack stack, ItemStack stack2, ComponentType<T> componentType) {
      T val = stack.get(componentType);
      if (val != null) {
         stack2.set(componentType, val);
      }
   }

   public static void performActionEx(ItemStack stack, ItemStack stack2) {
      Text customName = stack.getCustomName();
      if (customName != null) {
         String string = customName.getString();
         if (!string.contains("1.21 Mace") && !string.contains("1.21 Wind Charge")) {
            stack2.set(DataComponentTypes.CUSTOM_NAME, customName);
         }
      }
   }
}
