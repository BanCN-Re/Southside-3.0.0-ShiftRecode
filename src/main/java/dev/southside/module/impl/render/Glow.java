package dev.southside.module.impl.render;

import cc.polyfrost.oneconfig.config.core.OneColor;
import com.google.gson.annotations.SerializedName;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.ColorValue;
import dev.southside.utils.ModeValue;
import java.awt.Color;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.scoreboard.Team;

public class Glow extends Module {
   private static final Color teamsTeammateColor = new Color(170, 60, 255);
   private static final Color teamsEnemyColor = new Color(255, 215, 0);
   private static final Color bedwarsFallbackColor = new Color(255, 150, 60);
   private static final Color customPlayerColor = new Color(90, 255, 120);
   private static final Color customAnimalColor = new Color(120, 220, 255);
   private static final Color customMonsterColor = new Color(0, 210, 80);
   @SerializedName("mode")
   public final ModeValue mode = new ModeValue("Mode", "Teams", "Teams", "Bedwars", "Custom");
   @SerializedName("teammateColor")
   public final ColorValue teammateColor = new ColorValue("Teammate Color", new OneColor(0, 255, 140));
   @SerializedName("enemyColor")
   public final ColorValue enemyColor = new ColorValue("Enemy Color", new OneColor(255, 70, 70));
   @SerializedName("useArmorColor")
   public final BooleanValue useArmorColor = new BooleanValue("Use Armor Color", true);
   @SerializedName("fallbackColor")
   public final ColorValue fallbackColor = new ColorValue("Fallback Color", new OneColor(255, 255, 255));
   @SerializedName("playerColor")
   public final ColorValue playerColor = new ColorValue("Player Color", new OneColor(255, 255, 255));
   @SerializedName("animalColor")
   public final ColorValue animalColor = new ColorValue("Animal Color", new OneColor(0, 255, 0));
   @SerializedName("monsterColor")
   public final ColorValue monsterColor = new ColorValue("Monster Color", new OneColor(255, 0, 0));
   @SerializedName("itemColor")
   public final ColorValue itemColor = new ColorValue("Item Color", new OneColor(255, 215, 0));
   @SerializedName("itemsGlow")
   public final BooleanValue itemsGlow = new BooleanValue("Items Glow", false);
   @SerializedName("godItems")
   public final BooleanValue godItems = new BooleanValue("God Items", true);
   @SerializedName("bedwarsItems")
   public final BooleanValue bedwarsItems = new BooleanValue("Bedwars Items", false);

   public Glow() {
      super(Category.Render, "Glow", "实体描边发光");
   }

   @Override
   public void postInit() {
      super.postInit();
      this.addListener(this.mode.getLabel(), () -> this.setInfoText(this.mode.getDisplayTextEx()));
      this.hideIf(this.teammateColor.getLabel(), () -> !this.matches("Teams"));
      this.hideIf(this.enemyColor.getLabel(), () -> !this.matches("Teams"));
      this.hideIf(this.useArmorColor.getLabel(), () -> !this.matches("Bedwars"));
      this.hideIf(this.fallbackColor.getLabel(), () -> !this.matches("Bedwars"));
      this.hideIf(this.playerColor.getLabel(), () -> !this.matches("Custom"));
      this.hideIf(this.animalColor.getLabel(), () -> !this.matches("Custom"));
      this.hideIf(this.monsterColor.getLabel(), () -> !this.matches("Custom"));
      this.hideIf(this.itemColor.getLabel(), () -> !this.matches("Custom"));
      this.hideIf(this.godItems.getLabel(), () -> !(Boolean)this.itemsGlow.getValue());
      this.hideIf(this.bedwarsItems.getLabel(), () -> !(Boolean)this.itemsGlow.getValue());
      this.setInfoText(this.mode.getDisplayTextEx());
   }

   private boolean matches(String text) {
      return this.isEnabled() && this.mode.matches(text);
   }

   public boolean isValidTarget(Entity entity) {
      if (!this.isEnabled() || entity == null || mc2.player == null || entity == mc2.player) {
         return false;
      } else if (entity instanceof PlayerEntity player) {
         return player.isAlive();
      } else {
         return this.itemsGlow.getValue() && entity instanceof ItemEntity itemEntity
            ? this.blendColor(itemEntity.getStack()) != null
            : this.matches("Custom") && entity instanceof LivingEntity living && living.isAlive() && (entity instanceof AnimalEntity || entity instanceof Monster);
      }
   }

