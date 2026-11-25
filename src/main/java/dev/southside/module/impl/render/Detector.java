package dev.southside.module.impl.render;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.events.DrawContextEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.client.Targets;
import dev.southside.module.impl.misc.GodItemDetector;
import dev.southside.module.impl.misc.HackerDetector;
import dev.southside.module.values.BooleanValue;
import dev.southside.render.ProjectionUtils;
import dev.southside.render.NanoVGTransform;
import dev.southside.utils.OneConfigFont;
import dev.southside.utils.OneConfigFonts;
import dev.southside.utils.EnchantmentUtils;
import dev.southside.utils.NanoVGHelper;
import dev.southside.utils.NumberValue;
import dev.southside.utils.FakePlayerEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class Detector extends Module {
   private static final List<EquipmentSlot> lines = List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
   private static final OneConfigFont ONE_CONFIG_FONT = OneConfigFonts.REGULAR;
   private static final Map<RegistryEntry<StatusEffect>, String> EFFECT_SHORT_NAMES = Map.of(
      StatusEffects.SPEED,
      "Spd",
      StatusEffects.STRENGTH,
      "Str",
      StatusEffects.RESISTANCE,
      "Res",
      StatusEffects.REGENERATION,
      "Reg",
      StatusEffects.FIRE_RESISTANCE,
      "FRes",
      StatusEffects.ABSORPTION,
      "Abs",
      StatusEffects.INVISIBILITY,
      "Inv",
      StatusEffects.JUMP_BOOST,
      "Jump",
      StatusEffects.SLOWNESS,
      "Slow",
      StatusEffects.WEAKNESS,
      "Weak"
   );
   @SerializedName("distanceScale")
   public final BooleanValue distanceScale = new BooleanValue("Distance Scale", false);
   @SerializedName("customScale")
   public final BooleanValue customScale = new BooleanValue("Custom Scale", false);
   @SerializedName("scale")
   public final NumberValue scale = new NumberValue("Scale", 1.0F, 0.1F, 10.0F, 0.1F);
   @SerializedName("respectTargets")
   public final BooleanValue respectTargets = new BooleanValue("Respect Targets", true);
   @SerializedName("showDistance")
   public final BooleanValue showDistance = new BooleanValue("Show Distance", true);
   @SerializedName("showProtection")
   public final BooleanValue showProtection = new BooleanValue("Show Protection", true);
   @SerializedName("showPotions")
   public final BooleanValue showPotions = new BooleanValue("Show Potions", true);
   @SerializedName("range")
   public final NumberValue range = new NumberValue("Range", 40.0, 5.0, 120.0, 1.0);
   @SerializedName("offsetY")
   public final NumberValue offsetY = new NumberValue("Offset Y", 0.0, -60.0, 120.0, 1.0);
   private static final long DEFAULT_LONG_VALUE = -7066402969486360576L;

   public Detector() {
      super(Category.Render, "Detector", "在玩家脚底显示状态监控");
   }

   @Override
   public void postInit() {
      super.postInit();
      this.hideIf(this.customScale.getLabel(), this.distanceScale::getValue);
      this.hideIf(this.scale.getLabel(), () -> !(Boolean)this.customScale.getValue() || (Boolean)this.distanceScale.getValue());
   }

   @EventTarget
   public void onDrawContext(DrawContextEvent drawContextEvent) {
      if (mc2.player != null && mc2.world != null) {
         NanoVGHelper nvg = NanoVGHelper.INSTANCE;
         nvg.performAction(true, l -> {
            for (PlayerEntity player : mc2.world.getPlayers()) {
               if (this.isValidTarget(player) && mc2.player.distanceTo(player) <= ((Number)this.range.getValue()).floatValue()) {
                  this.getMaxWidth(nvg, l, player, drawContextEvent.getTickDelta());
               }
            }
         });
      }
   }

   private boolean isValidTarget(PlayerEntity player) {
      return player != mc2.player
         && player.isAlive()
         && !player.isSpectator()
         && (!(Boolean)this.respectTargets.getValue() || !(player instanceof FakePlayerEntity) && (Boolean)Targets.INSTANCE.players.getValue());
   }

   private void getMaxWidth(NanoVGHelper nvg, long l, PlayerEntity player, float f) {
      Vec3d vec = player.getLerpedPos(f).add(0.0, 0.05, 0.0);
      Vec2f vec2f = ProjectionUtils.getVec2f(vec.x, vec.y, vec.z, 0.0, 0.0);
      if (vec2f != null) {
         List list = this.collectItems(player);
         if (!list.isEmpty()) {
            float f2 = 11.0F;
            float f3 = nvg.getTextHeight(l, f2, ONE_CONFIG_FONT) + 2.0F;
            float f4 = 0.0F;

            for (Object obj : list) {
               String text = (String)obj;
               f4 = Math.max(f4, nvg.getTextWidth(l, text, f2, ONE_CONFIG_FONT));
            }

            float f5 = f4 + 18.0F;
            float f6 = f3 * list.size() + 12.0F;
            float f7 = vec2f.x - f5 / 2.0F;
            float f8 = vec2f.y + ((Number)this.offsetY.getValue()).floatValue() - f6;
            double dist = mc2.gameRenderer.getCamera().getPos().distanceTo(player.getLerpedPos(f));
            float f9 = this.customScale.getValue()
               ? (float)this.scale.getCurrentValue()
               : (this.distanceScale.getValue() ? (float)Math.max(0.5, 1.0 - dist * 0.01) : 1.0F);
            NanoVGTransform.scale(vec2f.x, vec2f.y, f9);

            try {
               nvg.drawRoundedRect(l, f7, f8, f5, f6, (int)DEFAULT_LONG_VALUE, 7.5F);
               float f10 = f8 + 8.0F;

               for (Object obj2 : list) {
                  String text2 = (String)obj2;
                  nvg.drawEx(l, text2, f7 + 9.0F, f10, -1, f2, ONE_CONFIG_FONT);
                  f10 += f3;
               }
            } finally {
               NanoVGTransform.restore();
            }
         }
      }
   }

   private List<String> collectItems(PlayerEntity player) {
      ArrayList list = new ArrayList();
      List list2 = this.collectItemsEx(player);
      if (!list2.isEmpty()) {
         list.add(String.join(" ", list2));
      }

      if ((Boolean)this.showDistance.getValue()) {
         String text = String.valueOf(Formatting.GRAY);
         String text2 = String.valueOf(Formatting.WHITE);
         String formatted = String.format("%.1fm", mc2.player.distanceTo(player));
         String text3 = text2;
         list.add(text + "Dist " + text3 + formatted);
      }

      if ((Boolean)this.showProtection.getValue()) {
         list.add(this.formatText(player));
         int n = EnchantmentUtils.computeInt(Enchantments.SHARPNESS, player.getMainHandStack());
         if (!player.getMainHandStack().isEmpty() && n > 0) {
            Formatting formatting = n >= 5 ? Formatting.RED : (n >= 3 ? Formatting.YELLOW : Formatting.WHITE);
            String text4 = String.valueOf(Formatting.GRAY);
            String text5 = String.valueOf(formatting);
            int n2 = n;
            String text6 = text5;
            list.add(text4 + "Sharp " + text6 + n2);
         }
      }

      if ((Boolean)this.showPotions.getValue() && !player.getStatusEffects().isEmpty()) {
         ArrayList list3 = new ArrayList();

         for (StatusEffectInstance statusEffectInstance : player.getStatusEffects()) {
            String text7 = EFFECT_SHORT_NAMES.get(statusEffectInstance.getEffectType());
            if (text7 == null) {
               String string = ((StatusEffect)statusEffectInstance.getEffectType().value()).getName().getString();
               text7 = string.length() > 4 ? string.substring(0, 3) : string;
            }

            int n3 = statusEffectInstance.getAmplifier() + 1;
            list3.add(n3 > 1 ? text7 + n3 : text7);
         }

         list.add(Formatting.AQUA + String.join(" ", list3));
      }

      return list;
   }

   private String formatText(PlayerEntity player) {
      boolean flag = false;
      int n = 0;

      for (EquipmentSlot equipmentSlot : lines) {
         ItemStack equippedStack = player.getEquippedStack(equipmentSlot);
         if (!equippedStack.isEmpty()) {
            flag = true;
            n = Math.max(n, EnchantmentUtils.computeInt(Enchantments.PROTECTION, equippedStack));
         }
      }

      if (!flag) {
         return Formatting.GRAY + "Prot " + Formatting.RED + "None";
      }

      Formatting formatting = n >= 4 ? Formatting.RED : (n >= 2 ? Formatting.YELLOW : Formatting.GREEN);
      String text = String.valueOf(Formatting.GRAY);
      String text2 = String.valueOf(formatting);
      int n2 = n;
      String text3 = text2;
      return text + "Prot " + text3 + n2;
   }

   private List<String> collectItemsEx(PlayerEntity player) {
      ArrayList list = new ArrayList();
      HackerDetector hackerDetector = Southside.INSTANCE.getModuleManager().getModule(HackerDetector.class);
      if (hackerDetector.isEnabled() && hackerDetector.isValidTarget(player)) {
         list.add(Formatting.DARK_RED + "[Hacker]" + Formatting.RESET);
      }

      GodItemDetector godItemDetector = GodItemDetector.getGodItemDetector();
      if (godItemDetector != null && godItemDetector.isEnabled()) {
         GodItemDetector.Mode mode = GodItemDetector.Mode.values()[godItemDetector.godItemTags.getValue()];
         if (mode != GodItemDetector.Mode.BOTH && mode != GodItemDetector.Mode.DETECTOR) {
            return list;
         }

         if (GodItemDetector.notifiedPlayers.contains(player)) {
            list.add(Formatting.GOLD + "[GodAxe]" + Formatting.RESET);
         }

         if (GodItemDetector.godAxeHolders.contains(player)) {
            list.add(Formatting.YELLOW + "[Mace]" + Formatting.RESET);
         }

         if (GodItemDetector.maceHolders.contains(player)) {
            list.add(Formatting.DARK_PURPLE + "[Crystal]" + Formatting.RESET);
         }

         if (GodItemDetector.endCrystalHolders.contains(player)) {
            list.add(Formatting.LIGHT_PURPLE + "[GApple]" + Formatting.RESET);
         }

         return list;
      } else {
         return list;
      }
   }
}
