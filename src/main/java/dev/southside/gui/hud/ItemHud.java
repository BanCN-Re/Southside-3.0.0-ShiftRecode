package dev.southside.gui.hud;

import cc.polyfrost.oneconfig.hud.Hud;
import com.google.gson.annotations.SerializedName;
import dev.southside.module.impl.world.Scaffold;
import dev.southside.module.values.BooleanValue;
import dev.southside.render.UIMatrixStack;
import dev.southside.utils.SlotIndexMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class ItemHud extends Hud {
   private static final int DEFAULT_INT = 36;
   private static final int DEFAULT_INT_VALUE = 5;
   private static final int DEFAULT_INT_DATA = 20;
   private static final int DEFAULT_INT_REF = 27;
   private static final int DEFAULT_INT_PRIMARY = 10;
   @SerializedName("diamond")
   public final BooleanValue diamond = new BooleanValue("Diamond", true);
   @SerializedName("emerald")
   public final BooleanValue emerald = new BooleanValue("Emerald", true);
   @SerializedName("ironIngot")
   public final BooleanValue ironIngot = new BooleanValue("Iron Ingot", true);
   @SerializedName("goldIngot")
   public final BooleanValue goldIngot = new BooleanValue("Gold Ingot", true);
   @SerializedName("mushroomStew")
   public final BooleanValue mushroomStew = new BooleanValue("Mushroom Stew", true);
   @SerializedName("scaffoldBlocks")
   public final BooleanValue scaffoldBlocks = new BooleanValue("Scaffold Blocks", true);
   @SerializedName("godItems")
   public final BooleanValue godItems = new BooleanValue("God Items", false);
   @SerializedName("hideZeroCount")
   public final BooleanValue hideZeroCount = new BooleanValue("Hide Zero Count", false);

   public ItemHud(float x, float y, float z) {
      super(x, y, 1, z);
   }

   @Override
   public float computeFloat(float f, boolean flag) {
      if (mc.textRenderer == null) {
         return 140.0F * f;
      } else {
         return this.isValidImpl() ? this.computeFloat(this.collectItems(flag)) * f : 180.0F * f;
      }
   }

   @Override
   public float setScale(float f, boolean flag) {
      if (mc.textRenderer == null) {
         return 20.0F * f;
      } else {
         return this.isValidImpl() ? this.computeFloatEx(this.collectItems(flag)) * f : 70.0F * f;
      }
   }

   @Override
   public void draw(DrawContext context, UIMatrixStack matrices, float f, float f2, float f3, boolean flag) {
      if (mc.textRenderer != null && this.isValidImpl()) {
         List list = this.collectItems(flag);
         if (!list.isEmpty()) {
            float f4 = this.computeFloat(list);
            float f5 = this.computeFloatEx(list);
            MatrixStack matrices2 = context.getMatrices();
            matrices2.push();
            matrices2.translate(f, f2, 0.0F);
            matrices2.scale(f3, f3, 1.0F);
            context.fill(0, 0, Math.round(f4), Math.round(f5), 1711276032);
            int n = this.computeIntEx(list);
            int n2 = Math.round(f4) - 5 - n;

            for (int i = 0; i < list.size(); i++) {
               ItemHud.Entry entry = (ItemHud.Entry)list.get(i);
               int n3 = 5 + i * 20;
               int n4 = n3 + 5;
               context.drawItem(entry.getStack(), 5, n3 + 2);
               context.drawText(mc.textRenderer, entry.getName(), 27, n4, -1, false);
               int width = mc.textRenderer.getWidth(entry.getDisplayText());
               int n5 = n2 + (n - width);
               context.drawText(mc.textRenderer, entry.getDisplayText(), n5, n4, -8064, false);
            }

            matrices2.pop();
         }
      }
   }

   private boolean isValidImpl() {
      return this.diamond != null
         && this.emerald != null
         && this.ironIngot != null
         && this.goldIngot != null
         && this.mushroomStew != null
         && this.scaffoldBlocks != null
         && this.godItems != null
         && this.hideZeroCount != null;
   }

   private List<ItemHud.Entry> collectItems(boolean flag) {
      ArrayList list = new ArrayList();
      if ((Boolean)this.diamond.getValue()) {
         this.addEntry(list, new ItemStack(Items.DIAMOND), "Diamond", this.computeInt(Items.DIAMOND, flag), flag);
      }

      if ((Boolean)this.emerald.getValue()) {
         this.addEntry(list, new ItemStack(Items.EMERALD), "Emerald", this.computeInt(Items.EMERALD, flag), flag);
      }

      if ((Boolean)this.ironIngot.getValue()) {
         this.addEntry(list, new ItemStack(Items.IRON_INGOT), "Iron", this.computeInt(Items.IRON_INGOT, flag), flag);
      }

      if ((Boolean)this.goldIngot.getValue()) {
         this.addEntry(list, new ItemStack(Items.GOLD_INGOT), "Gold", this.computeInt(Items.GOLD_INGOT, flag), flag);
      }

      if ((Boolean)this.mushroomStew.getValue()) {
         this.addEntry(list, new ItemStack(Items.MUSHROOM_STEW), "Stew", this.computeInt(Items.MUSHROOM_STEW, flag), flag);
      }

      if ((Boolean)this.scaffoldBlocks.getValue()) {
         this.addEntry(list, this.getItemStack(flag), "Blocks", this.computeInt(flag), flag);
      }

      if ((Boolean)this.godItems.getValue()) {
         this.addEntry(list, new ItemStack(Items.GOLDEN_AXE), "God Axe", this.computeIntEx(flag), flag);
         this.addEntry(list, new ItemStack(Items.MACE), "Mace", this.computeInt(Items.MACE, flag), flag);
         this.addEntry(list, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE), "E-GApple", this.computeInt(Items.ENCHANTED_GOLDEN_APPLE, flag), flag);
         this.addEntry(list, new ItemStack(Items.END_CRYSTAL), "Crystal", this.computeInt(Items.END_CRYSTAL, flag), flag);
      }

      return list;
   }

   private void addEntry(List<ItemHud.Entry> list, ItemStack stack, String text, int n, boolean flag) {
      if (flag || !(Boolean)this.hideZeroCount.getValue() || n > 0) {
         list.add(new ItemHud.Entry(stack, text, n));
      }
   }

   private ItemStack getItemStack(boolean flag) {
      if (!flag && mc.player != null) {
         ItemStack mainHandStack = mc.player.getMainHandStack();
         if (this.isValidItem(mainHandStack)) {
            return mainHandStack;
         }

         ItemStack offHandStack = mc.player.getOffHandStack();
         if (this.isValidItem(offHandStack)) {
            return offHandStack;
         }

         for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (this.isValidItem(stack)) {
               return stack;
            }
         }

         return new ItemStack(Items.STONE);
      } else {
         return new ItemStack(Items.STONE);
      }
   }

   private int computeInt(Item item, boolean flag) {
      return !flag && mc.player != null ? this.computeInt(stack -> !stack.isEmpty() && stack.getItem() == item) : 64;
   }

   private int computeInt(boolean flag) {
      return !flag && mc.player != null ? this.computeInt(this::isValidItem) : 128;
   }

   private int computeIntEx(boolean flag) {
      return !flag && mc.player != null ? this.computeInt(this::isValidItemEx) : 1;
   }

   private int computeInt(Predicate<ItemStack> predicate) {
      int n = 0;

      for (int i = 0; i < 36; i++) {
         ItemStack stack = mc.player.getInventory().getStack(i);
         if (predicate.test(stack)) {
            n += stack.getCount();
         }
      }

      ItemStack offHandStack = mc.player.getOffHandStack();
      if (predicate.test(offHandStack)) {
         n += offHandStack.getCount();
      }

      return n;
   }

   private boolean isValidItem(ItemStack stack) {
      return stack != null
         && !stack.isEmpty()
         && stack.getItem() instanceof BlockItem blockItem
         && !Scaffold.invalidBlocks.contains(blockItem.getBlock())
         && SlotIndexMapper.isValid(stack.getItem());
   }

   private boolean isValidItemEx(ItemStack stack) {
      return stack != null
         && !stack.isEmpty()
         && stack.getItem().equals(Items.GOLDEN_AXE)
         && stack.getComponents() != null
         && stack.getComponents().toString().contains("一刀999!");
   }

   private int computeInt(List<ItemHud.Entry> list) {
      if (mc.textRenderer == null) {
         return 40;
      }

      int n = 0;

      for (ItemHud.Entry entry : list) {
         n = Math.max(n, mc.textRenderer.getWidth(entry.getName()));
      }

      return n;
   }

   private int computeIntEx(List<ItemHud.Entry> list) {
      if (mc.textRenderer == null) {
         return 20;
      }

      int n = 0;

      for (ItemHud.Entry entry : list) {
         n = Math.max(n, mc.textRenderer.getWidth(entry.getDisplayText()));
      }

      return n;
   }

   private float computeFloat(List<ItemHud.Entry> list) {
      if (list.isEmpty()) {
         return 0.0F;
      }

      int n = this.computeInt(list);
      short s = 27177;
      int n2 = this.computeIntEx(list);
      long l = 6243634389103387238L;
      return 27 + n + 10 + n2 + 5;
   }

   private float computeFloatEx(List<ItemHud.Entry> list) {
      return list.isEmpty() ? 0.0F : 10.0F + 20 * list.size();
   }

   private record Entry(ItemStack stack, String name, int count) {
      public String getDisplayText() {
         return String.valueOf(this.count);
      }

      public ItemStack getStack() {
         return this.stack;
      }

      public String getName() {
         return this.name;
      }

      public int getCount() {
         return this.count;
      }
   }
}