   public int computeInt(Entity entity) {
      if (entity == null) {
         return -1;
      }

      if (entity instanceof PlayerEntity player && player.isAlive()) {
         Color color = this.blendColor(player);
         if (color != null) {
            return color.getRGB();
         } else if (this.mode.matches("Bedwars")) {
            return (this.useArmorColor.getValue() ? this.blendColor(player) : ((OneColor)this.fallbackColor.getValue()).blendColor()).getRGB();
         } else {
            return this.mode.matches("Custom")
               ? ((OneColor)this.playerColor.getValue()).blendColor().getRGB()
               : ((OneColor)(this.isValidTarget(player) ? this.teammateColor : this.enemyColor).getValue()).blendColor().getRGB();
         }
      } else {
         if ((Boolean)this.itemsGlow.getValue() && entity instanceof ItemEntity itemEntity) {
            Color color2 = this.blendColor(itemEntity.getStack());
            if (color2 != null) {
               return color2.getRGB();
            }
         }

         if (this.matches("Custom")) {
            if (entity instanceof AnimalEntity) {
               return ((OneColor)this.animalColor.getValue()).blendColor().getRGB();
            }

            if (entity instanceof Monster) {
               return ((OneColor)this.monsterColor.getValue()).blendColor().getRGB();
            }
         }

         Team scoreboardTeam = entity.getScoreboardTeam();
         Integer integer = scoreboardTeam == null ? null : scoreboardTeam.getColor().getColorValue();
         return integer == null ? -1 : 0xFF000000 | integer;
      }
   }

   private Color blendColor(PlayerEntity player) {
      for (int i = 3; i >= 2; i--) {
         ItemStack armorStack = player.getInventory().getArmorStack(i);
         if (!armorStack.isEmpty() && armorStack.getItem() instanceof ArmorItem && armorStack.isIn(ItemTags.DYEABLE)) {
            int color = DyedColorComponent.getColor(armorStack, 0);
            if (color != 0) {
               return new Color(color | 0xFF000000, true);
            }
         }
      }

      return ((OneColor)this.fallbackColor.getValue()).blendColor();
   }

   private boolean isValidTarget(PlayerEntity player) {
      if (mc2.player == null) {
         return false;
      }

      Team team = this.getTeam(mc2.player);
      Team team2 = this.getTeam(player);
      if (team != null && team2 != null) {
         if (team.getName().equals(team2.getName())) {
            return true;
         }

         Integer integer = team.getColor() == null ? null : team.getColor().getColorValue();
         Integer integer2 = team2.getColor() == null ? null : team2.getColor().getColorValue();
         return integer != null && integer.equals(integer2);
      } else {
         return false;
      }
   }

   private Team getTeam(PlayerEntity player) {
      if (mc2.getNetworkHandler() == null) {
         return null;
      }

      PlayerListEntry playerListEntry = mc2.getNetworkHandler().getPlayerListEntry(player.getUuid());
      return playerListEntry == null ? null : playerListEntry.getScoreboardTeam();
   }

   private Color blendColor(ItemStack stack) {
      if (stack.isEmpty()) {
         return null;
      }

      if ((Boolean)this.godItems.getValue()) {
         Color color = this.blendColorEx(stack);
         if (color != null) {
            return color;
         }
      }

      if ((Boolean)this.bedwarsItems.getValue()) {
         if (stack.isOf(Items.DIAMOND)) {
            return customAnimalColor;
         }

         if (stack.isOf(Items.EMERALD)) {
            return customMonsterColor;
         }
      }

      return this.matches("Custom") ? ((OneColor)this.itemColor.getValue()).blendColor() : null;
   }

   private Color blendColor(LivingEntity living) {
      if ((Boolean)this.itemsGlow.getValue() && (Boolean)this.godItems.getValue()) {
         Color color = this.blendColorEx(living.getMainHandStack());
         return color != null ? color : this.blendColorEx(living.getOffHandStack());
      } else {
         return null;
      }
   }

   private Color blendColorEx(ItemStack stack) {
      if (stack.isEmpty()) {
         return null;
      } else if (stack.isOf(Items.END_CRYSTAL)) {
         return teamsTeammateColor;
      } else if (stack.isOf(Items.GOLDEN_AXE) && stack.getComponents().toString().contains("一刀999!")) {
         return teamsEnemyColor;
      } else if (stack.isOf(Items.TOTEM_OF_UNDYING)) {
         return bedwarsFallbackColor;
      } else {
         return stack.isOf(Items.SLIME_BALL) ? customPlayerColor : null;
      }
   }
}
